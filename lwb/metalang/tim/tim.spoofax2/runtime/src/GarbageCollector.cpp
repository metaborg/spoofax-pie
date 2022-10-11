#include <iostream>
#include <map>
#include "GarbageCollector.h"

extern uint8_t __LLVM_StackMaps[]; // NOLINT(bugprone-reserved-identifier)

GarbageCollector::GarbageCollector(size_t mem_size) :
        parser(StackMapParser(llvm::ArrayRef<uint8_t>(__LLVM_StackMaps, SIZE_MAX))),
        old_space(mem_size), active_space(mem_size) {
}

GarbageCollector::~GarbageCollector() {
    collect(nullptr);
    assert(old_space.free_ptr == old_space.start);
}

void *GarbageCollector::allocate(size_t size, ObjectTag tag, void *fp) {
    collect(fp);
    return allocate_no_collect(size, tag);
}

void *GarbageCollector::allocate_no_collect(size_t size, ObjectTag tag) {
    size_t total_size = size + sizeof(ObjectMetadata);  // Type tag and size
    total_size = (total_size + 7) & ~7;  // Ensure we are 8 byte aligned
    if (active_space.free_ptr + total_size > active_space.end) {
        std::cout << "Heap is full :'(" << std::endl;
        exit(1);
    }
    auto *new_space = reinterpret_cast<ObjectMetadata *>(active_space.free_ptr);
    new_space->tag = tag;
    new_space->size = total_size;
    active_space.free_ptr += total_size;
    void *user_space = new_space + 1;
    return user_space;
}

void GarbageCollector::collect(void *fp) {
    swap_spaces();
    if (fp != nullptr) {
        scan_stack(fp);
    }
    visit_heap(active_space);
    update_finalizers(old_space);
    old_space.free_ptr = old_space.start;
}

void GarbageCollector::swap_spaces() {
    swap(old_space, active_space);
}

Optional<const StackRecord> GarbageCollector::find_record(uint64_t ret_addr) {
    if (parser.getNumFunctions() == 0) {
        return llvm::None;
    }
    uint32_t preceding_records = 0;

    auto fun_record = parser.getFunction(0);

    if (ret_addr < fun_record.getFunctionAddress()) {
        return llvm::None;
    }

    for (auto next_fun_record = ++parser.functions_begin();
         next_fun_record != parser.functions_end(); next_fun_record++) {
        if (ret_addr < next_fun_record->getFunctionAddress()) {
            break;
        }
        preceding_records += fun_record.getRecordCount();
        fun_record = *next_fun_record;
    }

    uint32_t instr_offset = ret_addr - fun_record.getFunctionAddress();

    for (uint64_t i = 0; i < fun_record.getRecordCount(); i++) {
        auto loc_record = parser.getRecord(i + preceding_records);
        if (loc_record.getInstructionOffset() == instr_offset) {
            return loc_record;
        }
    }

    return llvm::None;
}

void GarbageCollector::scan_stack(void *fp) {
    uint64_t ret_addr = static_cast<uint64_t *>(fp)[1];
    auto recordOpt = find_record(ret_addr);
    if (!recordOpt.has_value()) {
        std::cerr << "couldn't find record for current stack position" << std::endl;
        return;
    }
    auto &record = recordOpt.value();
//    std::cerr << "Found record: " << record.getInstructionOffset() << std::endl;
    uint32_t offset = 3 + record.getLocation(2).getSmallConstant();
    uint8_t **caller_sp = static_cast<uint8_t **>(fp) + 2;
    for (uint32_t i = offset; i < record.getNumLocations(); i += 2) {
        auto base_loc = record.getLocation(i);  // Only interested in the derived pointer for now
        auto der_loc = record.getLocation(i + 1);  // Only interested in the derived pointer for now
        assert(base_loc.getDwarfRegNum() == 7);
        assert(der_loc.getDwarfRegNum() == 7);
        uint8_t *&base_ptr = caller_sp[base_loc.getOffset() / sizeof(uint8_t *)];
        uint8_t *&der_ptr = caller_sp[der_loc.getOffset() / sizeof(uint8_t *)];
        uint64_t derived_offset = der_ptr - base_ptr;

//        std::cerr << "\tBase pointer: " << (void *) base_ptr << std::endl;
//        std::cerr << "\tDerived pointer: " << (void *) der_ptr << std::endl;
        relocate(base_ptr);
        der_ptr = base_ptr + derived_offset;
//        std::cerr << "\tBase relocated to: " << (void *) base_ptr << std::endl;
//        std::cerr << "\tDerived relocated to: " << (void *) der_ptr << std::endl;
    }
}

void GarbageCollector::relocate(uint8_t *&pointer) {
    if (old_space.contains(pointer)) {
        auto *metadata = reinterpret_cast<ObjectMetadata *>(pointer) - 1;
        if (metadata->is_forwarded()) {
            std::cerr << "\tAlready relocated " << (void *) pointer << " to " << (void *) metadata->forwarded_pointer
                      << std::endl;
            pointer = metadata->forwarded_pointer;
            return;
        }
        auto *new_pointer = static_cast<uint8_t *>(allocate_no_collect(metadata->size - sizeof(ObjectMetadata),
                                                                       metadata->tag));
        memcpy(new_pointer, pointer, metadata->size - sizeof(ObjectMetadata));
        std::cerr << "\tRelocating " << (void *) pointer << " to " << (void *) new_pointer << std::endl;
        pointer = new_pointer;
    } else {
        std::cerr << "\tNot relocating " << (void *) pointer << std::endl;
    }
}

void GarbageCollector::visit_heap(GcSpace &space) {
    // Cheney algorithm
    uint8_t *scan_ptr = space.start;
    while (scan_ptr < space.free_ptr) {
        auto *metadata = reinterpret_cast<ObjectMetadata *>(scan_ptr);
        scan_ptr += metadata->size;
        switch (metadata->tag) {
            case ARRAY:
            case CLOSURE:
            case REF: {
                auto **pointers = reinterpret_cast<uint8_t **>(metadata + 1);
                for (; reinterpret_cast<uint8_t *>(pointers) < scan_ptr; pointers++) {
                    relocate(*pointers);
                }
                break;
            }
            case RECORD: {
                auto &map = *reinterpret_cast<std::map<std::string, int64_t> *>(metadata + 1);
                for (auto &item: map) {
                    relocate(reinterpret_cast<uint8_t *&>(item.second));
                }
            }
                break;
            case STRING:
                break;
            case FORWARDED_FLAG:
            default:
                assert(false);
        }
    }
}

void GarbageCollector::register_finalizer(void *object, FinalizerEntry::FinalizerFunction const &finalizer) {
    auto *metadata = static_cast<ObjectMetadata *>(object) - 1;
    finalizers.emplace_back(metadata, finalizer);
}

void GarbageCollector::update_finalizers(GcSpace &oldSpace) {
    auto iter = finalizers.begin();
    auto end = finalizers.end();
    while (iter != end) {
        auto &finalizer = *iter;
        if (finalizer.object_metadata->is_forwarded()) {
            std::cerr << "Updating reference from " << (void *) finalizer.object_metadata << " to "
                      << (void *) finalizer.object_metadata->forwarded_pointer << std::endl;
            finalizer.object_metadata = reinterpret_cast<ObjectMetadata *>(finalizer.object_metadata->forwarded_pointer);
        }
        if (finalizer.is_in_space(oldSpace)) {
            std::cerr << "Object abandoned in old space, finalizing" << std::endl;
            void *object = static_cast<void *>(finalizer.object_metadata + 1);
            finalizer.finalizer(object);
            iter = finalizers.erase(iter);
            end = finalizers.end();
        } else {
            iter++;
        }
    }
}

GcSpace::GcSpace(size_t size) {
    start = free_ptr = new uint8_t[size];
    end = start + size;
    std::cerr << "Creating new gc space: " << (void *) start << " to " << (void *) end << std::endl;
}

GcSpace::GcSpace(GcSpace &&space) noexcept {
    std::cerr << "Moving gc space to other variable" << std::endl;
    start = std::exchange(space.start, nullptr);
    end = std::exchange(space.end, nullptr);
    free_ptr = std::exchange(space.free_ptr, nullptr);
}

GcSpace::~GcSpace() {
    std::cerr << "Deleting gc space: " << (void *) start << std::endl;
    delete[] start;
}

GcSpace &GcSpace::operator=(GcSpace &&other) noexcept {
    // Copy and swap: https://stackoverflow.com/questions/3279543/what-is-the-copy-and-swap-idiom
    swap(*this, other);
    return *this;
}

void swap(GcSpace &one, GcSpace &other) {
    std::swap(one.start, other.start);
    std::swap(one.end, other.end);
    std::swap(one.free_ptr, other.free_ptr);
}

GarbageCollector garbageCollector = GarbageCollector(256);