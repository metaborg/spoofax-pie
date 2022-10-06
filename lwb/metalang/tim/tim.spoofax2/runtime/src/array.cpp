#include <cstring>
#include <iostream>
#include "array.h"
#include "gc.h"

uint64_t *array_concat(uint64_t *a, uint64_t *b) {
    auto *fp = get_frame_pointer();
    std::cerr << "ARRAY_CONCAT" << std::endl;
    uint64_t a_length = a[0];
    uint64_t b_length = b[0];
    uint64_t new_length = a[0] + b[0];
    auto *result = static_cast<uint64_t *>(gc_alloc_fp((new_length + 1) * sizeof(uint64_t), fp));
    result[0] = new_length;
    memcpy(result + 1, a + 1, a_length * sizeof(uint64_t));
    memcpy(result + 1 + a_length, b + 1, b_length * sizeof(uint64_t));

    return result;
}

uint64_t *array_tail(uint64_t *a) {
    auto *fp = get_frame_pointer();
    std::cerr << "ARRAY_TAIL" << std::endl;
    uint64_t new_length = a[0] - 1;
    auto *result = static_cast<uint64_t *>(gc_alloc_fp((new_length + 1) * sizeof(uint64_t), fp));
    result[0] = new_length;
    memcpy(result + 1, a + 2, new_length * sizeof(uint64_t));
    return result;
}
