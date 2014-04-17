#!/bin/bash

echo "updating SlidingMenu" 
cp app/libs/android-support-v4.jar external/SlidingMenu/library/libs/android-support-v4.jar

echo "updating HoloEverywhere" 
mkdir -p external/HoloEverywhere/library/libs/
cp app/libs/android-support-v4.jar external/HoloEverywhere/library/libs/android-support-v4.jar

echo "updating HoloEverywhere/contrib/ActionBarSherlock" 
cp app/libs/android-support-v4.jar external/HoloEverywhere/contrib/ActionBarSherlock/library/libs/android-support-v4.jar

echo "updating Android-ViewPagerIndicator" 
cp app/libs/android-support-v4.jar external/Android-ViewPagerIndicator/library/libs/android-support-v4.jar

echo "updating RangeSeekBar" 
cp app/libs/android-support-v4.jar external/RangeSeekBar/library/libs/android-support-v4.jar

echo "updating SecureShareLib" 
cp app/libs/android-support-v4.jar external/SecureShareLib/SecureShareUILibrary/libs/android-support-v4.jar

echo "updating Facebook SDK" 
cp app/libs/android-support-v4.jar external/SecureShareLib/SecureShareUILibrary/external/facebook-sdk/facebook/libs/android-support-v4.jar
