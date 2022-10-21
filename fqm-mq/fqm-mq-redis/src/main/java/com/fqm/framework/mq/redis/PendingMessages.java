package com.fqm.framework.mq.redis;

import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Range;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;
/**
 * 
 * 
 * @version 
 * @author 傅泉明
 */
public class PendingMessages implements Streamable<PendingMessage> {

    private final String groupName;
    private final Range<?> range;
    private final List<PendingMessage> pendingMessages;

    public PendingMessages(String groupName, List<PendingMessage> pendingMessages) {
        this(groupName, Range.unbounded(), pendingMessages);
    }

    public PendingMessages(String groupName, Range<?> range, List<PendingMessage> pendingMessages) {

        Assert.notNull(range, "Range must not be null");
        Assert.notNull(pendingMessages, "Pending Messages must not be null");

        this.groupName = groupName;
        this.range = range;
        this.pendingMessages = pendingMessages;
    }

    /**
     * Adds the range to the current {@link PendingMessages}.
     *
     * @param range must not be {@literal null}.
     * @return new instance of {@link PendingMessages}.
     */
    public PendingMessages withinRange(Range<?> range) {
        return new PendingMessages(groupName, range, pendingMessages);
    }

    /**
     * The {@literal consumer group} name.
     *
     * @return never {@literal null}.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * The {@link Range} pending messages have been loaded.
     *
     * @return never {@literal null}.
     */
    public Range<?> getRange() {
        return range;
    }

    /**
     * @return {@literal true} if no messages pending within range.
     */
    @Override
    public boolean isEmpty() {
        return pendingMessages.isEmpty();
    }

    /**
     * @return the number of pending messages in range.
     */
    public int size() {
        return pendingMessages.size();
    }

    /**
     * Get the {@link PendingMessage} at the given position.
     *
     * @param index
     * @return the {@link PendingMessage} a the given index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public PendingMessage get(int index) {
        return pendingMessages.get(index);
    }

    /**
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<PendingMessage> iterator() {
        return pendingMessages.iterator();
    }

    @Override
    public String toString() {
        return "PendingMessages{" + "groupName='" + groupName + '\'' + ", range=" + range + ", pendingMessages="
                + pendingMessages + '}';
    }
}
