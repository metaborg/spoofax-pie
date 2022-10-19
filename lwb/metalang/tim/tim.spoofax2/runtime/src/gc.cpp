#include <iostream>
#include "gc.h"
#include "GarbageCollector.h"

[[maybe_unused]]
void gc_init() {
    // NOOP
}

[[maybe_unused]]
void *gc_alloc(uint64_t size) {
    std::cerr << "GC_ALLOC" << std::endl;
    return garbageCollector.allocate(size, NOT_FORWARDED_FLAG);
}

void *gc_alloc_bitfield(uint64_t size, uint64_t bitfield) {
    std::cerr << "GC_ALLOC" << std::endl;
    return garbageCollector.allocate_bitfield(size, bitfield);
}

[[maybe_unused]]
void gc_mark_has_pointers(uint64_t *object) {
    garbageCollector.mark_has_pointers(object);
}

[[maybe_unused]]
void gc_collect() {
    garbageCollector.collect();
}