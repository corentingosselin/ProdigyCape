package fr.cocoraid.prodigycape.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

/**
 * Created by cocoraid on 14/01/2018.
 */
public enum VersionChecker {

    v1_19_R2(0), v1_19_R3(1), v1_20_R1(2), v1_20_R2(3), v1_20_R3(4), v1_20_R4(5), v1_21_R1(6);

    @Getter
    private static VersionChecker currentVersion;
    private static boolean isPapermc = false;

    static {
        try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            isPapermc = true;
        } catch (ClassNotFoundException ignored) {
        }

        if (isPapermc) {
            String version = Bukkit.getBukkitVersion();
            if (version.contains("1.20.6")) {
                currentVersion = VersionChecker.v1_20_R4;
            } else if (version.contains("1.21")) {
                currentVersion = VersionChecker.v1_21_R1;
            } else {
                currentVersion = VersionChecker.valueOf(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
            }
        } else {
            currentVersion = VersionChecker.valueOf(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
        }
    }

    @Getter
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

}
