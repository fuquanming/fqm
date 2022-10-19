package com.fqm.framework.common.redis.listener.spring;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.core.RedisKeyspaceEvent;
import org.springframework.data.redis.core.convert.MappingRedisConverter.BinaryKeyspaceIdentifier;
import org.springframework.lang.Nullable;

/**
 * Redis 删除key的事件
 * 
 * @version 
 * @author 傅泉明
 */
@SuppressWarnings("serial")
public class RedisKeyDeleteEvent extends RedisKeyspaceEvent {
    /**
     * Use {@literal UTF-8} as default charset.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final BinaryKeyspaceIdentifier objectId;
    private final @Nullable Object value;

    /**
     * Creates new {@link RedisKeyDeleteEvent}.
     *
     * @param key the expired key.
     */
    public RedisKeyDeleteEvent(byte[] key) {
        this(key, null);
    }

    /**
     * Creates new {@link RedisKeyDeleteEvent}
     *
     * @param key the expired key.
     * @param value the value of the expired key. Can be {@literal null}.
     */
    public RedisKeyDeleteEvent(byte[] key, @Nullable Object value) {
        this(null, key, value);
    }

    /**
     * Creates new {@link RedisKeyDeleteEvent}
     *
     * @param channel the Pub/Sub channel through which this event was received.
     * @param key the expired key.
     * @param value the value of the expired key. Can be {@literal null}.
     * @since 1.8
     */
    public RedisKeyDeleteEvent(@Nullable String channel, byte[] key, @Nullable Object value) {
        super(channel, key);

        if (BinaryKeyspaceIdentifier.isValid(key)) {
            this.objectId = BinaryKeyspaceIdentifier.of(key);
        } else {
            this.objectId = null;
        }

        this.value = value;
    }

    /**
     * Gets the keyspace in which the expiration occured.
     *
     * @return {@literal null} if it could not be determined.
     */
    public String getKeyspace() {
        return objectId != null ? new String(objectId.getKeyspace(), CHARSET) : null;
    }

    /**
     * Get the expired objects id.
     *
     * @return the expired objects id.
     */
    public byte[] getId() {
        return objectId != null ? objectId.getId() : getSource();
    }

    /**
     * Get the expired Object
     *
     * @return {@literal null} if not present.
     */
    @Nullable
    public Object getValue() {
        return value;
    }

    /**
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        byte[] id = getId();
        return "RedisKeyDeleteEvent [keyspace=" + getKeyspace() + ", id=" + (id == null ? null : new String(id)) + "]";
    }
}
