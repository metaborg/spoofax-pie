#include <map>
#include "gc.h"
#include "record.h"

using Record = std::map<std::string, int64_t>;

int64_t record_new(uint64_t pair_count, ...) {
    auto *record = static_cast<Record *>(gc_alloc(sizeof(Record)));
    new(record) Record;
    va_list args;
    va_start(args, pair_count);
    for (uint64_t i = 0; i < pair_count; i++) {
        const char *key = va_arg(args, const char*);
        int64_t value = va_arg(args, int64_t);
        (*record)[key] = value;
    }
    return reinterpret_cast<int64_t>(record);
}

int64_t record_write(int64_t record_ptr, int64_t str_ptr, int64_t value) {
    auto &record = *reinterpret_cast<Record *>(record_ptr);
    const char *text = reinterpret_cast<const char *>(str_ptr);
    record[text] = value;
    return value;
}

int64_t record_read(int64_t record_ptr, int64_t str_ptr) {
    auto &record = *reinterpret_cast<Record *>(record_ptr);
    const char *text = reinterpret_cast<const char *>(str_ptr);
    auto search = record.find(text);
    if (search == record.end()) {
        printf("Invalid record read %s\n", text);
        exit(-1);
    }
    return search->second;
}

void record_delete(int64_t record_ptr) {
    auto &record = *reinterpret_cast<Record *>(record_ptr);
    record.~Record();
}