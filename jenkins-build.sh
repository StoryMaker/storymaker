#!/bin/sh

set -e
set -x

if [ -z $WORKSPACE ]; then
    export WORKSPACE=`pwd`
fi

if [ -z $ANDROID_HOME ]; then
    if [ -e ~/.android/bashrc ]; then
        . ~/.android/bashrc
    else
        echo "ANDROID_HOME must be set!"
        exit
    fi
fi

git submodule sync --recursive
git submodule foreach --recursive "git submodule sync"
git submodule update --init --recursive

./setup-ant.sh
cd $WORKSPACE/external/android-ffmpeg-java/external/android-ffmpeg/
./configure_make_everything.sh
