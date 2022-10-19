#include <cstring>
#include <iostream>
#include "array.h"
#include "GarbageCollector.h"

[[maybe_unused]]
uint64_t *array_concat(uint64_t *a, uint64_t *b) {
    std::cerr << "ARRAY_CONCAT" << std::endl;
    uint64_t a_length = a[0];
    uint64_t b_length = b[0];
    uint64_t new_length = a[0] + b[0];
    std::cerr << "Array at: " << (void *) a << " " << (void *) b << std::endl;
    auto *result = static_cast<uint64_t *>(garbageCollector.allocate((new_length + 1) * sizeof(uint64_t), ARRAY,
                                                                     reinterpret_cast<void *&>(a),
                                                                     reinterpret_cast<void *&>(b)));
    if (garbageCollector.get_has_pointers(a)) {
        garbageCollector.mark_has_pointers(result);
    }
    std::cerr << "Array now at: " << (void *) a << " " << (void *) b << std::endl;
    result[0] = new_length;
    memcpy(result + 1, a + 1, a_length * sizeof(uint64_t));
    memcpy(result + 1 + a_length, b + 1, b_length * sizeof(uint64_t));

    return result;
}

[[maybe_unused]]
uint64_t *array_tail(uint64_t *a) {
    std::cerr << "ARRAY_TAIL" << std::endl;
    uint64_t new_length = a[0] - 1;
    auto *result = static_cast<uint64_t *>(garbageCollector.allocate((new_length + 1) * sizeof(uint64_t), ARRAY,
                                                                     reinterpret_cast<void *&>(a)));
    if (garbageCollector.get_has_pointers(a)) {
        garbageCollector.mark_has_pointers(result);
    }
    result[0] = new_length;
    memcpy(result + 1, a + 2, new_length * sizeof(uint64_t));
    return result;
}
