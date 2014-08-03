#!/bin/bash
checksToDo=100000
ant
java -jar dist/findbiome.jar `expr $RANDOM \* 99999999` $checksToDo

# en veldig fin løsning, samle opp alle kommandoene som skal kjøres i en egen fil, en kommando per linje.
# den catter du inn i xargs --max-proces=<antall CPUer>, da vil xargs fordele alt utover så det alltid kjører
# N tråder.   Tiperdet mellom hver kjøring må tas en kopi av .minecraft mappa, og gamedir sendes med som java-opsjon
