#!/bin/bash
BIOMES="Plains,Mesa,Roofed Forest,Taiga,Jungle,Savanna"
TOCHECK=15000000
STARTSEED=0
SEEDSPERJOB=4999999
BASEDIR=~/.minecraft
TMPDIR=/tmp
ant
CURRENTSTARTSEED=$STARTSEED
for ((i=$TOCHECK;i>=0;i-=$SEEDSPERJOB));
do
	CURRENTTEMP=$(mktemp -d ${TMPDIR}/mctmp.XXXXXXXX)
	rsync -r $BASEDIR $CURRENTTEMP
	echo "Rsyncing data to $CURRENTTEMP"
	CURRENTENDSEED=$(($CURRENTSTARTSEED+$SEEDSPERJOB))
	echo -n "echo java -Dbiomefinder.mcpath=$CURRENTTEMP -Dbiomefinder.biomes='${BIOMES}' -jar dist/findbiome.jar " >> ${TMPDIR}/cmd.args
	echo "${CURRENTSTARTSEED} ${SEEDSPERJOB} >> $tmpDir/scan-${CURRENTSTARTSEED}-to-${CURRENTENDSEED}.log" >> ${TMPDIR}/cmd.args
	CURRENTSTARTSEED=$(($CURRENTENDSEED+1))
done
procs=`cat /proc/cpuinfo|grep processor -c`
#xargs --max-procs=$procs --arg-file=$tmpDir/cmd.args > result.txt
cat ${TMPDIR}/cmd.args
rm -rf ${TMPDIR}/mctmp.*
rm ${TMPDIR}/cmd.args
