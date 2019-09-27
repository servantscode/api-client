package org.servantscode.commons.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Search {
    private static final Logger LOG = LogManager.getLogger(Search.class);

    private List<SearchClause> clauses;

    public Search() {
        this.clauses = new ArrayList<>();
    }

    public void addClause(SearchClause clause) {
        this.clauses.add(clause);
    }

    public List<SearchClause> getClauses() {
        return clauses;
    }

    public static abstract class SearchClause {
        abstract String getSql();
        abstract List<Object> getValues();
    }

    public static class TextClause extends SearchClause {
        private final String field;
        private final String value;

        public TextClause(String field, String value) {
            if (field == null || value == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.value = value;
        }

        @Override
        public String getSql() { return String.format("%s ILIKE ?", field); }

        @Override
        public List<Object> getValues() { return asList(String.format("%%%s%%", value)); }
    }

    public static class EnumClause extends SearchClause {
        private final String field;
        private final String value;

        public EnumClause(String field, String value) {
            if (field == null || value == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.value = value.toUpperCase();
        }

        @Override
        public String getSql() { return String.format("%s=?", field); }

        @Override
        public List<Object> getValues() { return asList(value); }
    }

    public static class NumberClause extends SearchClause {
        private final String field;
        private final Number value;

        public NumberClause(String field, Number value) {
            if (field == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.value = value;
        }

        @Override
        public String getSql() { return String.format("%s = ?", field); }

        @Override
        public List<Object> getValues() { return asList(value); }
    }

    public static class NumberRangeClause extends SearchClause {
        private final String field;
        private final Number startValue;
        private final Number endValue;

        public NumberRangeClause(String field, Number startValue, Number endValue) {
            if (field == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.startValue = startValue;
            this.endValue = endValue;
        }

        @Override
        public String getSql() {
            String query = (startValue != null)? String.format("%s >= ?", field): "";
            query += (startValue != null && endValue != null)? " AND ": "";
            query +=  (endValue != null)? String.format("%s <= ?", field): "";
            return query;
        }

        @Override
        public List<Object> getValues() {
            List<Object> values = new ArrayList<>(2);
            if(startValue != null) values.add(startValue);
            if(endValue != null) values.add(endValue);
            return values;
        }
    }


    public static class BooleanClause extends SearchClause {
        private final String field;
        private final boolean value;

        public BooleanClause(String field, boolean value) {
            if (field == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.value = value;
        }

        @Override
        public String getSql() { return String.format("%s = ?", field); }

        @Override
        public List<Object> getValues() { return asList(value); }
    }

    public static class DateClause extends SearchClause {
        private final String field;
        private final LocalDate value;

        public DateClause(String field, LocalDate value) {
            if (field == null || value == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.value = value;
        }

        @Override
        public String getSql() { return String.format("%s > ? AND %s < ?", field, field); }

        @Override
        public List<Object> getValues() { return asList(value, value.plusDays(1)); }
    }

    public static class DateRangeClause extends SearchClause {
        private final String field;
        private final LocalDate start;
        private final LocalDate end;

        public DateRangeClause(String field, LocalDate start, LocalDate end) {
            if (field == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getSql() {
            String query = (start != null)? String.format("%s >= ?", field): "";
            query += (start != null && end != null)? " AND ": "";
            query +=  (end != null)? String.format("%s <= ?", field): "";
            return query;
        }

        @Override
        public List<Object> getValues() {
            List<Object> values = new ArrayList<>(2);
            if(start != null) values.add(convert(start));
            if(end != null) values.add(convert(end));
            return values;
        }

        protected Date convert(LocalDate date) {
            return date == null? null: Date.valueOf(date);
        }
    }

    public static class TimeRangeClause extends SearchClause {
        private final String field;
        private final ZonedDateTime start;
        private final ZonedDateTime end;

        public TimeRangeClause(String field, ZonedDateTime start, ZonedDateTime end) {
            if (field == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            this.field = field;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getSql() {
            String query = (start != null)? String.format("%s > ?", field): "";
            query += (start != null && end != null)? " AND ": "";
            query +=  (end != null)? String.format("%s < ?", field): "";
            return query;
        }

        @Override
        public List<Object> getValues() {
            List<Object> values = new ArrayList<>(2);
            if(start != null) values.add(convert(start));
            if(end != null) values.add(convert(end));
            return values;
        }

        private static Timestamp convert(ZonedDateTime input) {
            //Translate zone to UTC then save
            return input != null? Timestamp.valueOf(input.withZoneSameInstant(ZoneId.of("Z")).toLocalDateTime()): null;
        }
    }

    public static class ListItemClause extends SearchClause {
        private final String field;
        private final List<String> items;

        public ListItemClause(String field, List<String> items) {
            if (field == null || items == null) {
                throw new NullPointerException("Can't pass null value to clause");
            }
            if (items.size() == 0) {
                throw new IllegalArgumentException("List can't be of length 0");
            }
            for (String s : items) {
                if (s == null) {
                    throw new NullPointerException("Can't have null item in a list");
                }
            }
            this.field = field;
            this.items = items;
        }

        @Override
        public String getSql() {
            return "(" + items.stream().map(item -> String.format("? = any(%s)", field)).collect(Collectors.joining(" OR ")) + ")";
        }

        @Override
        public List<Object> getValues() {
            return new ArrayList<>(items);
        }
    }

}
