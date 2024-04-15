package fr.cocoraid.prodigycape.nms;

import fr.cocoraid.prodigycape.utils.VersionChecker;
import org.bukkit.entity.Player;

public class NmsHandlerFactory {

    public static NmsHandler getHandler() {

       /* switch (VersionChecker.getCurrentVersion()) {
            case v1_19_R2:
                return new NMS_1_19_4();
            case v1_19_R3:
                return new fr.prodigycape.nms.v1202.NmsHandler1202();
            case v1_20_R1:
                return new fr.prodigycape.nms.v1204.NmsHandler1204();
            default:
                throw new UnsupportedOperationException("Unsupported version: " + VersionChecker.getCurrentVersion());
        }*/
        return new NmsHandler() {
            @Override
            public Object clientInfoWithoutCape(Object object) {
                return null;
            }

            @Override
            public void removeCape(Player player) {

            }
        };
    }
}
