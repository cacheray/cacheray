cmake_minimum_required(VERSION 3.5)

project(cacheray)

message("LLVM_DIR:" $ENV{LLVM_DIR})
if(NOT DEFINED ENV{LLVM_DIR})
    message(SEND_MESSAGE "LLVM_DIR is not defined. Please add the LLVM_DIR environment variable.")
endif()

message("LLVM_BUILD_DIR: " $ENV{LLVM_BUILD_DIR})
if(NOT DEFINED ENV{LLVM_BUILD_DIR})
    message(SEND_MESSAGE "LLVM_BUILD_DIR is not defined. Please define it.")
endif()

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

set(CMAKE_C_STANDARD 11)

add_library(cacheray STATIC
        src/cacheray.c
        src/cacheray-utils.c )

#add_definitions(-DDEBUG)

target_include_directories(cacheray
        PUBLIC
        ${PROJECT_SOURCE_DIR}/include)

if(DEFINED ENV{LLVM_DIR} AND DEFINED ENV{LLVM_BUILD_DIR})
    add_subdirectory(test)
endif()

add_subdirectory(tracecheck)



