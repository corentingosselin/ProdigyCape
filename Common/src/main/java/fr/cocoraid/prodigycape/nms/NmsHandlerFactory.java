package fr.cocoraid.prodigycape.nms;

import fr.cocoraid.NmsHandler;
import fr.cocoraid.prodigycape.utils.VersionChecker;
import nms.NMS_1_19_4;
import nms.NMS_1_20_1;
import nms.NMS_1_20_2;
import nms.NMS_1_20_4;

public class NmsHandlerFactory {

    public static NmsHandler getHandler() {

        switch (VersionChecker.getCurrentVersion()) {
            case v1_19_R3:
                return new NMS_1_19_4();
            case v1_20_R1:
                return new NMS_1_20_1();
            case v1_20_R2:
                return new NMS_1_20_2();
            case v1_20_R3:
                return new NMS_1_20_4();
            default:
                throw new UnsupportedOperationException("Unsupported version: " + VersionChecker.getCurrentVersion());
        }

    }
}
