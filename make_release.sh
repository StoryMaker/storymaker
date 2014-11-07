#!/bin/sh

# from:  http://developer.android.com/tools/publishing/app-signing.html

./gradlew assembleRelease

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore storymaker-release.key app/build/outputs/apk/app-release-unsigned.apk release

jarsigner -verify -verbose -certs app/build/outputs/apk/app-release-unsigned.apk

/home/josh/android-sdks/build-tools/21.0.0/zipalign -v 4 app/build/outputs/apk/app-release-unsigned.apk storymaker-release.apk
