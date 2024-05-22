package fr.cocoraid.prodigycape.nms;

import fr.cocoraid.prodigycape.IDisplayItem;
import fr.cocoraid.prodigycape.NmsHandler;

import fr.cocoraid.prodigycape.support.NMS_1_19_4;
import fr.cocoraid.prodigycape.support.NMS_1_20_1;
import fr.cocoraid.prodigycape.support.NMS_1_20_2;
import fr.cocoraid.prodigycape.support.NMS_1_20_4;
import fr.cocoraid.prodigycape.support.NMS_1_20_6;
import fr.cocoraid.prodigycape.utils.VersionChecker;
import org.bukkit.Bukkit;

public class NmsHandlerFactory {

    public static NmsHandler getHandler() {
        Bukkit.getLogger().info("PaperMC detected " + Bukkit.getBukkitVersion());
        switch (VersionChecker.getCurrentVersion()) {
            case v1_19_R3:
                return new NMS_1_19_4();
            case v1_20_R1:
                return new NMS_1_20_1();
            case v1_20_R2:
                return new NMS_1_20_2();
            case v1_20_R3:
                return new NMS_1_20_4();
            case v1_20_R4:
                return new NMS_1_20_6();
            default:
                throw new UnsupportedOperationException("Unsupported version: " + VersionChecker.getCurrentVersion());
        }

    }

    public static IDisplayItem getDisplayItem() {
        switch (VersionChecker.getCurrentVersion()) {
            case v1_19_R3:
                return new fr.cocoraid.prodigycape.support.DisplayItem_1_19_4();
            case v1_20_R1:
                return new fr.cocoraid.prodigycape.support.DisplayItem_1_20_1();
            case v1_20_R2:
                return new fr.cocoraid.prodigycape.support.DisplayItem_1_20_2();
            case v1_20_R3:
                return new fr.cocoraid.prodigycape.support.DisplayItem_1_20_4();
            case v1_20_R4:
                return new fr.cocoraid.prodigycape.support.DisplayItem_1_20_6();
            default:
                throw new UnsupportedOperationException("Unsupported version: " + VersionChecker.getCurrentVersion());
        }
    }
}
