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

enum ObjectTag : uint32_t {
    FORWARDED_FLAG = 1,
    ARRAY = 0x03,
    CLOSURE = 0x05,
    STRING = 0x07,
    RECORD = 0x09,
    REF = 0x11,
    PLACEHOLDER = 0x3
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
        uint8_t *forwarded_pointer;
    };

    [[nodiscard]]
    inline bool is_forwarded() const {
        return !(tag & FORWARDED_FLAG);
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

    void update_finalizers(GcSpace &oldSpace);

public:
    explicit GarbageCollector(size_t mem_size);

    ~GarbageCollector();

    void *allocate(size_t size, ObjectTag tag, void *fp);

    void collect(void *fp);

    inline void *allocate(size_t size, ObjectTag tag) {
        return allocate(size, tag, get_frame_pointer());
    }

    inline void collect() {
        return collect(get_frame_pointer());
    }

    void register_finalizer(void *object, FinalizerEntry::FinalizerFunction const &finalizer);
};

extern GarbageCollector garbageCollector;
