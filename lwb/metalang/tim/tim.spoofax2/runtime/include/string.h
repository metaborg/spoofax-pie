#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

const char * string_concat(const char *a, const char *b);
const char * string_index(const char *str, int64_t index);
const char * int_to_string(int64_t num);

#ifdef __cplusplus
}
#endif
