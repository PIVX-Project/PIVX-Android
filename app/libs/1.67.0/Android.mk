# WARNING!!! THIS IS AUTO-GENERATED FILE!!! DO NOT EDIT IT MANUALLY!!!


LOCAL_PATH := $(call my-dir)

ifeq (,$(filter gnustl_% c++_%,$(APP_STL)))
$(error $(strip \
    We do not support APP_STL '$(APP_STL)' for Boost libraries! \
    Please use either "gnustl_shared", "gnustl_static", "c++_shared" or "c++_static". \
))
endif

__boost_libstdcxx_subdir := $(strip \
    $(strip $(if $(filter c++_%,$(APP_STL)),\
        llvm,\
        gnu\
    ))-$(strip $(if $(filter c++_%,$(APP_STL)),\
        $(if $(filter clang%,$(NDK_TOOLCHAIN_VERSION)),$(patsubst clang%,%,$(NDK_TOOLCHAIN_VERSION)),3.6),\
        $(if $(filter clang%,$(NDK_TOOLCHAIN_VERSION)),4.9,$(NDK_TOOLCHAIN_VERSION))\
    ))\
)
