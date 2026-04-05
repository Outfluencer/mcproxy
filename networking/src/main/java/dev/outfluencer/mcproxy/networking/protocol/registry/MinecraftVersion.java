package dev.outfluencer.mcproxy.networking.protocol.registry;

import java.util.List;

public final class MinecraftVersion {

    private MinecraftVersion() {}

    public static final int V1_7_2 = 4;
    public static final int V1_7_10 = 5;
    public static final int V1_8 = 47;
    public static final int V1_9 = 107;
    public static final int V1_9_1 = 108;
    public static final int V1_9_2 = 109;
    public static final int V1_9_4 = 110;
    public static final int V1_10 = 210;
    public static final int V1_11 = 315;
    public static final int V1_11_1 = 316;
    public static final int V1_12 = 335;
    public static final int V1_12_1 = 338;
    public static final int V1_12_2 = 340;
    public static final int V1_13 = 393;
    public static final int V1_13_1 = 401;
    public static final int V1_13_2 = 404;
    public static final int V1_14 = 477;
    public static final int V1_14_1 = 480;
    public static final int V1_14_2 = 485;
    public static final int V1_14_3 = 490;
    public static final int V1_14_4 = 498;
    public static final int V1_15 = 573;
    public static final int V1_15_1 = 575;
    public static final int V1_15_2 = 578;
    public static final int V1_16 = 735;
    public static final int V1_16_1 = 736;
    public static final int V1_16_2 = 751;
    public static final int V1_16_3 = 753;
    public static final int V1_16_4 = 754;
    public static final int V1_17 = 755;
    public static final int V1_17_1 = 756;
    public static final int V1_18 = 757;
    public static final int V1_18_2 = 758;
    public static final int V1_19 = 759;
    public static final int V1_19_1 = 760;
    public static final int V1_19_3 = 761;
    public static final int V1_19_4 = 762;
    public static final int V1_20 = 763;
    public static final int V1_20_2 = 764;
    public static final int V1_20_3 = 765;
    public static final int V1_20_5 = 766;
    public static final int V1_21 = 767;
    public static final int V1_21_2 = 768;
    public static final int V1_21_4 = 769;
    public static final int V1_21_5 = 770;
    public static final int V1_21_6 = 771;
    public static final int V1_21_7 = 772;
    public static final int V1_21_9 = 773;
    public static final int V1_21_11 = 774;
    public static final int V26_1 = 775;
    public static final List<Integer> SUPPORTED_VERSION = List.of(V1_21_11, V26_1);

    public static boolean isSupported(int version) {
        return SUPPORTED_VERSION.contains(version);
    }

}
