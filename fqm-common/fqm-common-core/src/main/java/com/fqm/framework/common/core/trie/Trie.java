/*
 * Copyright(2009-****) of FuQuanming Inc.. All rights reserved.
 * FuQuanming PROPRIETARY/CONFIDENTIAL, Use is subject to license terms.
 */
package com.fqm.framework.common.core.trie;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * <p>Description:字典树</p>
 * <p>Copyright:Copyright (c) 2009 
 *      [0]
 *     --+--
 *    /  |  \
 *  a/  d|   \b
 * [1]  [2]  [4]
 *  |       --+--
 *  |      /  |  \
 * c|    a/  e|  d\
 * [3]  [5]  [6]  [9]
 *     --+--
 *    /     \
 *  d/      e\
 * [7]       [8]
 * </p>
 * @author 傅泉明
 * @version v1.0
 */
public class Trie {

    private TrieNode root;
    
    public Trie() {
        root = new TrieNode();
    }
    
    public void addString(String str, Object obj) {
        if (str != null && str.length() > 0) {
            char[] chars = str.toCharArray();
            TrieNode node = root;
            int length = chars.length;
            for (int i = 0; i < length; i++) {
                node = node.addString(chars[i]);
            }
            // 最后一个设置 携带的对象Obj
            node.setObj(obj);
        }
    }
    /**
     * 取得字典中携带的对象 (包含匹配)
     * @param str
     * @return
     */
    public Object getObjectContains(String str) {
        if (str == null) {
            return null;
        }
        TrieNode lastNode = root;
        char[] chars = str.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            lastNode = lastNode.getNode(chars[i]);
            if (lastNode == null) {
                lastNode = root;
            } else if (lastNode.getObj() != null) {
                return lastNode.getObj();
            }
        }
        if (lastNode == null) {
            return null;
        }
        return lastNode.getObj();
    }
    
    /**
     * 取得字典中携带的对象 (包含匹配)
     * @param str
     * @return
     */
    public NodeObj getObjectContainsIndex(String str) {
        if (str == null) {
            return null;
        }
        TrieNode lastNode = root;
        char[] chars = str.toCharArray();
        int length = chars.length;
        /** 记录找到第一个字符的下标 */
        int temp = 0;
        for (int i = 0; i < length; i++) {
            lastNode = lastNode.getNode(chars[i]);
            if (lastNode == null) {
                lastNode = root;
                temp = i;
                temp++;
            } else if (lastNode.getObj() != null) {
                NodeObj nodeObj = new NodeObj();
                nodeObj.setObj(lastNode.getObj());
                nodeObj.setObjIndex(temp);
                return nodeObj;
            }
        }
        if (lastNode == null) {
            return null;
        }
        return null;
    }
    
    /**
     * 取得字典中携带的对象 (前缀匹配) 如：key=1890651 ，内容：18965188560 则找到
     * @param str
     * @return
     */
    public Object getObject(String str) {
        if (str == null) {
            return null;
        }
        TrieNode lastNode = root;
        char[] chars = str.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            lastNode = lastNode.getNode(chars[i]);
            if (lastNode == null) {
                return null;
            } else if (lastNode.getObj() != null) {
                return lastNode.getObj();
            }
        }
        if (lastNode == null) {
            return null;
        }
        return lastNode.getObj();
    }
    
    public void removeString(String str) {
        if (str == null) {
            return;
        }
        TrieNode lastNode = root;
        char[] chars = str.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            lastNode = lastNode.getNode(chars[i]);
            if (lastNode == null) {
                break;
            }
        }
        while (lastNode != null && lastNode != root) {
            TrieNode parentNode = lastNode.getParent();
            parentNode.getChildren().remove(lastNode.getCharacter());
            lastNode = parentNode.getParent();
        }
    }
    
    class TrieNode {
        /** 父亲 */
        private TrieNode parent;
        /** 孩子 */
        private Map<Character, TrieNode> children;
        /** 是否是子节点 */
        private boolean isLeaf;
        /** 该节点的字符 */
        private char character;
        /** 子节点、携带的对象 */
        private Object obj;
        
        public TrieNode() {
            children = new HashMap<>();
        }
        public TrieNode(char character) {
            this();
            this.character = character;
        }
        
        TrieNode addString(char c) {
            TrieNode node = children.computeIfAbsent(c, k -> new TrieNode(c));
            node.parent = this;
            return node;
        }
        public Object getObj() {
            return obj;
        }
        public void setObj(Object obj) {
            this.isLeaf = true;
            this.obj = obj;
        }
        TrieNode getNode(char c) {
            return children.get(c);
        }
        public TrieNode getParent() {
            return parent;
        }
        public void setParent(TrieNode parent) {
            this.parent = parent;
        }
        public Map<Character, TrieNode> getChildren() {
            return children;
        }
        public void setChildren(Map<Character, TrieNode> children) {
            this.children = children;
        }
        public boolean isLeaf() {
            return isLeaf;
        }
        public void setLeaf(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }
        public char getCharacter() {
            return character;
        }
        public void setCharacter(char character) {
            this.character = character;
        }
    }
}