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
    ObjectTag tag = INT_ARRAY;
    if (garbageCollector.get_has_pointers(a)) {
        tag = static_cast<ObjectTag>(tag | POINTER_FLAG);
    }
    auto *result = static_cast<uint64_t *>(garbageCollector.allocate((new_length + 1) * sizeof(uint64_t), tag,
                                                                     reinterpret_cast<void *&>(a),
                                                                     reinterpret_cast<void *&>(b)));
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
    ObjectTag tag = INT_ARRAY;
    if (garbageCollector.get_has_pointers(a)) {
        tag = static_cast<ObjectTag>(tag | POINTER_FLAG);
    }
    auto *result = static_cast<uint64_t *>(garbageCollector.allocate((new_length + 1) * sizeof(uint64_t), tag,
                                                                     reinterpret_cast<void *&>(a)));
    result[0] = new_length;
    memcpy(result + 1, a + 2, new_length * sizeof(uint64_t));
    return result;
}
