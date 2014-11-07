adb push sm.db /sdcard/sm.db
adb shell "run-as org.storymaker.app rm /data/data/org.storymaker.app/databases/sm.db" 
adb shell "run-as org.storymaker.app cat /sdcard/sm.db > /data/data/org.storymaker.app/databases/sm.db" 
