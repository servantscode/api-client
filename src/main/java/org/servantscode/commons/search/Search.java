package org.servantscode.commons.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Search {

    private List<SearchClause> clauses;

    public Search() {
        this.clauses = new ArrayList<>();
    }

    public void addClause(SearchClause clause) {
        this.clauses.add(clause);
    }

    public String getDBQueryString() {
        return clauses.stream().map(SearchClause::getQuery).collect(Collectors.joining(" AND "));
    }

    public static abstract class SearchClause {
        abstract String getQuery();
    }

    public static class TextClause extends SearchClause {
        private final String field;
        private final String value;

        public TextClause(String field, String value) {
            this.field = field;
            this.value = value;
        }

        @Override
        String getQuery() {
            return String.format("%s ILIKE '%%%s%%'", field, value.replace("'", "''"));
        }
    }

    public static class BooleanClause extends SearchClause {
        private final String field;
        private final boolean value;

        public BooleanClause(String field, boolean value) {
            this.field = field;
            this.value = value;
        }

        @Override
        String getQuery() {
            return String.format("%s=%s", field, Boolean.toString(value));
        }
    }

    public static class DateClause extends SearchClause {
        private final String field;
        private final LocalDate value;

        public DateClause(String field, LocalDate value) {
            this.field = field;
            this.value = value;
        }

        @Override
        String getQuery() {
            return String.format("%s > '%s' AND %s < '%s'",
                    field, value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00")),
                    field, value.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00")));
        }
    }

    public static class DateRangeClause extends SearchClause {
        private final String field;
        private final LocalDate start;
        private final LocalDate end;

        public DateRangeClause(String field, LocalDate start, LocalDate end) {
            this.field = field;
            this.start = start;
            this.end = end;
        }

        @Override
        String getQuery() {
            return String.format("%s > '%s' AND %s < '%s'",
                    field, start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00")),
                    field, end.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00")));
        }
    }
}
