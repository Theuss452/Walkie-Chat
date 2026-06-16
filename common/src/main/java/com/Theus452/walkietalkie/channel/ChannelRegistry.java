package com.Theus452.walkietalkie.channel;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ChannelRegistry extends SavedData {
    private static final Logger LOGGER = LoggerFactory.getLogger("WalkieChat-Channels");
    private static final String DATA_NAME = "walkietalkie_channels";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, ChannelDefinition> channels = new LinkedHashMap<>();

    public static ChannelRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ChannelRegistry::load, ChannelRegistry::new, DATA_NAME);
    }

    public static ChannelRegistry load(CompoundTag root) {
        ChannelRegistry registry = new ChannelRegistry();
        ListTag channelTags = root.getList("Channels", Tag.TAG_COMPOUND);
        for (int i = 0; i < channelTags.size(); i++) {
            CompoundTag tag = channelTags.getCompound(i);
            try {
                String frequency = tag.getString("Frequency");
                String name = tag.getString("Name");
                UUID owner = tag.getUUID("Owner");
                String ownerName = tag.getString("OwnerName");
                boolean passwordProtected = tag.getBoolean("PasswordProtected");
                String salt = tag.getString("Salt");
                String passwordHash = tag.getString("PasswordHash");
                if (ChannelManager.isValidFrequency(frequency)
                        && ChannelManager.isValidName(name)
                        && isValidPasswordData(passwordProtected, salt, passwordHash)) {
                    registry.channels.put(frequency, new ChannelDefinition(
                            frequency,
                            name,
                            owner,
                            ownerName,
                            passwordProtected,
                            salt,
                            passwordHash
                    ));
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("Ignoring invalid Walkie-Chat channel data at index {}", i, exception);
            }
        }
        return registry;
    }

    public ChannelDefinition getChannel(String frequency) {
        return channels.get(frequency);
    }

    public Collection<ChannelDefinition> getChannels() {
        return new ArrayList<>(channels.values());
    }

    public ChannelDefinition create(String frequency, String name, UUID owner, String ownerName, boolean passwordProtected, String password) {
        if (channels.containsKey(frequency)) {
            return null;
        }
        String salt = "";
        String passwordHash = "";
        if (passwordProtected) {
            byte[] saltBytes = new byte[16];
            RANDOM.nextBytes(saltBytes);
            salt = Base64.getEncoder().encodeToString(saltBytes);
            passwordHash = hashPassword(salt, password);
        }
        ChannelDefinition definition = new ChannelDefinition(
                frequency,
                name,
                owner,
                ownerName,
                passwordProtected,
                salt,
                passwordHash
        );
        channels.put(frequency, definition);
        setDirty();
        return definition;
    }

    public ChannelDefinition remove(String frequency) {
        ChannelDefinition removed = channels.remove(frequency);
        if (removed != null) {
            setDirty();
        }
        return removed;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag channelTags = new ListTag();
        for (ChannelDefinition definition : channels.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Frequency", definition.frequency());
            tag.putString("Name", definition.name());
            tag.putUUID("Owner", definition.owner());
            tag.putString("OwnerName", definition.ownerName());
            tag.putBoolean("PasswordProtected", definition.passwordProtected());
            tag.putString("Salt", definition.salt());
            tag.putString("PasswordHash", definition.passwordHash());
            channelTags.add(tag);
        }
        root.put("Channels", channelTags);
        return root;
    }

    private static String hashPassword(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            return Base64.getEncoder().encodeToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static boolean isValidPasswordData(boolean passwordProtected, String salt, String passwordHash) {
        if (!passwordProtected) {
            return salt.isEmpty() && passwordHash.isEmpty();
        }
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        byte[] hashBytes = Base64.getDecoder().decode(passwordHash);
        return saltBytes.length == 16 && hashBytes.length == 32;
    }

    public record ChannelDefinition(
            String frequency,
            String name,
            UUID owner,
            String ownerName,
            boolean passwordProtected,
            String salt,
            String passwordHash
    ) {
        public boolean matchesPassword(String password) {
            if (!passwordProtected) {
                return true;
            }
            byte[] expected = Base64.getDecoder().decode(passwordHash);
            byte[] actual = Base64.getDecoder().decode(hashPassword(salt, password));
            return MessageDigest.isEqual(expected, actual);
        }
    }
}
