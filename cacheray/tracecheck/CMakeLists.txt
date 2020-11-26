cmake_minimum_required (VERSION 3.5)

project(tracecheck)

add_executable(${PROJECT_NAME} tracecheck.c)

target_include_directories(${PROJECT_NAME}
        PUBLIC ../include)

target_link_libraries(${PROJECT_NAME}
        cacheray)