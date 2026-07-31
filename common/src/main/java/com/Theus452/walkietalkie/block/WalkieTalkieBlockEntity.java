package com.Theus452.walkietalkie.block;

import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.sound.ModSounds;
import com.Theus452.walkietalkie.util.SafeNbt;
import com.Theus452.walkietalkie.util.WalkieFrequency;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalkieTalkieBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger("WalkieTalkie-BlockEntity");

    private static final String NBT_FREQUENCY = "frequency";
    private static final String NBT_ACTIVE = "active";
    private static final String NBT_OWNER = "owner";
    private static final String NBT_REPEATER = "isRepeater";
    private static final String NBT_RELAY_ENABLED = "relayEnabled";
    private static final String NBT_CHANNEL_NAME = "channelName";

    private static final double FALLBACK_BLOCK_RANGE = 16.0D;

    private String frequency = "";
    private String channelName = "";
    private boolean active = false;
    private boolean registeredInNetwork = false;
    private ResourceKey<Level> registeredDimension = null;
    public UUID ownerUUID;
    public long lastMessageTick = -1L;
    private int decoyTicks = 0;
    private boolean repeater = false;
    private boolean relayEnabled = true;

    public WalkieTalkieBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WALKIE_TALKIE_BLOCK_ENTITY.get(), pos, state);
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        String sanitized = WalkieFrequency.sanitize(frequency);
        if (WalkieFrequency.isPrivate(sanitized)) {
            sanitized = "";
        }
        if (this.frequency.equals(sanitized)) {
            return;
        }
        unregisterIfNeeded();
        this.frequency = sanitized;
        this.channelName = "";
        registerIfNeeded();
        markDirtyAndSync();
    }

    public String getChannelName() {
        return channelName == null ? "" : channelName;
    }

    public void setChannelName(String name) {
        String sanitized = WalkieFrequency.sanitizeChannelName(name);
        if (sanitized.equals(this.channelName)) {
            return;
        }
        this.channelName = sanitized;
        markDirtyAndSync();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        if (!active) {
            unregisterIfNeeded();
        }
        this.active = active;
        if (active) {
            registerIfNeeded();
        }
        markDirtyAndSync();
    }

    @Override
    public void setRemoved() {
        unregisterIfNeeded();
        super.setRemoved();
    }

    public void markMessageReceived() {
        this.lastMessageTick = level == null ? -1L : level.getGameTime();
        setChanged();
    }

    public void playReceiveSound() {
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.WALKIE_TALKIE_MSG_RECEIVER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        try {
            super.load(nbt);
            this.frequency = WalkieFrequency.sanitize(SafeNbt.string(nbt, NBT_FREQUENCY, "", WalkieFrequency.MAX_WIRE_LENGTH));
            if (WalkieFrequency.isPrivate(this.frequency)) {
                this.frequency = "";
            }
            this.channelName = WalkieFrequency.sanitizeChannelName(SafeNbt.string(nbt, NBT_CHANNEL_NAME, "", 20));
            this.active = SafeNbt.bool(nbt, NBT_ACTIVE, false);
            this.ownerUUID = SafeNbt.uuidOrNull(nbt, NBT_OWNER);
            this.repeater = SafeNbt.bool(nbt, NBT_REPEATER, false);
            this.relayEnabled = SafeNbt.bool(nbt, NBT_RELAY_ENABLED, true);
        } catch (RuntimeException exception) {
            LOGGER.debug("Recovered Walkie block entity from malformed NBT at {}.", worldPosition, exception);
            this.frequency = "";
            this.channelName = "";
            this.active = false;
            this.ownerUUID = null;
            this.repeater = false;
            this.relayEnabled = true;
            this.registeredInNetwork = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString(NBT_FREQUENCY, frequency);
        if (channelName != null && !channelName.isEmpty()) {
            nbt.putString(NBT_CHANNEL_NAME, channelName);
        }
        nbt.putBoolean(NBT_ACTIVE, active);
        if (ownerUUID != null) {
            nbt.putUUID(NBT_OWNER, ownerUUID);
        }
        nbt.putBoolean(NBT_REPEATER, repeater);
        nbt.putBoolean(NBT_RELAY_ENABLED, relayEnabled);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WalkieTalkieBlockEntity blockEntity) {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel)) return;
        blockEntity.registerIfNeeded();
        if (blockEntity.decoyTicks > 0) {
            blockEntity.decoyTicks--;
            if (blockEntity.decoyTicks == 0) {
                blockEntity.setChanged();
            }
        }
    }

    public List<ServerPlayer> getNearbyPlayers() {
        if (!(level instanceof ServerLevel serverLevel)) return List.of();
        double rangeSq = FALLBACK_BLOCK_RANGE * FALLBACK_BLOCK_RANGE;
        List<ServerPlayer> nearby = new ArrayList<>();
        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.5D;
        double z = worldPosition.getZ() + 0.5D;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(x, y, z) <= rangeSq) {
                nearby.add(player);
            }
        }
        return nearby;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        markDirtyAndSync();
    }

    public boolean isRelayEnabled() {
        return relayEnabled;
    }

    public void setRelayEnabled(boolean relayEnabled) {
        if (this.relayEnabled == relayEnabled) return;
        this.relayEnabled = relayEnabled;
        markDirtyAndSync();
    }

    public void setRepeater(boolean repeater) {
        if (this.repeater == repeater) return;
        this.repeater = repeater;
        markDirtyAndSync();
    }

    public boolean isRepeater() {
        return repeater;
    }

    public boolean isDecoyActive() {
        return decoyTicks > 0;
    }

    public void activateDecoy() {
        this.decoyTicks = 200;
        markDirtyAndSync();
    }

    private void registerIfNeeded() {
        if (registeredInNetwork || !active || frequency.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        WalkieBlockRegistry.register(serverLevel, worldPosition, frequency);
        registeredInNetwork = true;
        registeredDimension = serverLevel.dimension();
    }

    private void unregisterIfNeeded() {
        if (!registeredInNetwork) {
            registeredDimension = null;
            return;
        }
        if (!frequency.isEmpty()) {
            if (level instanceof ServerLevel serverLevel) {
                WalkieBlockRegistry.unregister(serverLevel, worldPosition, frequency);
            } else if (registeredDimension != null) {
                WalkieBlockRegistry.unregister(registeredDimension, worldPosition, frequency);
            }
        }
        registeredInNetwork = false;
        registeredDimension = null;
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
