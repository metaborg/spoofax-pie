#include <cstdlib>
#include <functional>
#include <iostream>
#include <llvm/Object/StackMapParser.h>
#include "gc.h"

extern uint8_t __attribute__((section(".llvm_stackmaps"))) __LLVM_StackMaps[];

using gc_visitor = std::function<void(void *, void *)>;
using llvm::StackMapParser;
using llvm::ArrayRef;
using llvm::Optional;
using StackRecord = StackMapParser<llvm::support::native>::RecordAccessor;

static void gc_scan_stack(void *fp);

void gc_init() {
    // NOOP
}

void *gc_alloc(uint64_t size) {
//    auto *fp = get_frame_pointer();
    void *fp;
    asm ("movq %%rbp, %0" : "=r"(fp));
    std::cerr << "GC_ALLOC" << std::endl;
    return gc_alloc_fp(size, fp);
}

void *gc_alloc_fp(uint64_t size, void *fp) {

//    std::cerr << "FP: " << fp << std::endl;
    gc_scan_stack(fp);
    void *memory = malloc(size);
    std::cerr << "Allocated: " << memory << " size: " << size << std::endl;
    return memory;
}

void gc_collect() {
    gc_scan_stack(get_frame_pointer());
}

static Optional<const StackRecord> find_record_in_stackmap(uint64_t ret_addr) {
    static auto stackMapParser = StackMapParser<llvm::support::native>(ArrayRef<uint8_t>(__LLVM_StackMaps, SIZE_MAX));
//    std::cerr << "Stackmap exists: " << &__LLVM_StackMaps << std::endl
//              << "Ver: " << int(stackMapParser.getVersion()) << std::endl
//              << "Ret addr: " << (void *) ret_addr << std::endl
//              << "Functions: " << stackMapParser.getNumFunctions() << std::endl;
    if (stackMapParser.getNumFunctions() == 0) {
        return llvm::None;
    }
    uint32_t preceding_records = 0;

    auto fun_record = stackMapParser.getFunction(0);

    if (ret_addr < fun_record.getFunctionAddress()) {
        return llvm::None;
    }

    for (auto next_fun_record = ++stackMapParser.functions_begin();
         next_fun_record != stackMapParser.functions_end(); next_fun_record++) {
//        std::cerr << "Fun addr: " << (void *) (next_fun_record->getFunctionAddress()) << std::endl;
        if (ret_addr < next_fun_record->getFunctionAddress()) {
            break;
        }
        preceding_records += fun_record.getRecordCount();
        fun_record = *next_fun_record;
    }

//    std::cerr << "Skipping " << preceding_records << " records" << std::endl;

    uint32_t instr_offset = ret_addr - fun_record.getFunctionAddress();

    for (uint64_t i = 0; i < fun_record.getRecordCount(); i++) {
        auto loc_record = stackMapParser.getRecord(i + preceding_records);
//        std::cerr << "Comparing " << (void *) instr_offset << " == " << (void *) loc_record.getInstructionOffset()
//                  << std::endl;
        if (loc_record.getInstructionOffset() == instr_offset) {
            return loc_record;
        }
    }

    return llvm::None;
}

void gc_visitor_impl(void *ptr) {
    std::cerr << "In use: " << ptr << std::endl;
}

static void gc_scan_stack(void *fp) {
    uint64_t ret_addr = static_cast<uint64_t *>(fp)[1];
    auto recordOpt = find_record_in_stackmap(ret_addr);
    if (!recordOpt.has_value()) {
        std::cerr << "couldn't find record for current stack position" << std::endl;
        return;
    }
    auto &record = recordOpt.value();
//    std::cerr << "Found record: " << record.getInstructionOffset() << std::endl;
    uint32_t offset = 3 + record.getLocation(2).getSmallConstant();
    void **caller_sp = static_cast<void **>(fp) + 2;
    for (uint32_t i = offset; i < record.getNumLocations(); i += 2) {
        auto base_loc = record.getLocation(i);  // Only interested in the derived pointer for now
        auto der_loc = record.getLocation(i + 1);  // Only interested in the derived pointer for now
        assert(base_loc.getDwarfRegNum() == 7);
        assert(der_loc.getDwarfRegNum() == 7);
        void *base_ptr = caller_sp[base_loc.getOffset() / sizeof(void*)];
        void *der_ptr = caller_sp[der_loc.getOffset() / sizeof(void*)];
        std::cerr << "\tBase pointer: " << base_ptr << std::endl;
        std::cerr << "\tDerived pointer: " << der_ptr << std::endl;
    }
}