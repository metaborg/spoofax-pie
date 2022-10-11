#include <iostream>
#include "gc.h"
#include "GarbageCollector.h"

void gc_init() {
    // NOOP
}

void *gc_alloc(uint64_t size) {
    std::cerr << "GC_ALLOC" << std::endl;
    return garbageCollector.allocate(size, PLACEHOLDER);
}

void gc_collect() {
    garbageCollector.collect();
}