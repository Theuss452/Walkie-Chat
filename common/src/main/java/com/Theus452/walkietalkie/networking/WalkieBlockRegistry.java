package com.Theus452.walkietalkie.networking;

import com.Theus452.walkietalkie.block.WalkieTalkieBlockEntity;
import com.Theus452.walkietalkie.util.WalkieFrequency;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WalkieBlockRegistry {
    private static final Map<String, Set<GlobalPos>> BY_FREQUENCY = new ConcurrentHashMap<>();

    private WalkieBlockRegistry() {
    }

    public static void register(ServerLevel level, BlockPos pos, String frequency) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (level == null || pos == null || safeFrequency.isEmpty()) return;
        BY_FREQUENCY.computeIfAbsent(safeFrequency, key -> ConcurrentHashMap.newKeySet())
            .add(GlobalPos.of(level.dimension(), pos));
    }

    public static void unregister(ServerLevel level, BlockPos pos, String frequency) {
        if (level == null) return;
        unregister(level.dimension(), pos, frequency);
    }

    public static void unregister(ResourceKey<Level> dimension, BlockPos pos, String frequency) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (dimension == null || pos == null || safeFrequency.isEmpty()) return;
        Set<GlobalPos> set = BY_FREQUENCY.get(safeFrequency);
        if (set == null) return;
        set.remove(GlobalPos.of(dimension, pos));
        if (set.isEmpty()) {
            BY_FREQUENCY.remove(safeFrequency);
        }
    }

    public static List<GlobalPos> getBlocksNear(String frequency, ServerLevel level, BlockPos senderPos, double radius) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (level == null || senderPos == null || radius <= 0.0D || safeFrequency.isEmpty()) {
            return List.of();
        }
        Set<GlobalPos> set = BY_FREQUENCY.getOrDefault(safeFrequency, Set.of());
        List<GlobalPos> nearby = new ArrayList<>();
        double radiusSq = radius * radius;
        for (GlobalPos globalPos : set) {
            if (globalPos.dimension().equals(level.dimension())
                    && globalPos.pos().distSqr(senderPos) <= radiusSq
                    && isActiveBlock(level, globalPos.pos(), safeFrequency)) {
                nearby.add(globalPos);
            }
        }
        return nearby;
    }

    public static List<GlobalPos> getBlocksByRange(ServerLevel level, BlockPos pos, double radius) {
        if (level == null || pos == null || radius <= 0.0D) return List.of();
        LinkedHashSet<GlobalPos> result = new LinkedHashSet<>();
        double radiusSq = radius * radius;
        for (Map.Entry<String, Set<GlobalPos>> entry : BY_FREQUENCY.entrySet()) {
            String frequency = entry.getKey();
            for (GlobalPos globalPos : entry.getValue()) {
                if (globalPos.dimension().equals(level.dimension())
                        && globalPos.pos().distSqr(pos) <= radiusSq
                        && isActiveBlock(level, globalPos.pos(), frequency)) {
                    result.add(globalPos);
                }
            }
        }
        return new ArrayList<>(result);
    }

    public static int getBlockCount(String frequency) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (safeFrequency.isEmpty()) return 0;
        Set<GlobalPos> set = BY_FREQUENCY.get(safeFrequency);
        return set == null ? 0 : set.size();
    }

    public static List<GlobalPos> getBlocks(String frequency, MinecraftServer server) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (server == null || safeFrequency.isEmpty()) return List.of();
        Set<GlobalPos> set = BY_FREQUENCY.getOrDefault(safeFrequency, Set.of());
        List<GlobalPos> result = new ArrayList<>();
        for (GlobalPos globalPos : set) {
            ServerLevel level = server.getLevel(globalPos.dimension());
            if (level != null && isActiveBlock(level, globalPos.pos(), safeFrequency)) {
                result.add(globalPos);
            }
        }
        return result;
    }

    public static Set<String> getActiveFrequencies() {
        return Set.copyOf(BY_FREQUENCY.keySet());
    }

    public static void clearOnStop() {
        BY_FREQUENCY.clear();
    }

    private static boolean isActiveBlock(ServerLevel level, BlockPos pos, String frequency) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof WalkieTalkieBlockEntity walkie
            && walkie.isActive()
            && frequency.equals(walkie.getFrequency());
    }
}
