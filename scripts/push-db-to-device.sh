adb push sm.db /sdcard/sm.db
adb shell "run-as info.guardianproject.mrapp rm /data/data/info.guardianproject.mrapp/databases/sm.db" 
adb shell "run-as info.guardianproject.mrapp cat /sdcard/sm.db > /data/data/info.guardianproject.mrapp/databases/sm.db" 
