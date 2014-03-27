Story Maker - Make your story great.
=====

## [StoryMaker.cc](http://storymaker.cc/)

Download [StoryMaker - Video, Audio & Photo](https://play.google.com/store/apps/details?id=info.guardianproject.mrapp) in the the Google Play Store. 

## Setting up Development Environment

**Prerequisites:**
. 
* [Android SDK](https://developer.android.com/sdk/installing/index.html)
* Working [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html) toolchain
* Unix build toolchain.  Linux and OS X are well tested.

Follow these steps to setup your dev environment:

1. Clone the git repo, make sure you fork first if you intend to commit

1. Init and update git submodules

    git submodule update --init --recursive

1. Ensure `NDK_BASE` env variable is set to the location of your NDK, example:

    export NDK_BASE=/path/to/android-ndk

1. Build android-ffmpeg

    cd external/android-ffmpeg-java/external/android-ffmpeg/
    ./configure_make_everything.sh

1. run script to update all libraries android support library.  from command line run in the top level of the repo:

        $ scripts/copy-support-libs.sh

1. Import dependancies into Eclipse

    Using the File->Import->Android->"Existing Android Code Into Workspace" option, import these projects in this order:

        external/HoloEverywhere/contrib/ActionBarSherlock/library/
        external/HoloEverywhere/library/
        external/OnionKit/
        external/android-ffmpeg-java/
        external/WordpressJavaAndroid/
        external/cardsui-for-android/CardsUILib
        external/SlidingMenu/library
        external/Android-ViewPagerIndicator/library
        external/RangeSeekBar/library
        external/SecureShareLib/SecureShareUILibrary/

1. Import the StoryMaker project from the app/ subfolder

1. (optional) Build from the command line

        $ cd app/
        $ ./setup-ant.sh
        $ ant clean debug

