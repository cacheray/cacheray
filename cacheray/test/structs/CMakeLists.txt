cmake_minimum_required (VERSION 3.5)

project(structs)

set(SOURCES
        main.c)

add_executable(${PROJECT_NAME} ${SOURCES})

target_include_directories(${PROJECT_NAME}
        PUBLIC ${PROJECT_SOURCE_DIR}/include)

target_link_libraries(${PROJECT_NAME}
        cacheray)

target_compile_options(${PROJECT_NAME}
        PRIVATE
        $<$<COMPILE_LANGUAGE:C>:${C_FLAGS}>
        $<$<COMPILE_LANGUAGE:ASM>:${ASM_FLAGS}>)

