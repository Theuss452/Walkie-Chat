package com.Theus452.walkietalkie.signal;

public enum SignalStrength {

    EXCELLENT("signal.walkietalkie.excellent", 0xFF00FF55, 0.00f, 0.00f),
    GOOD("signal.walkietalkie.good", 0xFF7FFF00, 0.00f, 0.04f),
    MODERATE("signal.walkietalkie.moderate", 0xFFFFDD00, 0.08f, 0.14f),
    POOR("signal.walkietalkie.poor", 0xFFFF8800, 0.28f, 0.32f),
    CRITICAL("signal.walkietalkie.critical", 0xFFFF3300, 0.60f, 0.55f),
    NO_SIGNAL("signal.walkietalkie.no_signal", 0xFF666666, 1.00f, 0.00f);

    public final String langKey;
    public final int color;
    public final float dropChance;
    public final float corruptionRate;

    private static final double DISTANCE_SCALE = 380.0;
    private static final double DISTANCE_CURVE = 1.18;

    private static final double QUAL_EXCELLENT = 0.82;
    private static final double QUAL_GOOD = 0.64;
    private static final double QUAL_MODERATE = 0.44;
    private static final double QUAL_POOR = 0.22;

    SignalStrength(String langKey, int color, float dropChance, float corruptionRate) {
        this.langKey = langKey;
        this.color = color;
        this.dropChance = dropChance;
        this.corruptionRate = corruptionRate;
    }

    public int barCount() {
        return switch (this) {
            case EXCELLENT -> 4;
            case GOOD -> 3;
            case MODERATE -> 2;
            case POOR -> 1;
            case CRITICAL -> 0;
            case NO_SIGNAL -> 0;
        };
    }

    public static SignalStrength fromDistance(double blocks) {
        if (blocks < 0) return NO_SIGNAL;
        return fromQuality(qualityFromDistance(blocks));
    }

    public static double qualityFromDistance(double blocks) {
        if (blocks < 0) {
            return 0.0;
        }
        double normalized = Math.max(0.0, blocks) / DISTANCE_SCALE;
        return Math.exp(-Math.pow(normalized, DISTANCE_CURVE));
    }

    public static SignalStrength fromQuality(double quality) {
        double q = clamp01(quality);

        if (q < 0.045) return NO_SIGNAL;
        if (q >= QUAL_EXCELLENT) return EXCELLENT;
        if (q >= QUAL_GOOD) return GOOD;
        if (q >= QUAL_MODERATE) return MODERATE;
        if (q >= QUAL_POOR) return POOR;
        return CRITICAL;
    }

    public static float dropChanceFromQuality(double quality) {
        double q = clamp01(quality);
        if (q >= 0.78) return 0.0f;
        if (q < 0.045) return 1.0f;
        double v = Math.pow((0.78 - q) / 0.735, 1.55);
        return (float) Math.max(0.0, Math.min(1.0, v));
    }

    public static float corruptionRateFromQuality(double quality) {
        double q = clamp01(quality);
        if (q >= 0.86) return 0.0f;
        if (q < 0.045) return 1.0f;
        double v = Math.pow((0.86 - q) / 0.815, 1.30);
        return (float) Math.max(0.0, Math.min(1.0, v));
    }

    private static double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    public SignalStrength downgrade(int levels) {
        SignalStrength[] vals = values();
        return vals[Math.min(vals.length - 1, ordinal() + levels)];
    }

    public byte toByte() {
        return (byte) ordinal();
    }

    public static SignalStrength fromByte(byte b) {
        SignalStrength[] v = values();
        int i = b & 0xFF;
        return i < v.length ? v[i] : NO_SIGNAL;
    }

    public String chatColor() {
        return switch (this) {
            case EXCELLENT -> "\u00A7a";
            case GOOD -> "\u00A72";
            case MODERATE -> "\u00A7e";
            case POOR -> "\u00A76";
            case CRITICAL -> "\u00A7c";
            case NO_SIGNAL -> "\u00A78";
        };
    }

    public static String translationKeyForSignal(SignalStrength signal) {
        if (signal == null) {
            return "gui.walkietalkie.signal.no_signal";
        }
        return switch (signal) {
            case EXCELLENT -> "gui.walkietalkie.signal.excellent";
            case GOOD -> "gui.walkietalkie.signal.good";
            case MODERATE -> "gui.walkietalkie.signal.moderate";
            case POOR -> "gui.walkietalkie.signal.poor";
            case CRITICAL -> "gui.walkietalkie.signal.critical";
            case NO_SIGNAL -> "gui.walkietalkie.signal.no_signal";
        };
    }
}
