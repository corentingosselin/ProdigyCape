package fr.cocoraid.prodigycape.utils;

import org.bukkit.Bukkit;

/**
 * Created by cocoraid on 14/01/2018.
 */
public enum VersionChecker {

   v1_19_R2(0), v1_19_R3(1), v1_20_R1(2),  v1_20_R2(3), v1_20_R3(4), v1_20_R4(5);

    private static VersionChecker currentVersion = VersionChecker.valueOf(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
    private int index;

    VersionChecker(int index) {
        this.index = index;
    }


    public static boolean isHigherOrEqualThan(VersionChecker v) {
        return currentVersion.getIndex() >= v.getIndex();
    }

    public static boolean isLowerOrEqualThan(VersionChecker v) {
        return currentVersion.getIndex() <= v.getIndex();
    }

    public static VersionChecker getCurrentVersion() {
        return currentVersion;
    }

    public int getIndex() {
        return index;
    }
}
