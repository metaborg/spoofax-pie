cmake_minimum_required(VERSION 3.16)

project(tim_runtime)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_COMPILER clang)
set(CMAKE_CXX_COMPILER clang++)

add_library(tim_runtime STATIC src/record.cpp include/record.h src/gc.cpp include/gc.h)
target_include_directories(tim_runtime PUBLIC include)
#target_compile_options(tim_runtime PUBLIC -S -emit-llvm)
#target_link_options(tim_runtime PUBLIC -flto)