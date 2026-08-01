package com.Theus452.walkietalkie.client;

import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ClientWalkieItemCache {
    private static final long FALLBACK_SCAN_INTERVAL_TICKS = 5L;

    private static UUID cachedPlayerId;
    private static long lastFallbackScanTick = Long.MIN_VALUE;
    private static ItemStack cachedFallback = ItemStack.EMPTY;

    private ClientWalkieItemCache() {
    }

    public static ItemStack visibleStack(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            clear();
            return ItemStack.EMPTY;
        }

        UUID playerId = minecraft.player.getUUID();
        if (!playerId.equals(cachedPlayerId)) {
            cachedPlayerId = playerId;
            lastFallbackScanTick = Long.MIN_VALUE;
            cachedFallback = ItemStack.EMPTY;
        }

        ItemStack main = minecraft.player.getMainHandItem();
        ItemStack offhand = minecraft.player.getOffhandItem();

        if (isWalkieActive(main)) return main;
        if (isWalkieActive(offhand)) return offhand;

        long gameTime = minecraft.level.getGameTime();
        if (gameTime - lastFallbackScanTick >= FALLBACK_SCAN_INTERVAL_TICKS) {
            lastFallbackScanTick = gameTime;
            cachedFallback = scanFallback(minecraft, main, offhand);
        }

        if (!cachedFallback.isEmpty()) return cachedFallback;
        if (isWalkie(main)) return main;
        if (isWalkie(offhand)) return offhand;
        return ItemStack.EMPTY;
    }

    public static void clear() {
        cachedPlayerId = null;
        lastFallbackScanTick = Long.MIN_VALUE;
        cachedFallback = ItemStack.EMPTY;
    }

    private static ItemStack scanFallback(Minecraft minecraft, ItemStack main, ItemStack offhand) {
        ItemStack fallback = ItemStack.EMPTY;
        for (ItemStack candidate : minecraft.player.getInventory().items) {
            if (!isWalkie(candidate)) continue;
            if (isWalkieActive(candidate)) return candidate;
            if (fallback.isEmpty()) fallback = candidate;
        }
        if (isWalkie(main)) return main;
        if (isWalkie(offhand)) return offhand;
        return fallback;
    }

    private static boolean isWalkie(ItemStack stack) {
        return stack != null && stack.getItem() instanceof WalkieTalkieItem;
    }

    private static boolean isWalkieActive(ItemStack stack) {
        return isWalkie(stack) && !WalkieTalkieItem.getFrequency(stack).isEmpty();
    }
}
