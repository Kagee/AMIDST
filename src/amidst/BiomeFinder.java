package amidst;

import amidst.logging.Log;
import amidst.map.MapObjectStronghold;
import amidst.map.layers.SpawnLayer;
import amidst.map.layers.StrongholdLayer;
import amidst.minecraft.Biome;
import amidst.minecraft.Minecraft;
import amidst.minecraft.MinecraftUtil;
import amidst.preferences.BiomeColorProfile;
import amidst.version.LatestVersionList;
import amidst.version.MinecraftProfile;
import amidst.version.VersionFactory;

import java.awt.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * Created by Anders Einar Hilden <hildenae@gmail.com on 02.08.14.
 *
 */
public class BiomeFinder {

    public static void main(String args[]) {

       if(args.length < 2) {
           System.out.println("Arguments: startseed checknum");
           System.out.flush();;
           System.exit(1);
       }
        int seed = 1;
        int checknum = 1;
        try {
            seed = Integer.parseInt(args[0]);
            checknum = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Arguments must be integers.");
            System.exit(1);
        }

        setup();
        boolean c = true;
        int xRadius = 500;
        int yRadius = 500;
        String names[] = {"Mesa", "Desert", "Plains", "Jungle", "Roofed Forest", "Forest", "Savanna", "Taiga", "Plains"};

        Log.i(String.format("Startseed is %s, endseed is %s", seed, seed + checknum));

        int lastSeed = -1;
        for(int i = seed; c && i < (seed + checknum); i++) {
            if (isPerfectBiome(i, xRadius, yRadius, names, true)) {
                Log.debug(String.format("Found stronghold within +/- x%s y%s on seed %s", xRadius, yRadius, i));
                c = false;
            }
            if(i % 5000 == 0) {
                Log.debug(String.format("Seed %s failed", i));
            }
            lastSeed = seed;
        }
        Log.debug(String.format("Last tested seed was %s", lastSeed));
    }
    public static void setup() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Log.crash(e, "Ops: " + thread);
            }
        });
        Util.setMinecraftDirectory();
        if (!Util.minecraftDirectory.exists() || !Util.minecraftDirectory.isDirectory()) {
            Log.crash("Unable to find Minecraft directory at: " + Util.minecraftDirectory);
            return;
        }
        BiomeColorProfile.scan();
        LatestVersionList.get().load(false); // FALSE FALSE FALSE!!!
        VersionFactory versionFactory = new VersionFactory();
        versionFactory.scanForProfiles();
        MinecraftProfile[] localVersions = versionFactory.getProfiles();
        Log.debug(String.format("Found %s profiles, selecting #0", localVersions.length));
        try {
            Util.setProfileDirectory(localVersions[0].getGameDir());
            Log.debug(String.format("Gamedir is %s", localVersions[0].getJarFile()));

            MinecraftUtil.setBiomeInterface(new Minecraft(localVersions[0].getJarFile()).createInterface());

           // long seed = stringToLong("7526988084274174185");
           // Log.debug(String.format("Seed is: %s", seed));
           // String type = "default";
           // Log.debug(String.format("Biome type is %s", type));
        } catch (MalformedURLException e) {
            Log.crash(e, "MalformedURLException on Minecraft load.");
        }
    }
    public static boolean isPerfectBiome(long seed, int xRadius, int yRadius, String[] names, boolean stronghold) {
        Options.instance.seed = seed;
        MinecraftUtil.createWorld(seed, "default");

        if(stronghold) {
            StrongholdLayer sl = new StrongholdLayer();
            sl.findStrongholds();
            MapObjectStronghold[] strongholds = sl.getStrongholds();
            if(strongholdsWithinBorders(strongholds, xRadius, yRadius) == 0){
                //Log.debug(String.format("There are no strongholds within +/- x%s y%s on seed %s", xRadius, yRadius, seed));
                return false;
            }
        }

        if(getBiomeNameAt(0, 0).contains("Ocean")) {return false;}
        //Point spawn = new SpawnLayer().getSpawnPosition();
        //Log.debug(String.format("Spawn is as x%s y%s", spawn.getX(), spawn.getY()));


        ArrayList<String> biomes = new ArrayList<String>();
        Collections.addAll(biomes, names);

        // Iterate through points within x/yRadius, check biome of each 200th.
        // Remove from list if found.
        // If list in empty, return true.
        //xRadius
        // yRadius

        return true;
    }

    public static String getBiomeNameAt(int x, int y) {
        int size = 1;
        int x1 = x - size >> 2;
        int y1 = y - size >> 2;
        int x2 = x + size >> 2;
        int y2 = y + size >> 2;

        int width = x2 - x1 + 1;
        int height = y2 - y1 + 1;

        int[] arrayOfInt = MinecraftUtil.getBiomeData(x1, y1, width, height);
        Biome localBiome = Biome.biomes[arrayOfInt[0]];
        return localBiome.name;
    }

    private static int strongholdsWithinBorders(MapObjectStronghold[] strongholds, int radiusX, int radiusY) {
        int within = 0;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(strongholds[i].getX()) < radiusX && Math.abs(strongholds[i].getY()) < radiusY) {
                within++;
            }
        }
        return within;
    }

    private static long stringToLong(String seed) {
        long ret;
        try {
            ret = Long.parseLong(seed);
        } catch (NumberFormatException err) {
            ret = seed.hashCode();
        }
        return ret;
    }
}
