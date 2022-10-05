#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

uint64_t * array_concat(uint64_t *a, uint64_t *b);
uint64_t * array_tail(uint64_t *a);

#ifdef __cplusplus
}
#endif
