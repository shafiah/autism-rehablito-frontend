
package com.autismcentre.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class MessageDateTimeUtils {

    private MessageDateTimeUtils() {
        // Utility class
    }

    public static String formatTimestamp(String timestamp) {

        if (timestamp == null || timestamp.trim().isEmpty()) {
            return "";
        }

        String value = timestamp.trim();

        String[] inputFormats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String pattern : inputFormats) {
            try {
                SimpleDateFormat parser =
                        new SimpleDateFormat(pattern, Locale.US);

                parser.setLenient(false);

                // Backend timestamps without timezone are treated as UTC.
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = parser.parse(value);

                if (date != null) {
                    SimpleDateFormat formatter =
                            new SimpleDateFormat(
                                    "EEEE, dd MMM yyyy • h:mm a",
                                    Locale.ENGLISH
                            );

                    formatter.setTimeZone(
                            TimeZone.getTimeZone("Asia/Kolkata")
                    );

                    return formatter.format(date);
                }

            } catch (ParseException ignored) {
                // Try next format
            }
        }

        return timestamp;
    }
}
