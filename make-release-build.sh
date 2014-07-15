#!/bin/sh

set -e

if [ -e ~/.android/bashrc ]; then
    . ~/.android/bashrc
else
    echo "No ~/.android/bashrc found, 'android' and 'ndk-build' must be in PATH"
fi

./git-submodule-sync.sh

projectroot=`pwd`
projectname=`sed -n 's,.*name="app_name">\(.*\)<.*,\1,p' app/res/values/strings.xml`

for f in $projectroot/external/*/.git; do
    dir=`echo $f | sed 's,\.git$,,'`
    cd $dir
    git reset --hard
    git clean -fdx
    cd $projectroot
done

cd $projectroot
git reset --hard
git clean -fdx
git submodule foreach git reset --hard
git submodule foreach git clean -fdx
git submodule sync --recursive
git submodule update --init --recursive

cd external/android-ffmpeg-java/external/android-ffmpeg/ 
./configure_make_everything.sh
cd $projectroot

if [ -e ~/.android/ant.properties ]; then
    cp ~/.android/ant.properties $projectroot/app/
else
    echo "skipping release ant.properties"
fi

./setup-ant.sh
cd app/
ant release

apk=$projectroot/app/bin/$projectname-release.apk
if [ -e $apk ]; then
    gpg --detach-sign $apk
else
    echo $apk does not exist!
fi
