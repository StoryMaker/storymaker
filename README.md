Story Maker
=====

## Setting up Development Environment

**Prerequisites:**

* [Android SDK](https://developer.android.com/sdk/installing/index.html)
* Working [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html) toolchain

Follow these steps to setup your dev environment:

1. Checkout mrapp git repo
2. Init and update git submodules

    git submodule update --init --recursive

3. Ensure `NDK_BASE` env variable is set to the location of your NDK, example:

    export NDK_BASE=/path/to/android-ndk

4. Build android-ffmpeg

    cd external/android-ffmpeg-java/external/android-ffmpeg/
    ./configure_make_everything.sh

7. **Using Eclipse**

    Import into Eclipse (using the "existing projects" option) the projects in this order:

        external/HoloEverywhere/contrib/ActionBarSherlock/library/
        external/HoloEverywhere/library/
        external/OnionKit/
        external/android-ffmpeg-java/
        external/WordpressJavaAndroid/
        external/SecureShareLib/SecureShareUILibrary/


   **Using command line**

        cd app/
        ./setup-ant.sh
        ant clean debug
