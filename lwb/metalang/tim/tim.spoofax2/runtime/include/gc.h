#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

static inline void *get_frame_pointer() {
    void *frame_pointer;
    asm ("movq %%rbp, %0" : "=r"(frame_pointer));
    return frame_pointer;
}

void gc_init();
void *gc_alloc(uint64_t);
void *gc_alloc_fp(uint64_t, void*);
void gc_collect();

#ifdef __cplusplus
}
#endif
