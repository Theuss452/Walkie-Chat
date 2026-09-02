package com.Theus452.walkietalkie.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
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
            if (stack == null || stack.isEmpty() || !stack.hasTag()) {
                return Optional.empty();
            }
            return Optional.ofNullable(stack.getTag());
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to read existing item NBT.", exception);
            return Optional.empty();
        }
    }

    public static Optional<CompoundTag> writable(ItemStack stack) {
        try {
            if (stack == null || stack.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(stack.getOrCreateTag());
        } catch (RuntimeException exception) {
            LOGGER.debug("Failed to create writable item NBT.", exception);
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
        writable(stack).ifPresent(tag -> {
            String safe = value == null ? "" : value;
            if (maxLength > 0 && safe.length() > maxLength) {
                safe = safe.substring(0, maxLength);
            }
            tag.putString(key, safe);
        });
    }

    public static void putBoolean(ItemStack stack, String key, boolean value) {
        writable(stack).ifPresent(tag -> tag.putBoolean(key, value));
    }

    public static void putLong(ItemStack stack, String key, long value) {
        writable(stack).ifPresent(tag -> tag.putLong(key, value));
    }

    public static void putUuid(ItemStack stack, String key, UUID uuid) {
        writable(stack).ifPresent(tag -> {
            if (uuid == null) {
                tag.remove(key);
            } else {
                tag.putUUID(key, uuid);
            }
        });
    }
}
