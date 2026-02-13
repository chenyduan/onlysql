package com.originlang.onlysql.sql;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 每行数据类型（Map 或实体）
 */
public final class Page<T> {

    private final List<T> list;
    private final long total;
    private final int pageIndex;
    private final int pageSize;
    private final int totalPages;

    public Page(List<T> list, long total, int pageIndex, int pageSize) {
        this.list = list == null ? Collections.emptyList() : List.copyOf(list);
        this.total = total < 0 ? 0 : total;
        this.pageIndex = pageIndex < 0 ? 0 : pageIndex;
        this.pageSize = pageSize <= 0 ? 0 : pageSize;
        this.totalPages = this.pageSize == 0 ? 0 : (int) ((this.total + this.pageSize - 1) / this.pageSize);
    }

    /** 当前页数据列表。 */
    public List<T> getList() {
        return list;
    }

    /** 总记录数。 */
    public long getTotal() {
        return total;
    }

    /** 当前页码，从 0 开始。 */
    public int getPageIndex() {
        return pageIndex;
    }

    /** 每页条数。 */
    public int getPageSize() {
        return pageSize;
    }

    /** 总页数。 */
    public int getTotalPages() {
        return totalPages;
    }

    /** 是否有下一页。 */
    public boolean hasNext() {
        return pageIndex + 1 < totalPages;
    }

    /** 是否有上一页。 */
    public boolean hasPrevious() {
        return pageIndex > 0;
    }

    /** 是否为空结果（无数据）。 */
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
