SeedFinder
==========

SeedFinder is a command line program that can automatically analyzes Minecraft 
seeds and check if they match a set of desired criteria. This is very useful if 
you have size constraints, such as if you want to use the new world border 
feature in Minecraft 1.8. It reuses a lot of code from AMIDST 
(https://github.com/skiphs/AMIDST). 

THIS TOOL IS NOT USER FRIENDLY. It has no GUI, and the user is expected to know 
how to run software from the command line. It needs a copy of the Minecraft 
client to be able to run, just like AMIDST.


Performance
-----------
It has clocked in at around 700 seed/second running on an Core i7 laptop with
an SSD. 


License and warranty
--------------------

SeedFinder comes with ABSOLUTELY NO WARRANTY. It is free software, and you are
welcome to redistribute it under certain conditions. See LICENSES_COMBINED.txt
inside seedfinder.jar for more details on both of these points.


Simple usage (defaults)
-----------------------
java -jar seedfinder.jar <startseed> <endseed>

where <startseed> and <endseed> can be between -9223372036854775808 and 
9223372036854775807 (the range of all possible Minecraft seeds).
<startseed> must be smaller than <endseed>.

This will look for a map where 0, 0 is not in an ozean biome, and where, within 
a map size of 2000x2000 (-1000 to +1000 in both directions), it can find at 
least one stronghold plus the following biomes (chosen for a nice and varied 
Minecraft world): Taiga, Plains, Mesa, Jungle, Desert, Roofed Forest, Forest 
and Savanna.

The program will search all seeds from <startseed> to <endseed> for a map that 
matches these requirements. Unless sf.debug is set to true, it will only output 
"[POSSIBLE MATCH] Seed <seed>" whenever it finds a candidate. This seed can 
then be tested in AMIDST or the Minecraft client itself to see if it's suitable.



Advanced usage
--------------
The settings and criteria of SeedFinder.jar can be controlled by supplying any 
of the following properties to the java process.

sf.minecraftprofile
Value       : 0 to <number of available Minecraft profiles>
Default     : 0
Description : Allows you to choose which Minecraft profile (version) to use as
              a basis for generating the world. This can impact the terrain
              generation. Profiles are created in the Minecraft launcher.

sf.debug
Value       : true/false
Default     : false (limited output)
Description : When set to true, the program will output information about every
              seed that has been tested (can be extremely spammy).

sf.wateratzerozero
Value       : true/false
Default     : false
Description : When set to false, the program will discard any seed that has 
              water at 0,0.

sf.dontrequirestrongholds
Value       : true/false
Default     : false
Description : When set to true, a stronghold MAY be within the borders, but
              it is not required.

sf.requiredbiomes
Value       : Comma separated list of biomes that _must_ be present for a match
Default     : "Taiga, Plains, Mesa, Jungle, Desert, Roofed Forest, Forest, Savanna"
Description : A list of what you want within the borders set by sf.radius. 
              Matches using "begins with", that is, "Desert" will 
              also match "Desert M" and "Desert Hills". If a biome name with 
              spaces (e.g Cold Taiga Hills) is supplied, the whole argument
              must be contained within single quotes (see example).
              https://github.com/skiphs/AMIDST/blob/master/Default-sorted.json has 
              a list of possible biomes.

sf.mcpath
Value       : <path to a valid Minecraft installation folder>
Default     : Automatic discovery of Minecraft installation, same as AMIDST uses
Description : If Minecraft isn't installed in a default location, this parameter
              must be set in order for SeedFinder to be able to run.

sf.radius
Value       : Any long, but probably smart to keep it above 200
Default     : 1000
Description : The radius of the search area from 0,0. A radius of 1000
              will search within +/- x/z -1000 to +1000, useful for a 
              map of 2000 by 2000 blocks.


Examples
--------

# Default settings, but select the third (zero-indexed) of a number of 
# avaliable Minecraft profiles.
java -Dsf.minecraftProfile=2 -jar seedfinder.jar <startseed> <endseed>

# Find a seed with a Mesa Plateau and a Jungle within 500 blocks of 0,0.
java -Dsf.requiredbiomes='Mesa Plateau,Jungle' -Dsf.xlength=500 \
-Dsf.ylength=500 -jar seedfinder.jar <startseed> <endseed>

# Find a map with the default values, but allow water at 0,0
java -Dsf.wateratzerozero=true -jar seedfinder.jar <startseed> <endseed>

# Use the folder /tmp/fDrfTtjd instad of .minecraft to find the minecraft files.
# Useful if you e.g want to run several seedfinder processes in parallel. 
java -Dsf.mcpath=/tmp/fDrfTtjd -jar seedfinder.jar <startseed> <endseed>

The bash-script run.sh (inside seedfinder_src.zip) contains a not very tested 
script for running several seedfinder-processes in parallel on a UNIX system.



For developers
--------------

THIS SHOULD NOT BE NEEDED FOR MOST NORMAL USE CASES. YOU ONLY NEED TO DO THIS 
IF YOU E.G. WANT TO SEARCH AROUND THE NATURAL SPAWN OR WANT TO DO OTHER TWEAKS TO THE
SEARCH ALGORITHM

To edit or compile SeedFinder for your own uses, unzip seedfinder_src.zip
to the basefolder of a checkout of the AMIDST sourcecode.

The sourcecode for SeedFinder is located in src/amidst/SeedFinder.java

To compile, run "ant -f sf.xml". To run locally after compilation, 
run "java -jar dist/seedfinder.jar"

To distribute a jar that can be used on other computers, run 
"ant -f sf.xml uberjar", and distribute the 
"store/seedfinder-dist.jar" file.
