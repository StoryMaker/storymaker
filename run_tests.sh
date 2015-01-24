#/bin/bash

set -e

# Assemble Storymaker .apk
./gradlew assembleDebug

# Assemble Storymaker test .apk
./gradlew assembleDebugTest

# Run tests with Spoon
java -jar spoon-runner-1.1.2-jar-with-dependencies.jar --apk ./app/build/outputs/apk/app-debug.apk --test-apk ./app/build/outputs/apk/app-debug-test-unaligned.apk --class-name org.storymaker.app.MainTest