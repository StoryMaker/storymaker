#!/bin/bash

if ! type -P android &> /dev/null; then
    echo "Error: 'android' utility is not in your path."
    echo "  Did you forget to setup the SDK?"
    exit 1
fi
readarray <<END
external/HoloEverywhere/contrib/ActionBarSherlock/library
external/HoloEverywhere/library
external/android-ffmpeg-java
external/NetCipher/libnetcipher
external/WordpressJavaAndroid
external/RangeSeekBar/library
external/Android-ViewPagerIndicator/library
external/cardsui-for-android/CardsUILib
external/SlidingMenu/library
END


for project in "${MAPFILE[@]}"; do
        android update project --path $project -t "android-19"
done

cp app/libs/android-support-v4.jar external/SlidingMenu/library/libs/android-support-v4.jar
cp app/libs/android-support-v4.jar external/HoloEverywhere/contrib/ActionBarSherlock/library/libs/android-support-v4.jar
cp app/libs/android-support-v4.jar external/Android-ViewPagerIndicator/library/libs/android-support-v4.jar
cp app/libs/android-support-v4.jar external/RangeSeekBar/library/libs/android-support-v4.jar


cd app
android update project --path .


