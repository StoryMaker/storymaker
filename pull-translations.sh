#!/bin/sh

tx pull -f -l ru,ar,bs_BA,sr,es,fa,de,mk,ko,fr,rw,vi --minimum-perc=10

rm app/assets/LessonTitles-bs.txt
mv app/assets/LessonTitles-bs_BA.txt app/assets/LessonTitles-bs.txt

rm -r app/res/values-bs
mv app/res/values-bs_BA app/res/values-bs

rm -R external/liger/lib/src/main/res/values-bs
mv external/liger/lib/src/main/res/values-bs_BA external/liger/lib/src/main/res/values-bs

rm -r app/assets/story/templates/bs
mv app/assets/story/templates/bs_BA app/assets/story/templates/bs
