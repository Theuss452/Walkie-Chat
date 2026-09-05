package com.Theus452.walkietalkie.compat;

import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class AttractToChatCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("walkietalkie-attract-to-chat");
    private static final String MOD_ID = "attracttochat";
    private static Method vocalMuteMethod;
    private static Method proximityRangeMethod;
    private static Constructor<?> messageScoreConstructor;
    private static Method blockAttractionMethod;
    private static Method configValueGetMethod;
    private static Object hearingRangeValue;
    private static boolean vocalMuteLookupComplete;
    private static boolean proximityRangeLookupComplete;
    private static boolean blockAttractionLookupComplete;
    private static boolean vocalMuteFailureReported;
    private static boolean proximityRangeFailureReported;
    private static boolean blockAttractionFailureReported;

    private AttractToChatCompat() {
    }

    public static boolean isVocallyMuted(ServerPlayer player) {
        if (player == null || !isAvailable()) {
            return false;
        }
        Method method = getVocalMuteMethod();
        if (method == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(method.invoke(null, player.getUUID()));
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportVocalMuteFailure(exception);
            return false;
        }
    }

    public static double getEffectiveProximityRange(String message, double fallbackRange) {
        if (!isAvailable()) {
            return fallbackRange;
        }
        Method method = getProximityRangeMethod();
        if (method == null) {
            return fallbackRange;
        }
        try {
            Object result = method.invoke(null, message, fallbackRange);
            if (result instanceof Number number) {
                double range = number.doubleValue();
                if (Double.isFinite(range) && range >= 0.0D) {
                    return range;
                }
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportProximityRangeFailure(exception);
        }
        return fallbackRange;
    }

    public static void attractMobsAtBlock(ServerPlayer sender, ServerLevel level, BlockPos position,
            String message) {
        if (sender == null || level == null || position == null || message == null || message.isBlank()
                || !isAvailable() || !resolveBlockAttractionHook()) {
            return;
        }
        try {
            double range = 16.0D;
            if (configValueGetMethod != null && hearingRangeValue != null) {
                try {
                    Object rangeValue = configValueGetMethod.invoke(hearingRangeValue);
                    if (rangeValue instanceof Number number) {
                        double customRange = number.doubleValue();
                        if (Double.isFinite(customRange) && customRange >= 0.0D) {
                            range = customRange;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            Object score = messageScoreConstructor.newInstance(message, sender.getUUID());
            blockAttractionMethod.invoke(null, level, position, range, score);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportBlockAttractionFailure(exception);
        }
    }

    private static boolean isAvailable() {
        return Platform.getHelper().isModLoaded(MOD_ID);
    }

    private static synchronized Method getVocalMuteMethod() {
        if (vocalMuteLookupComplete) {
            return vocalMuteMethod;
        }
        vocalMuteLookupComplete = true;
        try {
            Class<?> engineClass = Class.forName("com.bielzinrx.attracttochat.engine.AtcEngine");
            vocalMuteMethod = engineClass.getMethod("isVocallyMuted", java.util.UUID.class);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportVocalMuteFailure(exception);
        }
        return vocalMuteMethod;
    }

    private static synchronized Method getProximityRangeMethod() {
        if (proximityRangeLookupComplete) {
            return proximityRangeMethod;
        }
        proximityRangeLookupComplete = true;
        try {
            Class<?> attractToChatClass = Class.forName("com.bielzinrx.attracttochat.AttractToChat");
            proximityRangeMethod = attractToChatClass.getMethod("getEffectiveWalkieProximityRange", String.class, double.class);
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportProximityRangeFailure(exception);
        }
        return proximityRangeMethod;
    }

    private static synchronized boolean resolveBlockAttractionHook() {
        if (blockAttractionLookupComplete) {
            return blockAttractionMethod != null && messageScoreConstructor != null;
        }
        blockAttractionLookupComplete = true;
        try {
            Class<?> engineClass = Class.forName("com.bielzinrx.attracttochat.engine.AtcEngine");
            Class<?> scoreClass = Class.forName("com.bielzinrx.attracttochat.engine.MessageScore");
            messageScoreConstructor = scoreClass.getConstructor(String.class, java.util.UUID.class);
            blockAttractionMethod = engineClass.getMethod("attractMobsAtPosition",
                    ServerLevel.class, BlockPos.class, double.class, scoreClass);

            try {
                Class<?> configClass = Class.forName("com.bielzinrx.attracttochat.config.AttractToChatConfig");
                Field commonField = configClass.getField("COMMON");
                Object commonConfig = commonField.get(null);
                if (commonConfig != null) {
                    Field hearingRangeField = commonConfig.getClass().getField("hearingRange");
                    hearingRangeValue = hearingRangeField.get(commonConfig);
                    if (hearingRangeValue != null) {
                        configValueGetMethod = hearingRangeValue.getClass().getMethod("get");
                    }
                }
            } catch (Exception ignored) {
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            reportBlockAttractionFailure(exception);
        }
        return blockAttractionMethod != null && messageScoreConstructor != null;
    }

    private static void reportVocalMuteFailure(Throwable exception) {
        if (!vocalMuteFailureReported) {
            vocalMuteFailureReported = true;
            LOGGER.warn("Attract To Chat vocal mute hook is unavailable.", exception);
        }
    }

    private static void reportProximityRangeFailure(Throwable exception) {
        if (!proximityRangeFailureReported) {
            proximityRangeFailureReported = true;
            LOGGER.warn("Attract To Chat proximity range hook is unavailable.", exception);
        }
    }

    private static void reportBlockAttractionFailure(Throwable exception) {
        if (!blockAttractionFailureReported) {
            blockAttractionFailureReported = true;
            LOGGER.warn("Attract To Chat block attraction hook is unavailable.", exception);
        }
    }
}
