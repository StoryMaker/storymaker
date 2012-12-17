#!/bin/bash

if ! type -P android &> /dev/null; then
    echo "Error: 'android' utility is not in your path."
    echo "  Did you forget to setup the SDK?"
    exit 1
fi
readarray <<END
external/HoloEverywhere/contrib/ActionBarSherlock/library
external/HoloEverywhere/library
external/android-ffmpeg-java
external/OnionKit/library
external/WordpressJavaAndroid
END

for project in "${MAPFILE[@]}"; do
        android update project --path $project -t 10
done


