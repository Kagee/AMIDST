package amidst;

import amidst.logging.Log;
import amidst.minecraft.Minecraft;
import amidst.minecraft.MinecraftUtil;
import amidst.preferences.BiomeColorProfile;
import amidst.version.LatestVersionList;
import amidst.version.MinecraftProfile;
import amidst.version.VersionFactory;

import java.net.MalformedURLException;

/**
 *
 * Created by Anders Einar Hilden <hildenae@gmail.com on 02.08.14.
 *
 */
public class BiomeFinder {

    public static void main(String args[]) {
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

            long seed = stringToLong("7526988084274174185");
            Log.debug(String.format("Seed is: %s", seed));
            String type = "Default";
            Log.debug(String.format("Biome type is %s", type));
            MinecraftUtil.createWorld(seed, type);
            System.out.println("Hello ... world?");
        } catch (MalformedURLException e) {
            Log.crash(e, "MalformedURLException on Minecraft load.");
        }
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
