#include <cstring>
#include <cstdlib>
#include <cstdio>
#include "string.h" // NOLINT(modernize-deprecated-headers)
#include "gc.h"

int64_t string_concat(int64_t a_ptr, int64_t b_ptr) {
    auto *a = reinterpret_cast<const char *>(a_ptr);
    auto *b = reinterpret_cast<const char *>(b_ptr);
    uint64_t a_length = strlen(a);
    uint64_t b_length = strlen(b);
    uint64_t new_length = a_length + b_length + 1;
    auto *result = static_cast<char *>(malloc(new_length * sizeof(char)));
    strcpy(result, a);
    strcpy(result + a_length, b);
    return reinterpret_cast<int64_t>(result);
}

int64_t string_index(int64_t str_ptr, int64_t index) {
    auto *str = reinterpret_cast<const char*>(str_ptr);
    auto *result = static_cast<char *>(malloc(2 * sizeof(char)));
    result[0] = str[index];
    result[1] = 0;
    return reinterpret_cast<int64_t>(result);
}

int64_t int_to_string(int64_t num) {
    char buffer[256];
    size_t size = snprintf(buffer, sizeof(buffer), "%ld", num) + 1;
    char* result = static_cast<char *>(gc_alloc(size * sizeof(char)));
    strcpy(result, buffer);
    return reinterpret_cast<int64_t>(result);
}