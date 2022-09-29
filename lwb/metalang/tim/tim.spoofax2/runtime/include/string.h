#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

int64_t string_concat(int64_t a, int64_t b);
int64_t string_index(int64_t str, int64_t index);
int64_t int_to_string(int64_t num);

#ifdef __cplusplus
}
#endif
