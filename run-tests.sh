#/bin/bash

set -e

adb uninstall org.storymaker.app
adb uninstall org.storymaker.app.test

echo "checkout 'testing' branch in external/liger"
#cd external/liger
#git checkout testing
#cd ../..

echo "copy local gitignored test.xml to app/res/values/test.xml"
#cp test.xml app/res/values/test.xml

echo "Assemble Storymaker Sqlite .apk"
./gradlew assembleMainSqliteDebug

echo "Assemble Storymaker Sqlite test .apk"
./gradlew assembleMainSqliteDebugTest

echo "Run tests with Spoon"
java -jar spoon-runner-1.1.2-jar-with-dependencies.jar --sdk ~/Android/Sdk/ --apk ./app/build/outputs/apk/app-mainSqlite-debug.apk --test-apk ./app/build/outputs/apk/app-mainSqlite-debug-test-unaligned.apk --class-name org.storymaker.app.tests.MinimumTest

adb uninstall org.storymaker.app
adb uninstall org.storymaker.app.test
