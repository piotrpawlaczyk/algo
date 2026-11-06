cmake_minimum_required(VERSION 3.14)
project(rekrutacja_backend CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Threads REQUIRED)

add_executable(server main.cpp server.cpp)
target_include_directories(server PRIVATE third_party)
target_link_libraries(server PRIVATE Threads::Threads)
if(WIN32)
  target_compile_definitions(server PRIVATE _WIN32_WINNT=0x0A00)
  target_link_libraries(server PRIVATE ws2_32)
endif()
if(MSVC)
  target_compile_options(server PRIVATE /bigobj)
endif()
