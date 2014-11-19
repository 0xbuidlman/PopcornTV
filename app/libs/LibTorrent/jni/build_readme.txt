	--- arm ---
1. В файле Application.mk:
	1.1. APP_ABI := armeabi armeabi-v7a
2. В файле Android.mk:
	2.1. для модуля libboost_filesystem-gcc-mt-1_53 пишем LOCAL_SRC_FILES := boost/android/lib-arm/libboost_filesystem-gcc-mt-1_53.a
	2.1. для модуля libboost_system-gcc-mt-1_53 пишем LOCAL_SRC_FILES := boost/android/lib-arm/libboost_system-gcc-mt-1_53.a
	
	--- x86 ---
1. В файле Application.mk:
	1.1. APP_ABI := x86
2. В файле Android.mk:
	2.1. для модуля libboost_filesystem-gcc-mt-1_53 пишем LOCAL_SRC_FILES := boost/android/lib-x86/libboost_filesystem-gcc-mt-1_53.a
	2.1. для модуля libboost_system-gcc-mt-1_53 пишем LOCAL_SRC_FILES := boost/android/lib-x86/libboost_system-gcc-mt-1_53.a
	
Запускаем ndk-build 

(ndk-build -j9 для использования всех ядер)