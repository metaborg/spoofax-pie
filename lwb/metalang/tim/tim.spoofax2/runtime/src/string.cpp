#include <cstring>
#include <cstdlib>
#include <cstdio>
#include <iostream>
#include "string.h" // NOLINT(modernize-deprecated-headers)
#include "gc.h"

const char * string_concat(const char *a, const char *b) {
    auto* fp = get_frame_pointer();
    std::cerr << "STRING_CONCAT" << std::endl;
    uint64_t a_length = strlen(a);
    uint64_t b_length = strlen(b);
    uint64_t new_length = a_length + b_length + 1;
    auto *result = static_cast<char *>(gc_alloc_fp(new_length * sizeof(char), fp));
    strcpy(result, a);
    strcpy(result + a_length, b);
    return result;
}

const char * string_index(const char *str, int64_t index) {
    auto* fp = get_frame_pointer();
    std::cerr << "STRING_INDEX" << std::endl;
    auto *result = static_cast<char *>(gc_alloc_fp(2 * sizeof(char), fp));
    result[0] = str[index];
    result[1] = 0;
    return result;
}

const char * int_to_string(int64_t num) {
    auto* fp = get_frame_pointer();
    std::cerr << "INT_TO_STRING" << std::endl;
    char buffer[256];
    size_t size = snprintf(buffer, sizeof(buffer), "%ld", num) + 1;
    char* result = static_cast<char *>(gc_alloc_fp(size * sizeof(char), fp));
    strcpy(result, buffer);
    return result;
}