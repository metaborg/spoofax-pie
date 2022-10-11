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
    return garbageCollector.allocate(size, PLACEHOLDER);
}

[[maybe_unused]]
void gc_collect() {
    garbageCollector.collect();
}