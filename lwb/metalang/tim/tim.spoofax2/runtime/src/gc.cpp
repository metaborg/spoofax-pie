#include <cstdlib>
#include "gc.h"

void gc_init() {
    // NOOP
}

void *gc_alloc(uint64_t size) {
    return malloc(size);
}

void gc_collect() {
    // NOOP
}