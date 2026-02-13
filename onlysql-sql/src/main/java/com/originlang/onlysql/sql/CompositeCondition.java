package com.originlang.onlysql.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组合条件：AND 或 OR 连接多个 WhereCriteria。
 */
public final class CompositeCondition implements WhereCriteria {

    private final String connector;
    private final List<WhereCriteria> criteriaList;

    private CompositeCondition(String connector, List<WhereCriteria> criteriaList) {
        this.connector = connector;
        this.criteriaList = criteriaList == null ? List.of() : new ArrayList<>(criteriaList);
    }

    /** (c1) AND (c2) AND ... */
    public static WhereCriteria and(WhereCriteria... criteria) {
        return and(Arrays.asList(criteria));
    }

    public static WhereCriteria and(List<WhereCriteria> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("and 至少需要一个条件");
        }
        if (criteria.size() == 1) {
            return criteria.get(0);
        }
        return new CompositeCondition(" AND ", criteria);
    }

    /** (c1) OR (c2) OR ... */
    public static WhereCriteria or(WhereCriteria... criteria) {
        return or(Arrays.asList(criteria));
    }

    public static WhereCriteria or(List<WhereCriteria> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("or 至少需要一个条件");
        }
        if (criteria.size() == 1) {
            return criteria.get(0);
        }
        return new CompositeCondition(" OR ", criteria);
    }

    @Override
    public String getWhereExpr() {
        return criteriaList.stream()
                .map(c -> "(" + c.getWhereExpr() + ")")
                .collect(Collectors.joining(connector));
    }

    @Override
    public List<Object> getWhereParams() {
        List<Object> out = new ArrayList<>();
        for (WhereCriteria c : criteriaList) {
            List<Object> p = c.getWhereParams();
            if (p != null) {
                out.addAll(p);
            }
        }
        return out;
    }
}
