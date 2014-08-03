#!/bin/bash
#
# Script for running SeedFinder in paralell
# Author: Tor Henning Ueland < tor.henning@gmail.com >
#
#config start
BIOMES="Plains,Mesa,Roofed Forest,Taiga,Jungle,Savanna"
TOCHECK=10000000
STARTSEED=20000000
SEEDSPERJOB=1000000
BASEDIR=~/.minecraft
TMPDIR=/tmp
#config end 
ant
CURRENTSTARTSEED=$STARTSEED
for ((i=$TOCHECK;i>=0;i-=$SEEDSPERJOB));
do
	CURRENTTEMP=$(mktemp -d ${TMPDIR}/mctmp.XXXXXXXX)
	rsync -r "$BASEDIR/" "$CURRENTTEMP/."
	echo "Rsyncing data to $CURRENTTEMP"
	CURRENTENDSEED=$(($CURRENTSTARTSEED+$SEEDSPERJOB))
	echo "java -Dsf.mcpath=$CURRENTTEMP -Dsf.biomes=\\\"${BIOMES}\\\" -jar dist/seedfinder.jar ${CURRENTSTARTSEED} ${SEEDSPERJOB};" >> ${TMPDIR}/cmd.args
	CURRENTSTARTSEED=$(($CURRENTENDSEED+1))
done
procs=`cat /proc/cpuinfo|grep processor -c`
cat ${TMPDIR}/cmd.args|xargs --max-procs=$procs  -I CMD bash -c CMD > result.txt
rm -rf ${TMPDIR}/mctmp.*
rm ${TMPDIR}/cmd.args
