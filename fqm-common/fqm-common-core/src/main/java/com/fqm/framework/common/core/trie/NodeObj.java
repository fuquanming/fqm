package com.fqm.framework.common.core.trie;
/** 节点包含的对象 */
public class NodeObj {
    private Object obj;
    /** 找到节点的下标索引 */
    private int objIndex;
    public Object getObj() {
        return obj;
    }
    public void setObj(Object obj) {
        this.obj = obj;
    }
    public int getObjIndex() {
        return objIndex;
    }
    public void setObjIndex(int objIndex) {
        this.objIndex = objIndex;
    }
}
