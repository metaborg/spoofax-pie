#include <cstring>
#include "array.h"
#include "gc.h"

int64_t array_concat(int64_t a_ptr, int64_t b_ptr) {
    auto *a = reinterpret_cast<uint64_t *>(a_ptr);
    auto *b = reinterpret_cast<uint64_t *>(b_ptr);
    uint64_t a_length = a[-1];
    uint64_t b_length = b[-1];
    uint64_t new_length = a[-1] + b[-1];
    auto *result = static_cast<uint64_t *>(gc_alloc((new_length + 1) * sizeof(uint64_t))) + 1;
    result[-1] = new_length;
    memcpy(result, a, a_length * sizeof(uint64_t));
    memcpy(result + a_length, b, b_length * sizeof(uint64_t));

    return reinterpret_cast<int64_t>(result);
}

int64_t array_tail(int64_t a_ptr) {
    auto *a = reinterpret_cast<uint64_t *>(a_ptr);
    uint64_t new_length = a[-1] - 1;
    auto *result = static_cast<uint64_t *>(gc_alloc((new_length + 1) * sizeof(uint64_t))) + 1;
    result[-1] = new_length;
    memcpy(result, a + 1, new_length * sizeof(uint64_t));
    return reinterpret_cast<int64_t>(result);
}
