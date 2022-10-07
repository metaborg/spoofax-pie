cmake_minimum_required(VERSION 3.16)

project(tim_runtime)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_COMPILER clang)
set(CMAKE_CXX_COMPILER clang++)
set(CMAKE_C_LINK_EXECUTABLE ld)
set(CMAKE_CXX_LINK_EXECUTABLE ld)
set(CMAKE_AR llvm-ar)
set(CMAKE_RANLIB llvm-ranlib)

add_library(tim_runtime STATIC src/record.cpp include/record.h src/gc.cpp include/gc.h src/array.cpp include/array.h src/string.cpp include/string.h src/GarbageCollector.cpp src/GarbageCollector.h)

if (${CMAKE_BUILD_TYPE} STREQUAL Debug)
    target_compile_options(tim_runtime PUBLIC -gdwarf-4)
endif ()

target_compile_options(tim_runtime PUBLIC -fPIC -fno-omit-frame-pointer -O3 -iquote ${CMAKE_SOURCE_DIR}/include -flto)
target_link_options(tim_runtime PUBLIC -flto)