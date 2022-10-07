#pragma once

#include <cstdint>
#include <llvm/Object/StackMapParser.h>

using StackMapParser = llvm::StackMapParser<llvm::support::native>;
using StackRecord = StackMapParser::RecordAccessor;
using llvm::Optional;
using GcVisitor = std::function<void(uint8_t *&ptr)>;

static inline void *get_frame_pointer() {
    void *frame_pointer;
    asm ("movq %%rbp, %0" : "=r"(frame_pointer));
    return frame_pointer;
}

struct GcSpace {
    uint8_t *start;
    uint8_t *end;
    uint8_t *free_ptr;

    explicit GcSpace(size_t size);

    GcSpace(GcSpace &&space) noexcept;

    GcSpace &operator=(GcSpace &&other) noexcept;

    void swap(GcSpace &other);

    inline bool contains(const uint8_t *pointer) const {
        return pointer >= start && pointer < free_ptr;
    }

    ~GcSpace();
};

struct ObjectMetadata {
    uint64_t tag;
    uint64_t size;
};

class GarbageCollector {
private:
    GcSpace old_space;
    GcSpace active_space;
    StackMapParser parser;

    void swap_spaces();

    Optional<const StackRecord> find_record(uint64_t ret_addr);

    void scan_stack(void *fp, const GcVisitor &visitor);

    void visitor(uint8_t *&pointer);

public:
    explicit GarbageCollector(size_t mem_size);

    ~GarbageCollector();

    void *allocate(size_t size, void *fp);

    void *allocate_no_collect(size_t size);

    void collect(void *fp);

    inline void *allocate(size_t size) {
        return allocate(size, get_frame_pointer());
    }

    inline void collect() {
        return collect(get_frame_pointer());
    }
};

extern GarbageCollector garbageCollector;
