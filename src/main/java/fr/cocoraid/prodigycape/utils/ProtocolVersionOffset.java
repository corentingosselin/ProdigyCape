package fr.cocoraid.prodigycape.utils;

public enum ProtocolVersionOffset {

    V1_21(767, -0.4F),
    V1_20_5_to_6(766, -0.4F),
    V1_20_3_to_4(765, -0.4F),
    V1_20_2(764, -0.4F),
    V1_20_1(763, 0F),
    V1_19_4(762, 0F);

    private final int version;
    private final float offset;

    ProtocolVersionOffset(int version, float offset) {
        this.version = version;
        this.offset = offset;
    }

    public float getOffset() {
        return offset;
    }

    public int getVersion() {
        return version;
    }

    public static float getOffsetByVersion(int version) {
        for (ProtocolVersionOffset offset : values()) {
            if (offset.getVersion() == version) {
                return offset.getOffset();
            }
        }
        return 0F;
    }
}
