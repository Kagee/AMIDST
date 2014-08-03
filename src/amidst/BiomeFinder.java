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
import sun.org.mozilla.javascript.ast.TryStatement;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Anders Einar Hilden <hildenae@gmail.com on 02.08.14.
 */

public class BiomeFinder extends Thread {

    long startSeed = 1;
    long numSeedsToCheck = 1;
    File tmpPath;
    String names[] = {"Taiga", "Plains", "Mesa", "Jungle", "Desert", "Roofed Forest", "Forest", "Savanna"};

    public BiomeFinder(long seed, int todo, File mcPath) {
        startSeed = seed;
        numSeedsToCheck = todo;
        tmpPath = mcPath;
    }

    public BiomeFinder(String[] args) {
        if (args.length < 2) {
            System.out.println("Arguments: <startseed (-long to +long)> <number of seed to check from startseed>");
            System.out.flush();
            System.exit(1);
        }
        try {
            startSeed = Long.parseLong(args[0]);
            numSeedsToCheck = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.err.println("Arguments must be java longs.");
            System.exit(1);
        }
        updateWantedBiomes();
        run();
    }

    public static void main(String args[]) {
        new BiomeFinder(args);
    }

    public void updateWantedBiomes() {
        String biomes = System.getProperty("biomefinder.biomes");
        if (biomes == null) {
            return;
        } else {
            names = biomes.split(",");
            Log.debug("New biomes are " + Arrays.asList(names).toString().replaceAll("^\\[|\\]$", ""));
        }
    }

    public void run() {
        setup();
        boolean c = true;
        int xRadius = 500;
        int yRadius = 500;
        Log.debug(String.format("Startseed is %s, endseed is %s", startSeed, startSeed + numSeedsToCheck));
        Log.debug("Looking for biomes: " + Arrays.asList(names).toString().replaceAll("^\\[|\\]$", ""));


        long lastSeed = -1;
        Log.isShowingDebug = Boolean.parseBoolean(System.getProperty("biomefinder.debug"));
        for (long i = startSeed; c && i < (startSeed + numSeedsToCheck); i++) {
            if (isPerfectBiome(i, xRadius, yRadius, names, true)) {
                System.out.println(String.format("[POSSIBLE MATCH] Seed %s", i));
                //c = false;
            }
            lastSeed = i;
        }
        Log.i(String.format("[END] Last tested seed was %s", lastSeed));
    }

    public void setup() {
        String overriveMinecraftPath = System.getProperty("biomefinder.mcpath");
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

        int profileInt = -1;
        if (localVersions.length > 1) {
            String profileNum = System.getProperty("biomefinder.profileNum", "-1");

            try {
                profileInt = Integer.parseInt(profileNum);
            } catch (NumberFormatException nfe) {
            }

            if (profileInt == -1) {
                System.out.println(String.format("Found %s profiles, please select the one to use with"
                        + " -Dbiomefinder.profileNum=<num>", localVersions.length));

                for (int i = 0; i < localVersions.length; i++) {
                    System.out.println(String.format(
                            "[%s]: %s", i, localVersions[i].getProfileName()));
                }
                System.exit(1);
            }

        } else {
            profileInt = 0;
        }

        MinecraftProfile selectedMinecraftProfile = localVersions[profileInt];
        Log.debug(String.format("Using Minecraft profile #%s, %s", profileInt, localVersions[profileInt].getProfileName()));

        try {

            Util.setProfileDirectory(selectedMinecraftProfile.getGameDir());

            Log.debug(String.format("Gamedir is %s", selectedMinecraftProfile.getJarFile()));

            MinecraftUtil.setBiomeInterface(new Minecraft(selectedMinecraftProfile.getJarFile()).createInterface());

        } catch (MalformedURLException e) {
            Log.debug(e, "MalformedURLException on Minecraft load.");
        }
    }

    public boolean isPerfectBiome(long seed, int xRadius, int yRadius, String[] names, boolean stronghold) {
        Options.instance.seed = seed;
        MinecraftUtil.createWorld(seed, "default");

        if (stronghold) {
            if (!strongholdWithinBorders(xRadius, yRadius)) {
                Log.debug("FAIL: No strongholds");
                return false;
            }
        }
        ArrayList<String> biomes = new ArrayList<String>();
        if (getBiomeNameAt(0, 0).contains("Ocean")) {
            Log.debug("FAIL: Ocean at 0, 0");
            return false;
        }
        for (int x = -xRadius; x < xRadius; x += 200) {
            for (int y = -yRadius; y < yRadius; y += 200) {
                String biome = getBiomeNameAt(x, y);
                if (!biomes.contains(biome)) {
                    biomes.add(biome);
                }
            }
        }
        for (String b : names) {
            if (!containsBiome(biomes, b)) {
                Log.debug(String.format("FAIL: Missing biome %s", b));
                return false;
            }
        }
        return true;
    }

    public boolean containsBiome(ArrayList<String> biomes, String biome) {
        for (String b : biomes) {
            if (b.startsWith(biome)) {
                return true;
            }
        }
        return false;
    }

    public String getBiomeNameAt(int x, int y) {
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

    private int strongholdsWithinBorders(int radiusX, int radiusY) {
        StrongholdLayer sl = new StrongholdLayer();
        sl.findStrongholds();
        MapObjectStronghold[] strongholds = sl.getStrongholds();
        int within = 0;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(strongholds[i].getX()) < radiusX && Math.abs(strongholds[i].getY()) < radiusY) {
                within++;
            }
        }
        return within;
    }

    private boolean strongholdWithinBorders(int radiusX, int radiusY) {
        StrongholdLayer sl = new StrongholdLayer();
        sl.findStrongholds();
        MapObjectStronghold[] strongholds = sl.getStrongholds();
        for (int i = 0; i < 3; i++) {
            if (Math.abs(strongholds[i].getX()) < radiusX && Math.abs(strongholds[i].getY()) < radiusY) {
                return true;
            }
        }
        return false;
    }

    private long stringToLong(String seed) {
        long ret;
        try {
            ret = Long.parseLong(seed);
        } catch (NumberFormatException err) {
            ret = seed.hashCode();
        }
        return ret;
    }
}
