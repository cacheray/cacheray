cmake_minimum_required (VERSION 3.5)

project(tests)
#set(C_FLAGS -fsanitize=thread -O0 -g)
set(ASM_FLAGS "") # Could be used for some linker specific flags    

add_definitions(
    -Xclang -load
    -Xclang $ENV{LLVM_BUILD_DIR}/lib/LLVMMallocTracker.so
    )

add_definitions(-fsanitize=thread -O0 -g)

add_subdirectory(cooperative)
add_subdirectory(structs)
add_subdirectory(grand)
