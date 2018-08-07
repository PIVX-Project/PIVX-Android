# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


#-----------------------------------------
add_library (boost_system SHARED IMPORTED)

set_target_properties( boost_system PROPERTIES IMPORTED_LOCATION
                    ${MY_BOOST_LIBS_DIR}/${ANDROID_ABI}/libboost_system.so
                    )

add_library (boost_thread SHARED IMPORTED)

set_target_properties( boost_thread PROPERTIES IMPORTED_LOCATION
                    ${MY_BOOST_LIBS_DIR}/${ANDROID_ABI}/libboost_thread.so
                    )

#-----------------------------------------

include_directories( ${MY_BOOST_INC_DIR}
                    )

#-----------------------------------------


add_library (crypto SHARED IMPORTED)

set_target_properties( crypto PROPERTIES IMPORTED_LOCATION
                    ${MY_OPENSSL_CRYPTO_LIBS_DIR}/${ANDROID_ABI}/libcrypto.so
                    )

#-----------------------------------------

include_directories( ${MY_OPENSSL_CRYPTO_INC_DIR}
                    )

#-----------------------------------------

include_directories(src/main/cpp/)

file(GLOB SOURCES "src/*.h")

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp

             ${SOURCES}
             )

file(GLOB native-lib_SRC
    "*.h"
    "*.cpp"
)

target_include_directories(
        native-lib PRIVATE
        src/main/cpp/
)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}

                       boost_system
                       boost_thread

                       crypto
                       )