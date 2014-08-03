#!/bin/bash
biomes="Plains,Mesa,Roofed Forest,Taiga,Jungle,Savanna"
checksToDo=10000000
startSeed=912850000
checksPerJob=5000000
mcDir=~/.minecraft
tmpDir=/tmp/mc
ant
mkdir $tmpDir
for ((i=$checksToDo;i>=0;i-=$checksPerJob));
do
	j=$(($j+$startSeed))
	mcTmp="$tmpDir/$RANDOM"
	rsync -r $mcDir $mcTmp
	echo "Rsyncing data to $mcTmp"
	echo "java -Dbiomefinder.mcpath=$mcTmp -Dbiomefinder.debug=false -Dbiomefinder.biomes='Taiga,Plains,Mesa,Jungle,Desert,Roofed Forest,Forest,Savanna' -jar dist/findbiome.jar $j $checksPerJob" >> $tmpDir/cmd.args
done
procs=`cat /proc/cpuinfo|grep processor -c`
xargs --max-procs=$procs --arg-file=$tmpDir/cmd.args > result.txt
rm -rf $tmpDir
# en veldig fin løsning, samle opp alle kommandoene som skal kjøres i en egen fil, en kommando per linje.
# den catter du inn i xargs --max-proces=<antall CPUer>, da vil xargs fordele alt utover så det alltid kjører
# N tråder.   Tiperdet mellom hver kjøring må tas en kopi av .minecraft mappa, og gamedir sendes med som java-opsjon
