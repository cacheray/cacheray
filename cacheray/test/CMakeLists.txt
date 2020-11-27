cmake_minimum_required (VERSION 3.5)

project(tests)

add_definitions(-fsanitize=thread -O0 -g)

add_subdirectory(cooperative)
add_subdirectory(structs)
add_subdirectory(grand)
