Story Maker
=====

## Setting up Development

**Prerequisites:**

* [Android SDK](https://developer.android.com/sdk/installing/index.html)
* [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html)

Follow these steps to setup your dev environment:

1. Checkout mrapp git repo
2. Checkout [android-ffmpeg-java](https://github.com/guardianproject/android-ffmpeg-java)
3. Download ActionBarSherlock and extract it.
4. Your directory layout should look like this:

    ```
    workspace/
     |
     - ActionBarSherlock.
     |     |
     |     - library/
     |     - ..other stuff..
     - android-ffmpeg-java/
     - mrapp/
           |
           - app/

5. Follow the build instructions in `android-ffmpeg-java/README.md`
7. Import `ActionBarSherlock/library`, `mrapp/app`, and `android-ffmpeg-java` into
   eclipse as existing projects.
