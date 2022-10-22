package com.fqm.framework.mq.redis;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.util.Assert;
/**
 * 
 * 
 * @version 
 * @author 傅泉明
 */
public class PendingMessagesSummary {

    private final String groupName;
    private final Long totalPendingMessages;
    private final Range<String> idRange;
    private final Map<String, Long> pendingMessagesPerConsumer;

    public PendingMessagesSummary(String groupName, long totalPendingMessages, Range<String> idRange,
            Map<String, Long> pendingMessagesPerConsumer) {

        Assert.notNull(idRange, "ID Range must not be null");
        Assert.notNull(pendingMessagesPerConsumer, "Pending Messages must not be null");

        this.groupName = groupName;
        this.totalPendingMessages = totalPendingMessages;
        this.idRange = idRange;
        this.pendingMessagesPerConsumer = pendingMessagesPerConsumer;
    }

    /**
     * Get the range between the smallest and greatest ID among the pending messages.
     *
     * @return never {@literal null}.
     */
    public Range<String> getIdRange() {
        return idRange;
    }

    /**
     * Get the smallest ID among the pending messages.
     *
     * @return never {@literal null}.
     */
    public RecordId minRecordId() {
        return RecordId.of(minMessageId());
    }

    /**
     * Get the greatest ID among the pending messages.
     *
     * @return never {@literal null}.
     */
    public RecordId maxRecordId() {
        return RecordId.of(maxMessageId());
    }

    /**
     * Get the smallest ID as {@link String} among the pending messages.
     *
     * @return never {@literal null}.
     */
    public String minMessageId() {
        Optional<String> value = idRange.getLowerBound().getValue();
        return value.isPresent() ? value.get() : null;
    }

    /**
     * Get the greatest ID as {@link String} among the pending messages.
     *
     * @return never {@literal null}.
     */
    public String maxMessageId() {
        Optional<String> value = idRange.getUpperBound().getValue();
        return value.isPresent() ? value.get() : null;
    }

    /**
     * Get the number of total pending messages within the {@literal consumer group}.
     *
     * @return never {@literal null}.
     */
    public long getTotalPendingMessages() {
        return totalPendingMessages;
    }

    /**
     * @return the {@literal consumer group} name.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Obtain a map of every {@literal consumer} in the {@literal consumer group} with at least one pending message, and
     * the number of pending messages.
     *
     * @return never {@literal null}.
     */
    public Map<String, Long> getPendingMessagesPerConsumer() {
        return Collections.unmodifiableMap(pendingMessagesPerConsumer);
    }

    @Override
    public String toString() {

        return "PendingMessagesSummary{" + "groupName='" + groupName + '\'' + ", totalPendingMessages='"
                + getTotalPendingMessages() + '\'' + ", minMessageId='" + minMessageId() + '\'' + ", maxMessageId='"
                + maxMessageId() + '\'' + ", pendingMessagesPerConsumer=" + pendingMessagesPerConsumer + '}';
    }
}
