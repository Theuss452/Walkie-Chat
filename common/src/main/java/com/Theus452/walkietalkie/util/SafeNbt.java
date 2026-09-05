package com.Theus452.walkietalkie.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public final class SafeNbt {
    private static final Logger LOGGER = LoggerFactory.getLogger("WalkieTalkie-SafeNbt");

    private SafeNbt() {
    }

    public static Optional<CompoundTag> existing(ItemStack stack) {
        try {
            if (stack == null || stack.isEmpty()) {
                return Optional.empty();
            }
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null || customData.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(customData.copyTag());
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to read existing item NBT.", exception);
            return Optional.empty();
        }
    }

    public static String string(CompoundTag tag, String key, String fallback, int maxLength) {
        try {
            if (tag == null || key == null || !tag.contains(key)) {
                return fallback;
            }
            String value = tag.getString(key);
            if (value == null) {
                return fallback;
            }
            return maxLength > 0 && value.length() > maxLength ? value.substring(0, maxLength) : value;
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to read string NBT key '{}'.", key, exception);
            return fallback;
        }
    }

    public static boolean bool(CompoundTag tag, String key, boolean fallback) {
        try {
            if (tag == null || key == null || !tag.contains(key)) {
                return fallback;
            }
            return tag.getBoolean(key);
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to read boolean NBT key '{}'.", key, exception);
            return fallback;
        }
    }

    public static long longValue(CompoundTag tag, String key, long fallback) {
        try {
            if (tag == null || key == null || !tag.contains(key)) {
                return fallback;
            }
            return tag.getLong(key);
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to read long NBT key '{}'.", key, exception);
            return fallback;
        }
    }

    public static UUID uuidOrNull(CompoundTag tag, String key) {
        try {
            if (tag == null || key == null || !tag.hasUUID(key)) {
                return null;
            }
            return tag.getUUID(key);
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to read UUID NBT key '{}'.", key, exception);
            return null;
        }
    }

    public static void putString(ItemStack stack, String key, String value, int maxLength) {
        if (stack == null || stack.isEmpty()) return;
        try {
            String safe = value == null ? "" : value;
            if (maxLength > 0 && safe.length() > maxLength) {
                safe = safe.substring(0, maxLength);
            }
            String finalSafe = safe;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(key, finalSafe));
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to put string NBT key '{}'.", key, exception);
        }
    }

    public static void putBoolean(ItemStack stack, String key, boolean value) {
        if (stack == null || stack.isEmpty()) return;
        try {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(key, value));
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to put boolean NBT key '{}'.", key, exception);
        }
    }

    public static void putLong(ItemStack stack, String key, long value) {
        if (stack == null || stack.isEmpty()) return;
        try {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(key, value));
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to put long NBT key '{}'.", key, exception);
        }
    }

    public static void putUuid(ItemStack stack, String key, UUID uuid) {
        if (stack == null || stack.isEmpty()) return;
        try {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                if (uuid == null) {
                    tag.remove(key);
                } else {
                    tag.putUUID(key, uuid);
                }
            });
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to put UUID NBT key '{}'.", key, exception);
        }
    }
}
