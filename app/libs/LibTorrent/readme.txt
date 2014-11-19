1. install android-ndk-r9d
2. overwrite /android-ndk-r9d/build/core/default-build-commands.mk with supplied one
3. type /android-ndk-r9d/ndk-build.cmd         to produce libs/armeabi/libtorrent.so

patched files to make this work:

1. android-ndk-r9d/build/core/default-build-commands.mk
2. jni/libtorrent/config.hpp
3. jni/libtorrent/file.cpp

