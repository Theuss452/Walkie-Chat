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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.Theus452.walkietalkie.networking.WalkieBlockRegistry;
import com.Theus452.walkietalkie.util.ConnectionManager;

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
                    ChannelDefinition definition = new ChannelDefinition(
                            frequency,
                            name,
                            owner,
                            ownerName,
                            passwordProtected,
                            salt,
                            passwordHash
                    );
                    if (tag.contains("Members", Tag.TAG_LIST)) {
                        ListTag membersList = tag.getList("Members", Tag.TAG_INT_ARRAY);
                        for (int j = 0; j < membersList.size(); j++) {
                            definition.members().add(net.minecraft.nbt.NbtUtils.loadUUID(membersList.get(j)));
                        }
                    }
                    if (tag.contains("MemberNames", Tag.TAG_LIST)) {
                        ListTag namesList = tag.getList("MemberNames", Tag.TAG_STRING);
                        for (int j = 0; j < namesList.size(); j++) {
                            definition.memberNames().add(namesList.getString(j));
                        }
                    }
                    if (tag.contains("PrivateAccess", Tag.TAG_LIST)) {
                        ListTag accessList = tag.getList("PrivateAccess", Tag.TAG_INT_ARRAY);
                        for (int j = 0; j < accessList.size(); j++) {
                            definition.privateAccess().add(net.minecraft.nbt.NbtUtils.loadUUID(accessList.get(j)));
                        }
                    }
                    registry.channels.put(frequency, definition);
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
        definition.privateAccess().add(owner);
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

    public void addMember(String frequency, UUID playerId, String playerName, MinecraftServer server) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            int existingIndex = definition.members().indexOf(playerId);
            if (existingIndex != -1) {
                definition.members().remove(existingIndex);
                if (existingIndex < definition.memberNames().size()) {
                    definition.memberNames().remove(existingIndex);
                }
            }
            definition.memberNames().removeIf(name -> name.equalsIgnoreCase(playerName));
            definition.members().add(playerId);
            definition.memberNames().add(playerName);
            setDirty();
            ConnectionManager.syncActiveChannels(server);
        }
    }

    public void removeMember(String frequency, UUID playerId, MinecraftServer server) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            boolean removed = false;
            while (definition.members().contains(playerId)) {
                int index = definition.members().indexOf(playerId);
                definition.members().remove(index);
                if (index < definition.memberNames().size()) {
                    definition.memberNames().remove(index);
                }
                removed = true;
            }
            if (removed) {
                if (definition.members().isEmpty() && !WalkieBlockRegistry.hasFrequency(frequency)) {
                    channels.remove(frequency);
                } else {
                    if (playerId.equals(definition.owner()) && !definition.members().isEmpty()) {
                        UUID nextOwner = definition.members().get(0);
                        String nextOwnerName = definition.memberNames().isEmpty() ? "" : definition.memberNames().get(0);
                        definition.setOwner(nextOwner, nextOwnerName);
                        definition.privateAccess().add(nextOwner);
                    }
                }
                setDirty();
                ConnectionManager.syncActiveChannels(server);
            }
        }
    }

    public void grantAccess(UUID playerId, String frequency) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            if (definition.privateAccess().add(playerId)) {
                setDirty();
            }
        }
    }

    public void revokeAccess(UUID playerId, String frequency) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            if (definition.privateAccess().remove(playerId)) {
                setDirty();
            }
        }
    }

    public boolean hasAccess(UUID playerId, String frequency) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            return !definition.passwordProtected() || definition.privateAccess().contains(playerId);
        }
        return true;
    }

    public void revokeFrequencyAccess(String frequency) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            definition.privateAccess().clear();
            setDirty();
        }
    }

    public void renameChannel(String frequency, String newName) {
        ChannelDefinition definition = channels.get(frequency);
        if (definition != null) {
            definition.setName(newName);
            setDirty();
        }
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

            ListTag membersList = new ListTag();
            for (UUID uuid : definition.members()) {
                membersList.add(net.minecraft.nbt.NbtUtils.createUUID(uuid));
            }
            tag.put("Members", membersList);

            ListTag namesList = new ListTag();
            for (String name : definition.memberNames()) {
                namesList.add(net.minecraft.nbt.StringTag.valueOf(name));
            }
            tag.put("MemberNames", namesList);

            ListTag accessList = new ListTag();
            for (UUID uuid : definition.privateAccess()) {
                accessList.add(net.minecraft.nbt.NbtUtils.createUUID(uuid));
            }
            tag.put("PrivateAccess", accessList);

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

    public static class ChannelDefinition {
        private final String frequency;
        private String name;
        private UUID owner;
        private String ownerName;
        private final boolean passwordProtected;
        private final String salt;
        private final String passwordHash;
        private final List<UUID> members = new ArrayList<>();
        private final List<String> memberNames = new ArrayList<>();
        private final Set<UUID> privateAccess = new HashSet<>();

        public ChannelDefinition(
                String frequency,
                String name,
                UUID owner,
                String ownerName,
                boolean passwordProtected,
                String salt,
                String passwordHash
        ) {
            this.frequency = frequency;
            this.name = name;
            this.owner = owner;
            this.ownerName = ownerName;
            this.passwordProtected = passwordProtected;
            this.salt = salt;
            this.passwordHash = passwordHash;
        }

        public String frequency() {
            return frequency;
        }

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public UUID owner() {
            return owner;
        }

        public String ownerName() {
            return ownerName;
        }

        public void setOwner(UUID owner, String ownerName) {
            this.owner = owner;
            this.ownerName = ownerName;
        }

        public boolean passwordProtected() {
            return passwordProtected;
        }

        public String salt() {
            return salt;
        }

        public String passwordHash() {
            return passwordHash;
        }

        public List<UUID> members() {
            return members;
        }

        public List<String> memberNames() {
            return memberNames;
        }

        public Set<UUID> privateAccess() {
            return privateAccess;
        }

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
