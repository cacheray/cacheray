cmake_minimum_required (VERSION 3.5)

project(grand)

set(SOURCES
        grand.c)

add_executable(${PROJECT_NAME} ${SOURCES})

target_link_libraries(${PROJECT_NAME}
        cacheray)

target_compile_options(${PROJECT_NAME}
        PRIVATE
        $<$<COMPILE_LANGUAGE:C>:${C_FLAGS}>
        $<$<COMPILE_LANGUAGE:ASM>:${ASM_FLAGS}>)
