target triple = "x86_64-unknown-linux-gnu"
@.int_fmt = private unnamed_addr constant [5 x i8] c"%ld\0A\00"
@.str_fmt = private unnamed_addr constant [4 x i8] c"%s\0A\00"
module asm ".globl __LLVM_StackMaps"

declare void @exit(i32) noreturn nounwind
declare i32 @printf(ptr nocapture, ...) nounwind
declare i64 @strlen(ptr) nounwind
declare i64 @strcmp(ptr, ptr) nounwind

; tim runtime library
declare ptr addrspace(1) @record_new(i64, ...) nounwind
declare void @record_write(ptr addrspace(1), ptr addrspace(1), i64) nounwind
declare i64 @record_read(ptr addrspace(1), ptr addrspace(1)) nounwind
declare void @record_write_ptr(ptr addrspace(1), ptr addrspace(1), ptr addrspace(1)) nounwind
declare ptr addrspace(1) @record_read_ptr(ptr addrspace(1), ptr addrspace(1)) nounwind
declare void @record_delete(ptr addrspace(1)) nounwind

declare ptr addrspace(1) @array_concat(ptr addrspace(1), ptr addrspace(1)) nounwind
declare ptr addrspace(1) @array_tail(ptr addrspace(1)) nounwind

declare ptr addrspace(1) @string_concat(ptr addrspace(1), ptr addrspace(1)) nounwind
declare ptr addrspace(1) @string_index(ptr addrspace(1), i64) nounwind

declare ptr addrspace(1) @int_to_string(i64) nounwind

declare void @gc_init( ) nounwind
declare ptr addrspace(1) @gc_alloc(i64) nounwind inaccessiblememonly allockind("alloc,uninitialized") allocsize(0) "alloc-family"="malloc"
declare void @gc_collect( ) nounwind

define i32 @main() {
  call tailcc void @start()
  ret i32 0
}