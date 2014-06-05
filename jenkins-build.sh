#!/bin/sh

git submodule sync --recursive
git submodule update --init --recursive

. ~/.android/bashrc

./setup-ant.sh
cd $WORKSPACE/external/android-ffmpeg-java/external/android-ffmpeg/
./configure_make_everything.sh
