#include <iostream>
#include "GarbageCollector.h"

extern uint8_t __LLVM_StackMaps[]; // NOLINT(bugprone-reserved-identifier)

GarbageCollector::GarbageCollector(size_t mem_size) :
        parser(StackMapParser(llvm::ArrayRef<uint8_t>(__LLVM_StackMaps, SIZE_MAX))),
        old_space(mem_size), active_space(mem_size) {
}

GarbageCollector::~GarbageCollector() {
//    collect();
    assert(old_space.free_ptr == old_space.start);
}

void *GarbageCollector::allocate(size_t size, void *fp) {
    collect(fp);
    return allocate_no_collect(size);
}

void *GarbageCollector::allocate_no_collect(size_t size) {
    size_t total_size = size + sizeof(ObjectMetadata);  // Type tag and size
    if (active_space.free_ptr + total_size > active_space.end) {
        std::cout << "Heap is full :'(" << std::endl;
        exit(1);
    }
    auto *new_space = reinterpret_cast<ObjectMetadata *>(active_space.free_ptr);
    new_space->tag = 0x01;
    new_space->size = size;
    active_space.free_ptr += total_size;
    void *user_space = new_space + 1;
    return user_space;
}

void GarbageCollector::collect(void *fp) {
    swap_spaces();
    scan_stack(fp, [this](uint8_t *&ptr) { visitor(ptr); });
    old_space.free_ptr = old_space.start;
}

void GarbageCollector::swap_spaces() {
    old_space.swap(active_space);
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

void GarbageCollector::scan_stack(void *fp, const GcVisitor &visitor) {
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
        visitor(base_ptr);
        der_ptr = base_ptr + derived_offset;
//        std::cerr << "\tBase relocated to: " << (void *) base_ptr << std::endl;
//        std::cerr << "\tDerived relocated to: " << (void *) der_ptr << std::endl;
    }
}

void GarbageCollector::visitor(uint8_t *&pointer) {
    if (old_space.contains(pointer)) {
        auto *metadata = reinterpret_cast<ObjectMetadata *>(pointer) - 1;
        auto *new_pointer = static_cast<uint8_t *>(allocate_no_collect(metadata->size));
        memcpy(new_pointer, pointer, metadata->size);
        std::cerr << "\tRelocating " << (void *) pointer << " to " << (void *) new_pointer << std::endl;
        pointer = new_pointer;
    } else {
        std::cerr << "\tNot relocating " << (void *) pointer << std::endl;
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
    swap(other);
    return *this;
}

void GcSpace::swap(GcSpace &other) {
    std::swap(start, other.start);
    std::swap(end, other.end);
    std::swap(free_ptr, other.free_ptr);
}

GarbageCollector garbageCollector = GarbageCollector(256);