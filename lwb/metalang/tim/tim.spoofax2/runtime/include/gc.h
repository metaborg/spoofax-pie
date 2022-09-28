#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

void gc_init();
void *gc_alloc(uint64_t);
void gc_collect();

#ifdef __cplusplus
}
#endif
