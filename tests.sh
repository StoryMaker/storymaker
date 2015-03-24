adb uninstall org.storymaker.app ; java -jar spoon-runner-1.1.2-jar-with-dependencies.jar --apk app/build/outputs/apk/app-mainSqlite-debug.apk --test-apk app/build/outputs/apk/app-mainSqlite-debug-test-unaligned.apk --sdk ~/Android/Sdk/ --class-name org.storymaker.app.tests.EulaTest

adb uninstall org.storymaker.app ; java -jar spoon-runner-1.1.2-jar-with-dependencies.jar --apk app/build/outputs/apk/app-mainSqlite-debug.apk --test-apk app/build/outputs/apk/app-mainSqlite-debug-test-unaligned.apk --sdk ~/Android/Sdk/ --class-name org.storymaker.app.tests.CompleteTest
