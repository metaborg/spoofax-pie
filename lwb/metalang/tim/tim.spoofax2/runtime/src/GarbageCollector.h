#pragma once

#include <cstdint>
#include <utility>
#include <llvm/Object/StackMapParser.h>

using StackMapParser = llvm::StackMapParser<llvm::support::native>;
using StackRecord = StackMapParser::RecordAccessor;
using llvm::Optional;

static inline void *get_frame_pointer() {
    void *frame_pointer;
#ifdef __x86_64__
    asm ("movq %%rbp, %0" : "=r"(frame_pointer));
#elif defined(__i386__)
    asm ("movl %%ebp, %0" : "=r"(frame_pointer));
#else
#error Support for this architecture is not yet implemented
#endif
    return frame_pointer;
}

struct GcSpace {
    uint8_t *start;
    uint8_t *end;
    uint8_t *free_ptr;

    explicit GcSpace(size_t size);

    GcSpace(GcSpace &&space) noexcept;

    GcSpace &operator=(GcSpace &&other) noexcept;

    friend void swap(GcSpace &one, GcSpace &other);

    inline bool contains(const uint8_t *pointer) const {
        return pointer >= start && pointer < free_ptr;
    }

    ~GcSpace();
};

/* Types:
 * Forwarded pointer 0x0
 * Array (with or without pointers)
 * Record/hashmap (with or without pointers)
 * Closure (with bitmap encoding pointers, 2 bits tag, 6 bits size, 56 bits pointer field)
 * bit 0: ~forward
 * bit 1: bitfield encoding
 * bit 2: Record
 * bit 3: Contains pointers
 */
enum ObjectTag : uint32_t {
    NOT_FORWARDED_FLAG = 0x01,
    BITFIELD_FLAG = 0x02,
    RECORD_FLAG = 0x04,
    POINTER_FLAG = 0x08,
    CLOSURE = NOT_FORWARDED_FLAG | BITFIELD_FLAG,
    STRUCT = NOT_FORWARDED_FLAG | BITFIELD_FLAG,
    ARRAY = NOT_FORWARDED_FLAG | POINTER_FLAG,
    INT_ARRAY = NOT_FORWARDED_FLAG,
    STRING = NOT_FORWARDED_FLAG,
    RECORD = NOT_FORWARDED_FLAG | RECORD_FLAG | POINTER_FLAG,
    INT_RECORD = NOT_FORWARDED_FLAG | RECORD_FLAG,
};

struct ObjectMetadata {
    union {
        struct {
            // Bit 0 of the object tag indicates whether this is a pointer or an object
            // Essentials of Compilation, Jeremy Siek
#ifdef LITTLE_ENDIAN
            ObjectTag tag;
            uint32_t size;
#elif defined BIG_ENDIAN
            uint32_t size;
            ObjectTag tag;
#endif
        };
        struct {
            uint64_t bitfield;
        };
        uint8_t *forwarded_pointer;
    };

    [[nodiscard]]
    inline bool is_forwarded() const {
        return !(tag & NOT_FORWARDED_FLAG);
    }

    [[nodiscard]]
    inline size_t get_bitfield_size() const {
        return ((bitfield >> 2) & 0x3F) * sizeof(uint64_t);
    }

    [[nodiscard]]
    inline uint64_t get_ptr_bitfield() const {
        return bitfield >> 8;
    }
};

struct FinalizerEntry {
    using FinalizerFunction = std::function<void(void *)>;
    ObjectMetadata *object_metadata;
    std::function<void(void *)> finalizer;

    FinalizerEntry(ObjectMetadata *object, FinalizerFunction const &finalizer) : object_metadata(object),
                                                                                 finalizer(finalizer) {}

    FinalizerEntry(ObjectMetadata *object, FinalizerFunction &&finalizer) : object_metadata(object),
                                                                            finalizer(std::move(finalizer)) {}

    FinalizerEntry(FinalizerEntry const &other) = default;

    [[nodiscard]]
    inline bool is_in_space(GcSpace const &gcSpace) const {
        auto *ptr = reinterpret_cast<uint8_t *>(object_metadata);
        return ptr >= gcSpace.start && ptr < gcSpace.free_ptr;
    }
};

class GarbageCollector {
private:
    GcSpace old_space;
    GcSpace active_space;
    std::vector<FinalizerEntry> finalizers;
    StackMapParser parser;

    void swap_spaces();

    Optional<const StackRecord> find_record(uint64_t ret_addr);

    void scan_stack(void *fp);

    void relocate(uint8_t *&pointer);

    void visit_heap(GcSpace &space);

    void *allocate_no_collect(size_t size, ObjectTag tag);

    void *allocate_bitfield_no_collect(size_t size, uint64_t bitfield);

    void update_finalizers(GcSpace &oldSpace);


    template<typename... Ts>
    inline void scan_roots(void *&root, Ts &... roots) {
        relocate(reinterpret_cast<uint8_t *&>(root));
        scan_roots(roots...);
    }

    inline void scan_roots() {}  // Noop

public:
    explicit GarbageCollector(size_t mem_size);

    ~GarbageCollector();

    template<typename... Ts>
    void *allocate_fp(size_t size, ObjectTag tag, void *fp, Ts &... roots) {
        collect_fp(fp, roots...);
        return allocate_no_collect(size, tag);
    }

    template<typename... Ts>
    void *allocate_bitfield_fp(size_t size, uint64_t bitfield, void *fp, Ts &... roots) {
        collect_fp(fp, roots...);
        void *result = allocate_bitfield_no_collect(size, bitfield);
        return result;
    }

    template<typename... Ts>
    void collect_fp(void *fp, Ts &... roots) {
        swap_spaces();
        scan_roots(roots...);
        if (fp != nullptr) {
            scan_stack(fp);
        }
        visit_heap(active_space);
        update_finalizers(old_space);
        old_space.free_ptr = old_space.start;
    }

    template<typename... Ts>
    inline void *allocate(size_t size, ObjectTag tag, Ts &... roots) {
        return allocate_fp(size, tag, get_frame_pointer(), roots...);
    }

    template<typename... Ts>
    inline void *allocate_bitfield(size_t size, uint64_t bitfield, Ts &... roots) {
        return allocate_bitfield_fp(size, bitfield, get_frame_pointer(), roots...);
    }

    template<typename... Ts>
    inline void collect(Ts &... roots) {
        return collect_fp(get_frame_pointer(), roots...);
    }

    void register_finalizer(void *object, FinalizerEntry::FinalizerFunction const &finalizer);

    void mark_has_pointers(void *ptr) {
        auto *object = reinterpret_cast<ObjectMetadata *>(ptr) - 1;
        object->tag = static_cast<ObjectTag>(object->tag | POINTER_FLAG);
    }

    [[nodiscard]]
    bool get_has_pointers(void *ptr) {
        auto *object = reinterpret_cast<ObjectMetadata *>(ptr) - 1;
        return (object->tag & POINTER_FLAG) != 0;
    }
};

extern GarbageCollector garbageCollector;
