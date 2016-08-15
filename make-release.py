#!/usr/bin/python

# from:  http://developer.android.com/tools/publishing/app-signing.html

import os
import xml.etree.ElementTree as ET

build_tools_version = "21.1.2"

def run(cmd):
    print("$ {0}".format(cmd))
    ret = os.system(cmd)
    if ret is not 0:
        print("!!! command failed: {0}".format(ret))
        exit()

tree = ET.parse('app/AndroidManifest.xml')
root = tree.getroot()
version_code = root.attrib['{http://schemas.android.com/apk/res/android}versionCode']
version_name = root.attrib['{http://schemas.android.com/apk/res/android}versionName']
version = "{0}-build{1}".format(version_name, version_code)

print("building app version {0}".format(version))

#run("./gradlew clean")

'''
run("./gradlew assembleMainSqlCipherRelease")
run("jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore storymaker-release.key app/build/outputs/apk/app-mainSqlCipher-release-unsigned.apk release")
run("jarsigner -verify -verbose -certs app/build/outputs/apk/app-mainSqlCipher-release-unsigned.apk")
run("/home/josh/Android/Sdk/build-tools/{0}/zipalign -v 4 app/build/outputs/apk/app-mainSqlCipher-release-unsigned.apk storymaker-release-{1}.apk".format(build_tools_version, version))
#'''

#'''
run("./gradlew assembleMainSqlCipherDebuggableRelease")
run("jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore storymaker-release.key app/build/outputs/apk/app-mainSqlCipher-debuggableRelease-unsigned.apk release")
run("jarsigner -verify -verbose -certs app/build/outputs/apk/app-mainSqlCipher-debuggableRelease-unsigned.apk")
run("/home/josh/Android/Sdk/build-tools/{0}/zipalign -v 4 app/build/outputs/apk/app-mainSqlCipher-debuggableRelease-unsigned.apk storymaker-debuggable-release-{1}.apk".format(build_tools_version, version))
#'''

#'''
run("./gradlew assembleMainSqlCipherDebug")
run("cp app/build/outputs/apk/app-mainSqlCipher-debug.apk storymaker-debug-{0}.apk".format(version))
#'''
