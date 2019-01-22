package org.servantscode.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class DateUtils {
    private static final Logger LOG = LogManager.getLogger(DateUtils.class);

    public static ZonedDateTime parse(String input) {
        return parse(input, (ZonedDateTime) null);
    }

    public static ZonedDateTime parse(String input, TemporalAdjuster adjuster) {
        return parse(input, ZonedDateTime.ofInstant(Instant.now().with(adjuster), ZoneId.systemDefault()));
    }

    public static ZonedDateTime parse(String input, ZonedDateTime defaultValue) {
        try {
            return ZonedDateTime.parse(input);
        } catch (Throwable t) {
            LOG.debug("Returning default date: " +
                    (defaultValue != null? defaultValue.format(ISO_OFFSET_DATE_TIME): "null"));
            return defaultValue;
        }
    }
}
