#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

int64_t record_new(uint64_t, ...);
int64_t record_write(int64_t, int64_t, int64_t);
int64_t record_read(int64_t, int64_t);
void record_delete(int64_t);

#ifdef __cplusplus
}
#endif