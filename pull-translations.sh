#!/bin/sh

tx pull -f -l ru,ja,nl,ar,bs_BA,sr,es,fa_IR,de,mk,ko,fr,rw,vi --minimum-perc=10

# fix bs

rm app/assets/LessonTitles-bs.txt
mv app/assets/LessonTitles-bs_BA.txt app/assets/LessonTitles-bs.txt

rm -r app/res/values-bs
mv app/res/values-bs_BA app/res/values-bs

rm -R external/liger/lib/src/main/res/values-bs
mv external/liger/lib/src/main/res/values-bs_BA external/liger/lib/src/main/res/values-bs

#rm -r app/assets/story/templates/bs
#mv app/assets/story/templates/bs_BA app/assets/story/templates/bs

# fix farsi

rm app/assets/LessonTitles-fa.txt
mv app/assets/LessonTitles-fa_IR.txt app/assets/LessonTitles-fa.txt

rm -r app/res/values-fa
mv app/res/values-fa_IR app/res/values-fa

rm -R external/liger/lib/src/main/res/values-fa
mv external/liger/lib/src/main/res/values-fa_IR external/liger/lib/src/main/res/values-fa

#rm -r app/assets/story/templates/fa
#mv app/assets/story/templates/fa_IR app/assets/story/templates/fa

rm -r external/SecureShareLib/SecureShareUILibrary/res/values-fa/
mv external/SecureShareLib/SecureShareUILibrary/res/values-fa_IR/ external/SecureShareLib/SecureShareUILibrary/res/values-fa/
