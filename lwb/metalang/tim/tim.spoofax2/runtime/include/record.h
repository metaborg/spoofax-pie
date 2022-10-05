#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

void* record_new(uint64_t, ...);
int64_t record_write(void *record, const char *str, int64_t value);
int64_t record_read(void *record_ptr, const char *text);
void record_delete(void *record_ptr);

#ifdef __cplusplus
}
#endif