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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WalkieBlockRegistry {
    private static final Map<String, Set<GlobalPos>> BY_FREQUENCY = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Set<GlobalPos>>> BY_OWNER = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Set<GlobalPos>>> CONNECTED_PLAYERS = new ConcurrentHashMap<>();
    private static final Map<String, Long> REVOCATIONS = new ConcurrentHashMap<>();

    private WalkieBlockRegistry() {
    }

    public static void registerConnectedPlayer(UUID player, String frequency, BlockPos pos, Level level) {
        if (player == null || frequency == null || frequency.isEmpty() || pos == null || level == null) return;
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        CONNECTED_PLAYERS.computeIfAbsent(player, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(safeFrequency, key -> ConcurrentHashMap.newKeySet())
                .add(globalPos);
    }

    public static void unregisterConnectedPlayer(UUID player, String frequency, BlockPos pos, Level level) {
        if (player == null || frequency == null || frequency.isEmpty() || pos == null || level == null) return;
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        Map<String, Set<GlobalPos>> freqs = CONNECTED_PLAYERS.get(player);
        if (freqs == null) return;
        Set<GlobalPos> blocks = freqs.get(safeFrequency);
        if (blocks != null) {
            blocks.remove(globalPos);
            if (blocks.isEmpty()) {
                freqs.remove(safeFrequency);
            }
        }
        if (freqs.isEmpty()) {
            CONNECTED_PLAYERS.remove(player);
        }
    }

    public static boolean isPlayerConnectedToAnyBlock(UUID player, String frequency) {
        if (player == null || frequency == null || frequency.isEmpty()) return false;
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        Map<String, Set<GlobalPos>> freqs = CONNECTED_PLAYERS.get(player);
        if (freqs == null) return false;
        Set<GlobalPos> blocks = freqs.get(safeFrequency);
        return blocks != null && !blocks.isEmpty();
    }

    public static void addConnectedFrequencies(UUID player, Set<String> destination) {
        if (player == null || destination == null) return;
        Map<String, Set<GlobalPos>> freqs = CONNECTED_PLAYERS.get(player);
        if (freqs != null) {
            destination.addAll(freqs.keySet());
        }
    }

    public static void register(ServerLevel level, BlockPos pos, String frequency, UUID owner) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (level == null || pos == null || safeFrequency.isEmpty()) return;
        if (owner != null && isRevoked(owner, safeFrequency)) {
            return;
        }
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        BY_FREQUENCY.computeIfAbsent(safeFrequency, key -> ConcurrentHashMap.newKeySet()).add(globalPos);
        if (owner != null) {
            BY_OWNER.computeIfAbsent(owner, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(safeFrequency, key -> ConcurrentHashMap.newKeySet())
                .add(globalPos);
        }
    }

    public static void unregister(ServerLevel level, BlockPos pos, String frequency, UUID owner) {
        if (level == null) return;
        unregister(level.dimension(), pos, frequency, owner);
    }

    public static void unregister(ResourceKey<Level> dimension, BlockPos pos, String frequency, UUID owner) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (dimension == null || pos == null || safeFrequency.isEmpty()) return;
        GlobalPos globalPos = GlobalPos.of(dimension, pos);
        Set<GlobalPos> set = BY_FREQUENCY.get(safeFrequency);
        if (set != null) {
            set.remove(globalPos);
            if (set.isEmpty()) {
                BY_FREQUENCY.remove(safeFrequency);
            }
        }
        if (owner != null) {
            Map<String, Set<GlobalPos>> ownerFrequencies = BY_OWNER.get(owner);
            if (ownerFrequencies == null) return;
            Set<GlobalPos> ownerBlocks = ownerFrequencies.get(safeFrequency);
            if (ownerBlocks != null) {
                ownerBlocks.remove(globalPos);
                if (ownerBlocks.isEmpty()) {
                    ownerFrequencies.remove(safeFrequency);
                }
            }
            if (ownerFrequencies.isEmpty()) {
                BY_OWNER.remove(owner);
            }
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
        Set<GlobalPos> set = BY_FREQUENCY.get(safeFrequency);
        return set == null ? 0 : set.size();
    }

    public static boolean hasFrequency(String frequency) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (safeFrequency.isEmpty()) return false;
        Set<GlobalPos> set = BY_FREQUENCY.get(safeFrequency);
        return set != null && !set.isEmpty();
    }

    public static int getOwnedBlockCount(UUID owner, String frequency) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (owner == null || safeFrequency.isEmpty()) return 0;
        Map<String, Set<GlobalPos>> frequencies = BY_OWNER.get(owner);
        if (frequencies == null) return 0;
        Set<GlobalPos> blocks = frequencies.get(safeFrequency);
        return blocks == null ? 0 : blocks.size();
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

    public static void addOwnedFrequencies(UUID owner, Set<String> destination) {
        if (owner == null || destination == null) return;
        Map<String, Set<GlobalPos>> frequencies = BY_OWNER.get(owner);
        if (frequencies != null) destination.addAll(frequencies.keySet());
    }

    public static boolean hasOwnedFrequency(UUID owner, String frequency) {
        if (owner == null || frequency == null || frequency.isEmpty()) return false;
        Map<String, Set<GlobalPos>> frequencies = BY_OWNER.get(owner);
        return frequencies != null && frequencies.containsKey(frequency);
    }

    public static void disconnectAllForOwner(MinecraftServer server, UUID owner, String frequency) {
        String safeFrequency = WalkieFrequency.sanitize(frequency);
        if (owner == null || safeFrequency.isEmpty()) return;
        REVOCATIONS.put(owner + ":" + safeFrequency, System.currentTimeMillis());
        Map<String, Set<GlobalPos>> ownerFrequencies = BY_OWNER.get(owner);
        if (ownerFrequencies != null) {
            Set<GlobalPos> blocks = ownerFrequencies.remove(safeFrequency);
            if (blocks != null) {
                for (GlobalPos globalPos : blocks) {
                    Set<GlobalPos> freqSet = BY_FREQUENCY.get(safeFrequency);
                    if (freqSet != null) {
                        freqSet.remove(globalPos);
                        if (freqSet.isEmpty()) {
                            BY_FREQUENCY.remove(safeFrequency);
                        }
                    }
                    if (server != null) {
                        ServerLevel level = server.getLevel(globalPos.dimension());
                        if (level != null) {
                            BlockEntity be = level.getBlockEntity(globalPos.pos());
                            if (be instanceof WalkieTalkieBlockEntity walkie && safeFrequency.equals(walkie.getFrequency())) {
                                walkie.setFrequency("");
                                walkie.setChannelName("");
                            }
                        }
                    }
                }
            }
            if (ownerFrequencies.isEmpty()) {
                BY_OWNER.remove(owner);
            }
        }
    }

    public static boolean isRevoked(UUID owner, String frequency) {
        if (owner == null || frequency == null || frequency.isEmpty()) return false;
        return REVOCATIONS.containsKey(owner + ":" + WalkieFrequency.sanitize(frequency));
    }

    public static void clearRevocation(UUID owner, String frequency) {
        if (owner == null || frequency == null || frequency.isEmpty()) return;
        REVOCATIONS.remove(owner + ":" + WalkieFrequency.sanitize(frequency));
    }

    public static void clearOnStop() {
        BY_FREQUENCY.clear();
        BY_OWNER.clear();
        CONNECTED_PLAYERS.clear();
        REVOCATIONS.clear();
    }

    private static boolean isActiveBlock(ServerLevel level, BlockPos pos, String frequency) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof WalkieTalkieBlockEntity walkie
            && walkie.isActive()
            && frequency.equals(walkie.getFrequency());
    }
}
