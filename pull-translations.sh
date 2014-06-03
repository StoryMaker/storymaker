#!/bin/sh

tx pull -l ar,bs_BA,sr,es,fa,de,mk,ko,fr

rm app/assets/LessonTitles-bs.txt
mv app/assets/LessonTitles-bs_BA.txt app/assets/LessonTitles-bs.txt

rm -r app/res/values-bs
mv app/res/values-bs_BA app/res/values-bs

rm -r app/assets/story/templates/bs
mv app/assets/story/templates/bs_BA app/assets/story/templates/bs
