/*
 * @(#)StreamInfo.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-mq-redis
 * 创建日期 : 2022年5月6日
 * 修改历史 : 
 *     1. [2022年5月6日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class StreamInfo {

    public static class XInfoObject {

        protected static final Map<String, Class<?>> DEFAULT_TYPE_HINTS;

        static {

            Map<String, Class<?>> defaults = new HashMap<>(2);
            defaults.put("root", Map.class);
            defaults.put("root.*", String.class);

            DEFAULT_TYPE_HINTS = Collections.unmodifiableMap(defaults);
        }

        private Map<String, Object> raw;

        private XInfoObject(List<Object> raw, Map<String, Class<?>> typeHints) {
            Map<String, Object> map = new HashMap<>();
            int size = raw.size();
            for (int i = 0; i < size; i++) {
                Object obj = raw.get(i);
                if (i % 2 == 0) {
                    map.put(obj.toString(), raw.get(i + 1));
                }
            }
            this.raw = map;
        }

        private XInfoObject(Map<String, Object> raw) {
            this.raw = raw;
        }

        <T> T get(String entry, Class<T> type) {

            Object value = raw.get(entry);

            return value == null ? null : type.cast(value);
        }

        <I, T> T getAndMap(String entry, Class<I> type, Function<I, T> f) {

            I value = get(entry, type);

            return value == null ? null : f.apply(value);
        }

        public Map<String, Object> getRaw() {
            return raw;
        }

        @Override
        public String toString() {
            return "XInfoStream" + raw;
        }
    }

    /**
     * Value object holding general information about a {@literal Redis Stream}.
     *
     * @author Christoph Strobl
     */
    public static class XInfoStream extends XInfoObject {

        private static final Map<String, Class<?>> typeHints;

        static {

            typeHints = new HashMap<>(DEFAULT_TYPE_HINTS);
            typeHints.put("root.first-entry", Map.class);
            typeHints.put("root.first-entry.*", Map.class);
            typeHints.put("root.first-entry.*.*", Object.class);
            typeHints.put("root.last-entry", Map.class);
            typeHints.put("root.last-entry.*", Map.class);
            typeHints.put("root.last-entry.*.*", Object.class);
        }

        private XInfoStream(List<Object> raw) {
            super(raw, typeHints);
        }

        /**
         * Factory method to create a new instance of {@link XInfoStream}.
         *
         * @param source the raw value source.
         * @return
         */
        public static XInfoStream fromList(List<Object> source) {
            return new XInfoStream(source);
        }

        /**
         * Total number of element in the stream. Corresponds to {@literal length}.
         *
         * @return
         */
        public Long streamLength() {
            return get("length", Long.class);
        }

        /**
         * The streams radix tree key size. Corresponds to {@literal radix-tree-keys}.
         *
         * @return
         */
        public Long radixTreeKeySize() {
            return get("radix-tree-keys", Long.class);
        }

        /**
         * Total number of element radix tree nodes. Corresponds to {@literal radix-tree-nodes}.
         *
         * @return
         */
        public Long radixTreeNodesSize() {
            return get("radix-tree-nodes", Long.class);
        }

        /**
         * The number of associated {@literal consumer groups}. Corresponds to {@literal groups}.
         *
         * @return
         */
        public Long groupCount() {
            return get("groups", Long.class);
        }

        /**
         * The last generated id. May not be the same as {@link #lastEntryId()}. Corresponds to
         * {@literal last-generated-id}.
         *
         * @return
         */
        public String lastGeneratedId() {
            return get("last-generated-id", String.class);
        }

        /**
         * The id of the streams first entry. Corresponds to {@literal first-entry 1)}.
         *
         * @return
         */
        public String firstEntryId() {
            return getAndMap("first-entry", Map.class, it -> it.keySet().iterator().next().toString());
        }

        /**
         * The streams first entry. Corresponds to {@literal first-entry}.
         *
         * @return
         */
        @SuppressWarnings("unchecked")
        public Map<Object, Object> getFirstEntry() {
            return (Map<Object, Object>) getAndMap("first-entry", Map.class, Collections::unmodifiableMap);
        }

        /**
         * The id of the streams last entry. Corresponds to {@literal last-entry 1)}.
         *
         * @return
         */
        public String lastEntryId() {
            return getAndMap("last-entry", Map.class, it -> it.keySet().iterator().next().toString());
        }

        /**
         * The streams first entry. Corresponds to {@literal last-entry}.
         *
         * @return
         */
        @SuppressWarnings("unchecked")
        public Map<Object, Object> getLastEntry() {
            return (Map<Object, Object>) getAndMap("last-entry", Map.class, Collections::unmodifiableMap);
        }

    }

    /**
     * Value object holding general information about {@literal consumer groups} associated with a
     * {@literal Redis Stream}.
     *
     * @author Christoph Strobl
     */
    public static class XInfoGroups {

        private final List<XInfoGroup> groupInfoList;

        @SuppressWarnings("unchecked")
        private XInfoGroups(List<Object> raw) {

            groupInfoList = new ArrayList<>();
            for (Object entry : raw) {
                groupInfoList.add(new XInfoGroup((List<Object>) entry));
            }
        }

        /**
         * Factory method to create a new instance of {@link XInfoGroups}.
         *
         * @param source the raw value source.
         * @return
         */
        public static XInfoGroups fromList(List<Object> source) {
            return new XInfoGroups(source);
        }

        /**
         * Total number of associated {@literal consumer groups}.
         *
         * @return zero if none available.
         */
        public int groupCount() {
            return size();
        }

        /**
         * Returns the number of {@link XInfoGroup} available.
         *
         * @return zero if none available.
         * @see #groupCount()
         */
        public int size() {
            return groupInfoList.size();
        }

        /**
         * @return {@literal true} if no groups associated.
         */
        public boolean isEmpty() {
            return groupInfoList.isEmpty();
        }

        /**
         * Returns an iterator over the {@link XInfoGroup} elements.
         *
         * @return
         */
        public Iterator<XInfoGroup> iterator() {
            return groupInfoList.iterator();
        }

        /**
         * Returns the {@link XInfoGroup} element at the given {@literal index}.
         *
         * @return the element at the specified position.
         * @throws IndexOutOfBoundsException if the index is out of range.
         */
        public XInfoGroup get(int index) {
            return groupInfoList.get(index);
        }

        /**
         * Returns a sequential {@code Stream} of {@link XInfoGroup}.
         *
         * @return
         */
        public Stream<XInfoGroup> stream() {
            return groupInfoList.stream();
        }

        /**
         * Performs the given {@literal action} on every available {@link XInfoGroup} of this {@link XInfoGroups}.
         *
         * @param action
         */
        public void forEach(Consumer<? super XInfoGroup> action) {
            groupInfoList.forEach(action);
        }

        @Override
        public String toString() {
            return "XInfoGroups" + groupInfoList;
        }

    }

    public static class XInfoGroup extends XInfoObject {

        private XInfoGroup(List<Object> raw) {
            super(raw, DEFAULT_TYPE_HINTS);
        }

        public static XInfoGroup fromList(List<Object> raw) {
            return new XInfoGroup(raw);
        }

        /**
         * The {@literal consumer group} name. Corresponds to {@literal name}.
         *
         * @return
         */
        public String groupName() {
            return get("name", String.class);
        }

        /**
         * The total number of consumers in the {@literal consumer group}. Corresponds to {@literal consumers}.
         *
         * @return
         */
        public Long consumerCount() {
            return get("consumers", Long.class);
        }

        /**
         * The total number of pending messages in the {@literal consumer group}. Corresponds to {@literal pending}.
         *
         * @return
         */
        public Long pendingCount() {
            return get("pending", Long.class);
        }

        /**
         * The id of the last delivered message. Corresponds to {@literal last-delivered-id}.
         *
         * @return
         */
        public String lastDeliveredId() {
            return get("last-delivered-id", String.class);
        }
    }

    public static class XInfoConsumers {

        private final List<XInfoConsumer> consumerInfoList;

        @SuppressWarnings("unchecked")
        public XInfoConsumers(String groupName, List<Object> raw) {

            consumerInfoList = new ArrayList<>();
            for (Object entry : raw) {
                consumerInfoList.add(new XInfoConsumer(groupName, (List<Object>) entry));
            }
        }

        public static XInfoConsumers fromList(String groupName, List<Object> source) {
            return new XInfoConsumers(groupName, source);
        }

        /**
         * Total number of {@literal consumers} in the {@literal consumer group}.
         *
         * @return zero if none available.
         */
        public int getConsumerCount() {
            return consumerInfoList.size();
        }

        /**
         * Returns the number of {@link XInfoConsumer} available.
         *
         * @return zero if none available.
         * @see #getConsumerCount()
         */
        public int size() {
            return consumerInfoList.size();
        }

        /**
         * @return {@literal true} if no groups associated.
         */
        public boolean isEmpty() {
            return consumerInfoList.isEmpty();
        }

        /**
         * Returns an iterator over the {@link XInfoConsumer} elements.
         *
         * @return
         */
        public Iterator<XInfoConsumer> iterator() {
            return consumerInfoList.iterator();
        }

        /**
         * Returns the {@link XInfoConsumer} element at the given {@literal index}.
         *
         * @return the element at the specified position.
         * @throws IndexOutOfBoundsException if the index is out of range.
         */
        public XInfoConsumer get(int index) {
            return consumerInfoList.get(index);
        }

        /**
         * Returns a sequential {@code Stream} of {@link XInfoConsumer}.
         *
         * @return
         */
        public Stream<XInfoConsumer> stream() {
            return consumerInfoList.stream();
        }

        /**
         * Performs the given {@literal action} on every available {@link XInfoConsumer} of this {@link XInfoConsumers}.
         *
         * @param action
         */
        public void forEach(Consumer<? super XInfoConsumer> action) {
            consumerInfoList.forEach(action);
        }

        @Override
        public String toString() {
            return "XInfoConsumers" + consumerInfoList;
        }
    }

    public static class XInfoConsumer extends XInfoObject {

        private final String groupName;

        public XInfoConsumer(String groupName, List<Object> raw) {

            super(raw, DEFAULT_TYPE_HINTS);
            this.groupName = groupName;
        }

        /**
         * The {@literal consumer group} name.
         *
         * @return
         */
        public String groupName() {
            return groupName;
        }

        /**
         * The {@literal consumer} name. Corresponds to {@literal name}.
         *
         * @return
         */
        public String consumerName() {
            return get("name", String.class);
        }

        /**
         * The idle time (in millis). Corresponds to {@literal idle}.
         *
         * @return
         */
        public Long idleTimeMs() {
            return get("idle", Long.class);
        }

        /**
         * The idle time. Corresponds to {@literal idle}.
         *
         * @return
         */
        public Duration idleTime() {
            return Duration.ofMillis(idleTimeMs());
        }

        /**
         * The number of pending messages. Corresponds to {@literal pending}.
         *
         * @return
         */
        public Long pendingCount() {
            return get("pending", Long.class);
        }
    }
}
