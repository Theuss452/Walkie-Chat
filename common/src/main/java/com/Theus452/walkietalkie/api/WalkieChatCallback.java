package com.Theus452.walkietalkie.api;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class WalkieChatCallback {

    private WalkieChatCallback() {
    }

    public static final Event<Consumer<BroadcastData>> WALKIE_BROADCAST = new Event<>();
    public static final Event<Consumer<ProximityData>> PROXIMITY_CHAT = new Event<>();

    public record BroadcastData(
            ServerPlayer sender,
            String message,
            String frequency,
            List<ServerPlayer> receivers) {
        public BroadcastData {
            receivers = Collections.unmodifiableList(new ArrayList<>(receivers));
        }
    }

    public record ProximityData(
            ServerPlayer sender,
            String message,
            List<ServerPlayer> recipients,
            double range) {
        public ProximityData {
            recipients = Collections.unmodifiableList(new ArrayList<>(recipients));
        }
    }

    public static class Event<T> {

        private final List<T> listeners = new CopyOnWriteArrayList<>();

        public void register(T listener) {
            listeners.add(listener);
        }

        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

        @SuppressWarnings("unchecked")
        public <D> void invoke(D data) {
            for (T listener : listeners) {
                try {
                    ((Consumer<D>) listener).accept(data);
                } catch (Exception e) {
                    LoggerFactory.getLogger("WalkieChat-API")
                            .error("[WalkieChat] Exception in callback listener, ignoring", e);
                }
            }
        }
    }
}
