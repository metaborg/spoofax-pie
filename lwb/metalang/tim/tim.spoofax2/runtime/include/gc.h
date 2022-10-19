#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

void gc_init();
void *gc_alloc(uint64_t);
void *gc_alloc_bitfield(uint64_t, uint64_t);
void gc_mark_has_pointers(uint64_t *object);
void gc_collect();

#ifdef __cplusplus
}
#endif
