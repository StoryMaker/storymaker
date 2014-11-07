adb shell "run-as org.storymaker.app cat /data/data/org.storymaker.app/databases/sm.db > /sdcard/sm.db" 
adb pull /sdcard/sm.db
