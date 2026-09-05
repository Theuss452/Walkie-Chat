package com.Theus452.walkietalkie.client;

import java.util.*;

public final class ChannelMessageCache {
    public static final int MAX_MESSAGES = 100;
    public static final int MAX_TRACKED_CHANNELS = 20;

    public record ChatEntry(String senderName, String message, long timestampMs) {
    }

    private static final Map<String, Deque<ChatEntry>> CACHE = Collections.synchronizedMap(
            new LinkedHashMap<String, Deque<ChatEntry>>(MAX_TRACKED_CHANNELS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Deque<ChatEntry>> eldest) {
                    return size() > MAX_TRACKED_CHANNELS;
                }
            }
    );

    private ChannelMessageCache() {
    }

    public static void add(String frequency, String senderName, String message) {
        if (frequency == null || frequency.isBlank())
            return;
        
        Deque<ChatEntry> deque = CACHE.computeIfAbsent(frequency,
                k -> new java.util.concurrent.ConcurrentLinkedDeque<>());
        
        deque.addLast(new ChatEntry(senderName, message, System.currentTimeMillis()));
        
        while (deque.size() > MAX_MESSAGES) {
            deque.pollFirst();
        }
    }

    public static List<ChatEntry> getRecent(String frequency, int maxLines) {
        Deque<ChatEntry> deque = CACHE.get(frequency);
        if (deque == null || deque.isEmpty())
            return Collections.emptyList();
        int skip = Math.max(0, deque.size() - maxLines);
        List<ChatEntry> result = new ArrayList<>(Math.min(maxLines, deque.size()));
        int i = 0;
        for (ChatEntry e : deque) {
            if (i++ >= skip)
                result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static void clear(String frequency) {
        if (frequency == null) return;
        CACHE.remove(frequency);
    }

    public static void retainFrequencies(Collection<String> activeFrequencies) {
        if (activeFrequencies == null) return;
        CACHE.keySet().retainAll(activeFrequencies);
    }
}
