package amidst;

import amidst.logging.Log;
import amidst.map.MapObjectStronghold;
import amidst.map.layers.StrongholdLayer;
import amidst.minecraft.Biome;
import amidst.minecraft.Minecraft;
import amidst.minecraft.MinecraftUtil;
import amidst.preferences.BiomeColorProfile;
import amidst.version.LatestVersionList;
import amidst.version.MinecraftProfile;
import amidst.version.VersionFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by
 * Anders Einar Hilden <hildenae@gmail.com>
 * Tor Henning Ueland <tor.henning@gmail.com>
 *
 * August 2014
 */

public class SeedFinder {
    int biomeSampleLength = 200;

    /* These should be changed by the two required
    * command line arguments */
    long startSeed = 1;
    long numSeedsToCheck = 1;
    long endSeed = 1;

    /* Default values, might be changed by readSettingsFromProperties() */
    String requiredBiomes[] = {"Taiga", "Plains", "Mesa", "Jungle", "Desert", "Roofed Forest", "Forest", "Savanna"};
    int xLength = 1000, yLength = 1000;
    int minecraftProfileNum = 0;
    boolean strongholdRequired = false; // TODO: implement sf.strongholdRequired

    public SeedFinder(String[] args) {
        if (args.length < 2) {
           usage();
        }
        try {
            startSeed = Long.parseLong(args[0]);
            numSeedsToCheck = Long.parseLong(args[1]);
            endSeed = Long.parseLong(args[1]);
            if (startSeed > endSeed) {
                System.err.println("Error: startseed must be less than endseed");
                usage();
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: startseed and endseed must be numbers " +
                    "within -9223372036854775808 to 9223372036854775807");
            usage();
        }
        readSettingsFromProperties();
        setup();
        run();
    }

    public static void main(String args[]) {
        new SeedFinder(args);
    }

    public void run() {
        Log.debug(String.format("Startseed is %s, endseed is %s", startSeed, endSeed));
        Log.debug("Looking for the following biomes: ");
        Log.debug(arrayToString(requiredBiomes));

        for (long seed = startSeed; seed <= endSeed; seed++) {
            if (isPerfectSeed(seed)) {
                System.out.println(String.format("[POSSIBLE MATCH] Seed %s", seed));
            }
        }

        Log.i(String.format("Finished run from startseed %s to endseed %s", startSeed, endSeed));
    }

    public void setup() {
        String overriveMinecraftPath = System.getProperty("sf.mcpath");
        if (overriveMinecraftPath != null) {
            File mcd;
            mcd = new File(overriveMinecraftPath);
            if (!mcd.exists() || !mcd.isDirectory()) {
                System.err.println(String.format("[ERROR] MinecraftDir %s is invalid", mcd));
                System.err.flush();
                System.exit(1);
            }
            Log.debug(String.format("[CONFIG] Set MinecraftDir to %s", mcd));
            Options.instance.minecraftPath = overriveMinecraftPath;
        }

        Util.setMinecraftDirectory();
        Log.i("MinecraftDirectory is " + Util.minecraftDirectory);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                Log.debug(e, "Ops: " + thread);
            }
        });

        BiomeColorProfile.scan();
        LatestVersionList.get().load(false); // FALSE FALSE FALSE!!!
        VersionFactory versionFactory = new VersionFactory();
        versionFactory.scanForProfiles();
        MinecraftProfile[] localVersions = versionFactory.getProfiles();

        if (localVersions.length > 1) {
            getMincraftProfileProperty(localVersions);
        }

        MinecraftProfile selectedMinecraftProfile = localVersions[minecraftProfileNum];
        Log.debug(String.format("Using Minecraft profile #%s, %s",
                minecraftProfileNum, localVersions[minecraftProfileNum].getProfileName()));

        try {

            Util.setProfileDirectory(selectedMinecraftProfile.getGameDir());

            Log.debug(String.format("Gamedir is %s", selectedMinecraftProfile.getJarFile()));

            MinecraftUtil.setBiomeInterface(new Minecraft(selectedMinecraftProfile.getJarFile()).createInterface());

        } catch (MalformedURLException e) {
            Log.debug(e, "MalformedURLException on Minecraft load.");
        }
    }

    public boolean isPerfectSeed(long seed) {
        Options.instance.seed = seed;
        MinecraftUtil.createWorld(seed, "default");

        if (strongholdRequired) {
            if (strongholdsWithinBorders() == 0) {
                Log.debug("FAIL: No strongholds");
                return false;
            }
        }

        if (getBiomeNameAt(0, 0).contains("Ocean")) {
            Log.debug("FAIL: Ocean at 0, 0");
            return false;
        }

        ArrayList<String> discoveredBiomes = new ArrayList<String>();
        /* Iterate over the avalible area, get biome samples every biomeSampleLength block. */
        for (int x = -xLength; x < xLength; x += biomeSampleLength) {
            for (int y = -xLength; y < xLength; y += biomeSampleLength) {
                String biome = getBiomeNameAt(x, y);
                if (!discoveredBiomes.contains(biome)) {
                    discoveredBiomes.add(biome);
                }
            }
        }

        for (String requiredBiome : requiredBiomes) {
            if (!containsBiome(discoveredBiomes, requiredBiome)) {
                Log.debug("[NOT MATCH] Missing biome " + requiredBiome);
                return false;
            }
        }
        return true;
    }

    public boolean containsBiome(ArrayList<String> discoveredBiomes, String requiredBiome) {
        for (String biome : discoveredBiomes) {
            if (biome.startsWith(requiredBiome)) {
                return true;
            }
        }
        return false;
    }

    public String getBiomeNameAt(int x, int y) {
        int x1 = x - 1 >> 2;
        int y1 = y - 1 >> 2;
        int x2 = x + 1 >> 2;
        int y2 = y + 1 >> 2;
        int width = x2 - x1 + 1;
        int height = y2 - y1 + 1;
        int[] arrayOfInt = MinecraftUtil.getBiomeData(x1, y1, width, height);
        return Biome.biomes[arrayOfInt[0]].name;
    }

    private int strongholdsWithinBorders() {
        StrongholdLayer sl = new StrongholdLayer();
        sl.findStrongholds();
        MapObjectStronghold[] strongholds = sl.getStrongholds();
        int within = 0;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(strongholds[i].getX()) < xLength && Math.abs(strongholds[i].getY()) < yLength) {
                within++;
            }
        }
        return within;
    }

    /*
    * *********************************
    * Functions for reading properties
    * *********************************
    * */

    private void readSettingsFromProperties() {
        Log.isShowingDebug = Boolean.parseBoolean(System.getProperty("sf.debug")); // Defaults to false
        updateWantedBiomes();
        setupXYBorders();
    }

    public void updateWantedBiomes() {
        String biomes = System.getProperty("sf.requiredbiomes");
        if (biomes == null) {
            return;
        } else {
            requiredBiomes = biomes.split(",");
            Log.debug("New requiredBiomes set to " + arrayToString(requiredBiomes));
        }
    }

    private void setupXYBorders() { /* Set up borders based on properties or default values */
        String xl = System.getProperty("sf.xlength");
        String yl = System.getProperty("sf.ylength");

        if (xl != null && yl != null) {
            try {
                xLength = Math.abs(Integer.parseInt(xl));
                yLength = Math.abs(Integer.parseInt(yl));
                Log.debug("Read new xLength and yLength from properties");
            } catch (NumberFormatException nfe) {
                Log.debug("xLength or yLength from properties is invalid, using defaults");
            }
        } else {
            // Not specified properties, use defaults.
            Log.debug("sf.xlength and sf.ylength not specified, using defaults");
        }
        Log.debug(String.format("xLength %s, yLength %s (mapsize: %s)",
                xLength, yLength, String.format("%s*%s", xLength * 2, yLength * 2)));
    }

    void getMincraftProfileProperty(MinecraftProfile[] localVersions) {
        String profileNum = System.getProperty("sf.mincraftProfile", "-1");

        try {
            minecraftProfileNum = Integer.parseInt(profileNum);
        } catch (NumberFormatException nfe) {}

        if (minecraftProfileNum == -1 || minecraftProfileNum >= localVersions.length) {
            System.out.println(String.format("Found %s profiles, please select the one to use with"
                    + " -Dsf.mincraftProfile=<num>", localVersions.length));

            for (int i = 0; i < localVersions.length; i++) {
                System.out.println(String.format(
                        "[%s]: %s", i, localVersions[i].getProfileName()));
            }
            System.exit(1);
        }
    }

    /* Utils */
    private String arrayToString(String[] arr) {
        return Arrays.asList(arr).toString().replaceAll("^\\[|\\]$", "");
    }
    private void usage() {
        System.err.println("Arguments: <startseed> <endseed>");
        System.err.println("Min: -9223372036854775808, Max: 9223372036854775807, startseed < endseed");
        System.err.flush();
        System.exit(1);
    }
}