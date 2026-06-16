package com.Theus452.walkietalkie.client;

import com.Theus452.walkietalkie.networking.packet.ChannelActionType;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class ChannelActionCache {
    private static final AtomicReference<Result> RESULT = new AtomicReference<>();
    private static final AtomicLong REQUEST_SEQUENCE = new AtomicLong();

    private ChannelActionCache() {
    }

    public static void set(boolean success, String frequency, String messageKey, ChannelActionType actionType, long requestId) {
        RESULT.set(new Result(success, frequency, messageKey, actionType, requestId));
    }

    public static Result consume() {
        return RESULT.getAndSet(null);
    }

    public static void clear() {
        RESULT.set(null);
    }

    public static long nextRequestId() {
        return REQUEST_SEQUENCE.incrementAndGet();
    }

    public record Result(boolean success, String frequency, String messageKey, ChannelActionType actionType, long requestId) {
    }
}
