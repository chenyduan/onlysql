package com.originlang.onlysql;

import java.util.Objects;

public class SqlBuilder {

	private StringBuilder sql = new StringBuilder();

	public SqlBuilder() {

	}

	public String build() {
		return sql.toString();
	}

	@Override
	public String toString() {
		return sql.toString();
	}

	/**
	 *
	 * select
	 */
	public SqlBuilder select(String... fields) {
		sql.append("SELECT ");
		for (String field : fields) {
			sql.append(" ").append(field);
			if (!Objects.equals(field, fields[fields.length - 1])) {
				sql.append(",");
			}
		}
		return this;
	}

	/**
	 * from
	 */
	public SqlBuilder from(String table) {
		sql.append(" FROM ").append(table);
		return this;
	}

	/**
	 * where
	 */
	public SqlBuilder where() {
		sql.append("WHERE ");
		return this;
	}

	/**
	 * eq
	 */
	public SqlBuilder eq(String field, String value) {
		sql.append(" = ");
		sql.append(value);
		return this;
	}

	/**
	 * ne
	 */
	public SqlBuilder ne(String field, String value) {
		sql.append(" != ");
		sql.append(value);
		return this;
	}

	/**
	 * gt
	 */
	public SqlBuilder gt(String field, String value) {
		sql.append(" > ");
		sql.append(value);
		return this;
	}

	/**
	 * gte
	 */
	public SqlBuilder gte(String field, String value) {
		sql.append(" >= ");
		sql.append(value);
		return this;
	}

	/**
	 * lt
	 */
	public SqlBuilder lt(String field, String value) {
		sql.append(" < ");
		sql.append(value);
		return this;
	}

	/**
	 * lte
	 */
	public SqlBuilder lte(String field, String value) {
		sql.append(" <= ");
		sql.append(value);
		return this;
	}

	/**
	 * in
	 */
	public SqlBuilder in(String field, String... value) {
		sql.append(" IN ");
		sql.append("(");
		for (String v : value) {
			sql.append(v);
			if (v != value[value.length - 1]) {
				sql.append(",");
			}
		}
		sql.append(")");
		return this;
	}

	/**
	 * not in
	 */
	public SqlBuilder notIn(String field, String... value) {
		sql.append(" NOT IN ");
		sql.append("(");
		for (String v : value) {
			sql.append(v);
			if (v != value[value.length - 1]) {
				sql.append(",");
			}
		}
		sql.append(")");
		return this;
	}

	/**
	 * like
	 */
	public SqlBuilder like(String field, String value) {
		sql.append(" LIKE ");
		sql.append(value);
		return this;
	}

	/**
	 * not like
	 */
	public SqlBuilder notLike(String field, String value) {
		sql.append(" NOT LIKE ");
		sql.append(value);
		return this;
	}

	/**
	 * between
	 */
	public SqlBuilder between(String field, String value1, String value2) {
		sql.append(" BETWEEN ");
		sql.append(value1);
		sql.append(" AND ");
		sql.append(value2);
		return this;
	}

	/**
	 * and
	 */
	public SqlBuilder and() {
		sql.append(" AND ");
		return this;
	}

	/**
	 * and
	 */
	public SqlBuilder and(String subSql) {
		sql.append(" AND (");
		sql.append(subSql);
		sql.append(")");
		return this;
	}

	/**
	 * or
	 */
	public SqlBuilder or() {
		sql.append(" OR ");
		return this;
	}

	/**
	 * or
	 */
	public SqlBuilder or(String subSql) {
		sql.append(" OR (");
		sql.append(subSql);
		sql.append(")");
		return this;
	}

	/**
	 * not
	 */
	public SqlBuilder not() {
		sql.append(" NOT ");
		return this;
	}

	/**
	 * not
	 */
	public SqlBuilder not(String subSql) {
		sql.append(" NOT (");
		sql.append(subSql);
		sql.append(")");
		return this;
	}

	/**
	 * order by
	 */
	public SqlBuilder orderBy(String field) {
		sql.append(" ORDER BY ");
		sql.append(field);
		return this;
	}

	/**
	 * limit
	 */
	public SqlBuilder limit(int limit) {
		sql.append(" LIMIT ");
		sql.append(limit);
		return this;
	}

	/**
	 * offset
	 */
	public SqlBuilder offset(int offset) {
		sql.append(" OFFSET ");
		sql.append(offset);
		return this;
	}

	/**
	 * as
	 */
	public SqlBuilder as(String alias) {
		sql.append(" AS ");
		sql.append(alias);
		return this;
	}

	/**
	 * join
	 */
	public SqlBuilder join(String table) {
		sql.append(" JOIN ");
		sql.append(table);
		return this;
	}

	/**
	 * left join
	 */
	public SqlBuilder leftJoin(String table) {
		sql.append(" LEFT JOIN ");
		sql.append(table);
		return this;
	}

	/**
	 * right join
	 */
	public SqlBuilder rightJoin(String table) {
		sql.append(" RIGHT JOIN ");
		sql.append(table);
		return this;
	}

	/**
	 * inner join
	 */
	public SqlBuilder innerJoin(String table) {
		sql.append(" INNER JOIN ");
		sql.append(table);
		return this;
	}

	/**
	 * full join
	 */
	public SqlBuilder fullJoin(String table) {
		sql.append(" FULL JOIN ");
		sql.append(table);
		return this;
	}

	/**
	 * on
	 */
	public SqlBuilder on(String condition) {
		sql.append(" ON ");
		sql.append(condition);
		return this;
	}

	/**
	 * having
	 */
	public SqlBuilder having(String condition) {
		sql.append(" HAVING ");
		sql.append(condition);
		return this;
	}

}
