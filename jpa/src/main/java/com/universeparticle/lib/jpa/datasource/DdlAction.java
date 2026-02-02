package com.universeparticle.lib.jpa.datasource;

/**
 * @author
 * @since
 */
public final class DdlAction {
    /**
     * 删除数据库,并重新创建
     */
    public static final String CREATE = "create";
    /**
     * 更新
     */
    public static final String DROP = "update";
    /**
     * 修改数据库
     */
    public static final String ALTER = "alter";
    /**
     * 不做任何操作
     */
    public static final String NONE = "none";
}
