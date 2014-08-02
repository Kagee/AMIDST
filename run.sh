#!/bin/bash
checksToDo=100000
ant
java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
#java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo &
