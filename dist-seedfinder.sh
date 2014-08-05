#! /bin/bash
rm seedfinder.zip seedfinder_src.zip
ant
ant -f sf.xml
ant -f sf.xml uberjar
mv store/seedfinder-dist.jar seedfinder.jar
7z a seedfinder_src.zip README_SEEDFINDER.txt LICENSES_COMBINED.txt run.sh sf.xml src/amidst/SeedFinder.java
7z a seedfinder.zip seedfinder_src.zip README_SEEDFINDER.txt LICENSES_COMBINED.txt seedfinder.jar
rm seedfinder.jar seedfinder_src.zip
rm -r store
