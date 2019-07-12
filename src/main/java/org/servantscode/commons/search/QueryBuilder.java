package org.servantscode.commons.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.Organization;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.security.OrganizationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.servantscode.commons.StringUtils.isSet;

public class QueryBuilder {
    private static Logger LOG = LogManager.getLogger(QueryBuilder.class);

    private enum BuilderState {START, SELECT, FROM, JOIN, WHERE, GROUP, SORT, LIMIT, OFFSET, DONE };

    private List<String> selections = new LinkedList<>();
    private List<String> tables = new LinkedList<>();
    private List<String> joins = new LinkedList<>();
    private List<String> wheres = new LinkedList<>();
    private List<String> groupBy = new LinkedList<>();
    private String sort;
    private boolean limit;
    private boolean offset;

    private List<Object> values = new LinkedList<>();

    private BuilderState state = BuilderState.START;

    public QueryBuilder() {}

    public QueryBuilder select(String... selections) {
        setState(BuilderState.SELECT);
        this.selections.addAll(asList(selections));
        return this;
    }

    public QueryBuilder from(String... tables) {
        setState(BuilderState.FROM);
        this.tables.addAll(asList(tables));
        return this;
    }

    public QueryBuilder join(String... joins) {
        setState(BuilderState.JOIN);
        this.joins.addAll(asList(joins));
        return this;
    }

    public QueryBuilder withId(int id) {
        setState(BuilderState.WHERE);
        this.wheres.add("id=?");
        values.add(id);
        return this;
    }

    public QueryBuilder where(String clause, Object value) {
        setState(BuilderState.WHERE);
        this.wheres.add(clause);
        values.add(value);
        return this;
    }

    public QueryBuilder where(String clause, Object... value) {
        setState(BuilderState.WHERE);
        this.wheres.add(clause);
        values.addAll(Arrays.asList(value));
        return this;
    }

    public QueryBuilder where(String clause) {
        setState(BuilderState.WHERE);
        this.wheres.add(clause);
        return this;
    }

    public QueryBuilder whereIdIn(String field, QueryBuilder subselect) {
        setState(BuilderState.WHERE);
        this.wheres.add(String.format("%s IN (%s)", field, subselect.getSql()));
        values.add(subselect);
        return this;
    }

    public QueryBuilder whereIdNotIn(String field, QueryBuilder subselect) {
        setState(BuilderState.WHERE);
        this.wheres.add(String.format("%s NOT IN (%s)", field, subselect.getSql()));
        values.add(subselect);
        return this;
    }

    public QueryBuilder inOrg() {
        return inOrg("org_id", OrganizationContext.orgId());
    }

    public QueryBuilder inOrg(boolean includeSystem) {
        return includeSystem?
                inOrgOrSystem("org_id", OrganizationContext.orgId()):
                inOrg("org_id", OrganizationContext.orgId());
    }

    public QueryBuilder inOrg(String field) {
        return inOrg(field, OrganizationContext.orgId());
    }

    public QueryBuilder inOrg(String field, boolean includeSystem) {
        return includeSystem?
                inOrgOrSystem(field, OrganizationContext.orgId()):
                inOrg(field, OrganizationContext.orgId());
    }

    public QueryBuilder inOrg(String field, int orgId) {
        setState(BuilderState.WHERE);
        this.wheres.add(String.format("%s=?", field));
        values.add(orgId);
        return this;
    }

    public QueryBuilder inOrgOrSystem(String field, int orgId) {
        setState(BuilderState.WHERE);
        this.wheres.add(String.format("(%s=? OR %s IS NULL)", field, field));
        values.add(orgId);
        return this;
    }

    public QueryBuilder search(Search search) {
        setState(BuilderState.WHERE);
        if(search != null) {
            search.getClauses().forEach(clause -> {
                    this.wheres.add(clause.getSql());
                    this.values.addAll(clause.getValues());
                });
        }
        return this;
    }

    public QueryBuilder groupBy(String... fields) {
        setState(BuilderState.GROUP);
        groupBy.addAll(Arrays.asList(fields));
        return this;
    }

    public QueryBuilder sort(String sort) {
        setState(BuilderState.SORT);
        this.sort = sort;
        return this;
    }

    public QueryBuilder limit(int limit) {
        setState(BuilderState.LIMIT);
        if(limit > 0) {
            this.limit = true;
            values.add(limit);
        }
        return this;
    }

    public QueryBuilder offset(int offset) {
        setState(BuilderState.OFFSET);
        if(offset > 0) {
            this.offset = true;
            values.add(offset);
        }
        return this;
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        //Stmt cannot be opened in a try structure, else it will auto close instead of returning.
        //So we need to open it outselves, and be wary of connection leaks.

        //Error here will not leak as higher level owns the connection and prepareStatement shouldn't leak on error.
        //We are relying on the underlying prepareStatement() but this seems reasonable.
        String sql = getSql();
        LOG.trace("generated sql: " + sql);

        PreparedStatement stmt = conn.prepareStatement(getSql());
        try {
            //Error here would leak connection, so catch, close and re-throw.
            fillStatement(stmt);
        } catch (Throwable t) {
            stmt.close();
            throw t;
        }
        return stmt;
    }

    public String getSql() {
        setState(BuilderState.DONE);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selections));
        sql.append(" FROM ").append(String.join(", ", tables));
        if(!joins.isEmpty())
            sql.append(" ").append(String.join(" ", joins));
        if(!wheres.isEmpty())
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        if(!groupBy.isEmpty())
            sql.append(" GROUP BY ").append(String.join(", ", groupBy));
        if(isSet(sort))
            sql.append(" ORDER BY " + sort);
        if(limit)
            sql.append(" LIMIT ?");
        if(offset)
            sql.append(" OFFSET ?");
        return sql.toString();
    }

    public void fillStatement(PreparedStatement stmt) {
        fillStatement(stmt, new AtomicInteger(1));
    }

    // ----- Private -----
    private void setState(BuilderState nextState) {
        if(nextState.compareTo(state) < 0)
            throw new IllegalStateException("Cannot " + nextState + " after " + state);

        state = nextState;
    }

    private void fillStatement(PreparedStatement stmt, AtomicInteger pos) {
        values.forEach(value -> {
            try {
                if(value instanceof QueryBuilder)
                    ((QueryBuilder) value).fillStatement(stmt, pos);
                else
                    stmt.setObject(pos.getAndIncrement(), sqlize(value));
            } catch (SQLException e) {
                throw new RuntimeException(String.format("Could not populate sql with value: %s at pos: %d\nsql: %s", value, pos.get() - 1, getSql()), e);
            }
        });
    }

    private Object sqlize(Object value) {
        if(value instanceof LocalDate)
            return DBAccess.convert((LocalDate)value);
        if(value instanceof ZonedDateTime)
            return DBAccess.convert((ZonedDateTime) value);
        return value;
    }
}
