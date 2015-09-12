#!/usr/bin/python

# from:  http://developer.android.com/tools/publishing/app-signing.html

import os
import xml.etree.ElementTree as ET

build_tools_version = "21.1.2"

tree = ET.parse('app/AndroidManifest.xml')
root = tree.getroot()
version = root.attrib['{http://schemas.android.com/apk/res/android}versionName']

print("building app version {0}".format(version))

os.system("./gradlew assembleDebug")
os.system("cp app/build/outputs/apk/app-mainSqlCipher-debug.apk storymaker-debug-{0}.apk".format(version))

os.system("./gradlew assembleRelease")
os.system("jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore storymaker-release.key app/build/outputs/apk/app-mainSqlCipher-release-unsigned.apk release")
os.system("jarsigner -verify -verbose -certs app/build/outputs/apk/app-mainSqlCipher-release-unsigned.apk")
os.system("/home/josh/Android/Sdk/build-tools/{0}/zipalign -v 4 app/build/outputs/apk/app-mainSqlCipher-release-unsigned.apk storymaker-release-{1}.apk".format(build_tools_version, version))

os.system("./gradlew assembleDebuggableRelease")
os.system("jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore storymaker-release.key app/build/outputs/apk/app-mainSqlCipher-debuggableRelease-unsigned.apk release")
os.system("jarsigner -verify -verbose -certs app/build/outputs/apk/app-mainSqlCipher-debuggableRelease-unsigned.apk")
os.system("/home/josh/Android/Sdk/build-tools/{0}/zipalign -v 4 app/build/outputs/apk/app-mainSqlCipher-debuggableRelease-unsigned.apk storymaker-debuggable-release-{1}.apk".format(build_tools_version, version))

