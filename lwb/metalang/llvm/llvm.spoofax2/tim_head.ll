target triple = "x86_64-unknown-linux-gnu"
@.int_fmt = private unnamed_addr constant [5 x i8] c"%ld\0A\00"
@.str_fmt = private unnamed_addr constant [4 x i8] c"%s\0A\00"

declare ptr @malloc(i64) nounwind
declare void @free(ptr) nounwind
declare i32 @puts(ptr nocapture) nounwind
declare void @exit(i32) noreturn nounwind
declare i32 @printf(ptr nocapture, ...) nounwind
declare i64 @strlen(ptr) nounwind

; tim runtime library
declare i64 @record_new(i64, ...) nounwind
declare i64 @record_write(i64, i64, i64) nounwind
declare i64 @record_read(i64, i64) nounwind
declare void @record_delete(i64) nounwind

declare i64 @array_concat(i64, i64) nounwind
declare i64 @array_tail(i64) nounwind

declare i64 @string_concat(i64, i64) nounwind
declare i64 @string_index(i64, i64) nounwind

declare i64 @int_to_string(i64) nounwind

declare void @gc_init( ) nounwind
declare ptr @gc_alloc(i64) nounwind inaccessiblememonly allockind("alloc,uninitialized") allocsize(0) "alloc-family"="malloc"
declare void @gc_collect( ) nounwind

declare void @gc_init() nounwind
declare ptr @gc_alloc(i64) nounwind inaccessiblememonly allockind("alloc,uninitialized") allocsize(0) "alloc-family"="malloc"
declare void @gc_collect() nounwind

define i32 @main() {
  call tailcc void @start()
  ret i32 0
}