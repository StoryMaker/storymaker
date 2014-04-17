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
external/SecureShareLib/SecureShareUILibrary
external/SecureShareLib/SecureShareUILibrary/external/facebook-sdk/facebook/
END


for project in "${MAPFILE[@]}"; do
        android update project --path $project -t "android-17"
done

sh scripts/copy-support-libs.sh

cd app
android update project --path .


