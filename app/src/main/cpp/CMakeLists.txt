cmake_minimum_required(VERSION 3.22.1)
project(SonixNativeBrain LANGUAGES CXX)

add_library(sonix_native SHARED native_ai.cpp)
target_compile_features(sonix_native PRIVATE cxx_std_17)
target_compile_options(sonix_native PRIVATE -Wall -Wextra)

find_library(log-lib log)
target_link_libraries(sonix_native ${log-lib})
