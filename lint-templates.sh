#!/bin/bash


failures=()

for d in app/assets/story/templates/;
do
    for d2 in $d*;
    do
        printf "--------\nprocessing dir: $d2\n--------\n"
        for f in $d2/simple/*;
        do
            printf "linting file: $f"
            
            jsonlint -v $f
            if  [ "$?" -eq 0 ]; then
                printf "        ok"
            else
                printf "        FAILED"
                failures+="\n$f"
            fi
            printf "\n"
        done
    done    
done

printf "\nThese files all failed jsonlint:\n"
printf $failures
printf "\n"
















