mrapp
=====

Mobile Reporter App

## Setting up Development

**Prerequisites:**

* [Android SDK](https://developer.android.com/sdk/installing/index.html)
* [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html)

Follow these steps to setup your dev environment:

1. Checkout mrapp git repo
2. Checkout [android-ffmpeg](https://github.com/guardianproject/android-ffmpeg)
   and place it in the same directory as your mrapp checkout.
3. Download ActionBarSherlock and extract it to the same directory as your
   mrapp checkout.
4. Your directory layout should look like this:

    ```
    workspace/
     |
     - ActionBarSherlock.
     |     |
     |     - library/
     |     - ..other stuff..
     - android-ffmpeg/
     - mrapp/
           |
           - app/

5. Checkout the `library-dev` branch in android-ffmpeg: `git checkout library-dev`
6. Follow the setup instructions in `android-ffmpeg/README.txt`
7. Import `ActionBarSherlock/library`, `mrapp/app`, and `android-ffmpeg/library` into
   eclipse as existing projects.
