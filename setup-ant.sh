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
external/OnionKit/libonionkit
external/WordpressJavaAndroid
external/RangeSeekBar/library
external/Android-ViewPagerIndicator/library
external/cardsui-for-android/CardsUILib
external/SlidingMenu/library
END


for project in "${MAPFILE[@]}"; do
        android update project --path $project -t 1
done

cd app
android update project --path .


