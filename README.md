PopcornTV
=========
![image](https://raw.githubusercontent.com/pepibumur/PopcornTV/master/assets/popcorntime_icon.png)

PopcornTV is an Android TV application for torrent streaming (based on Popcorn Time)

### How to setup the project locally

1. Ensure you have the Android NDK and SDK installed on your computer ([steps](https://gist.github.com/pepibumur/f17fcabe266546949f87)). 
2. You need the Android SDK 21.1 installed.
2. Git clone the repo in your system, `https://github.com/pepibumur/libvlc-android.git`
3. Import the project in Android Studio.
4. Enjoy!

### Build LibVLC

PopcornTV relies on LibVLC so it's important to know how to build it in order to keep it updated with the library. LibVLC is developed in C++ so it needs Android-NDK to compile it. The steps you should follow are detailed [here](https://wiki.videolan.org/AndroidCompile) but basically you should:

```bash
## Set an environment var with the device type
export ANDROID_ABI=armeabi-v7a # For normal, like Corted-A8 or Cortex-A9
export ANDROID_ABI=armeabi-v7a # For Tegra2 device
export ANDROID_ABI=armeabi # For ARMv6 device
export ANDROID_ABI=armeabi \ export NO_FPU=1 # For ARMv6 device without FPU
export ANDROID_ABI=armeabi \ export NO_ARMV6=1 # For an ARMv5 device or the emulator
export ANDROID_ABI=x86 \ # For a x86 device (e.g. Android-x86, Google TV, Logitech TV, etc)
export ANDROID_ABI=mips # For a MIPS device (i.e. Ainol Novo tablets)

## Get VLC Source
git clone git://git.videolan.org/vlc-ports/android.git

## Compile
sh compile.sh / sh compile.sh release # For non debug package
# Note: The previous step takes a lot of time
make .sdk

## Generate the .jar with the .so files
cp vlc-sdk/libs vlc-sdk/lib
zip -r libvlc.jar vlc-sdk/lib

## Copy the .jar file into the project /libs folder
## Copy the vlc-sdk/src into the project src folder
```



### Libraries

- **LibTorrent**: https://github.com/steeve/libtorrent
- **LibVLC**: https://wiki.videolan.org/LibVLC/


### Thanks
- Thanks to the [**Popcorn Time**](http://popcorn-time.se/) project which has inspired the development of this app for Android TV
- Thanks to Roberto Pacheco for the [**Pocorn Time Icon**](https://dribbble.com/shots/1526730-Popcorn-Time-Desktop-icon)
