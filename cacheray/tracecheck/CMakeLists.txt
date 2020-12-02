cmake_minimum_required (VERSION 3.5)

project(cacheray-trace-check)

add_executable(${PROJECT_NAME} cacheray-trace-check.c)

target_include_directories(${PROJECT_NAME}
        PUBLIC ../include)

target_link_libraries(${PROJECT_NAME}
        cacheray)
