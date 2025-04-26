cmake_minimum_required(VERSION 3.22.1)

project(vw_jni)

set(CMAKE_VERBOSE_MAKEFILE ON)

add_library(
        android_ffi
        SHARED
        IMPORTED)

set_target_properties(
        android_ffi
        PROPERTIES IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libandroid_ffi.so")

