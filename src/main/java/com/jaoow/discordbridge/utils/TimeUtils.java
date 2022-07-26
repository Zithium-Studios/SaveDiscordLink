package com.jaoow.discordbridge.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TimeUtils {

    DAY(86400000, "days", "day", "d", "day", "days"),
    HOUR(3600000, "hours", "hour", "h", "hora", "horas"),
    MINUTE(60000, "minutes", "minute", "m", "minuto", "minutos"),
    SECOND(1000, "seconds", "second", "s", "segundo", "segundos");

    private static final Pattern PATTERN = Pattern.compile("(\\d+)([a-zA-Z]+)");

    private final long millis;
    private final String[] formats;

    TimeUtils(long millis, String... formats) {
        this.millis = millis;
        this.formats = formats;
    }

    public static Duration getDuration(String string) {
        Matcher matcher = PATTERN.matcher(string);
        long time = 0;

        while (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                TimeUtils type = getTimeFromFormats(matcher.group(2));
                if (type != null) time += (value * type.millis);
            } catch (Exception ignored) {
            }
        }

        return Duration.ofMillis(time);
    }

    public static String format(long time) {
        if (time <= 0) return "now";

        long days = TimeUnit.MILLISECONDS.toDays(time);
        long hours = TimeUnit.MILLISECONDS.toHours(time) - (days * 24);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - (TimeUnit.MILLISECONDS.toHours(time) * 60);
        long second = TimeUnit.MILLISECONDS.toSeconds(time) - (TimeUnit.MILLISECONDS.toMinutes(time) * 60);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (second > 0) sb.append(second).append("s");

        String s = sb.toString();
        return s.isEmpty() ? "0s" : s;
    }

    public static String formatTime(long time) {
        if (time <= 0) return "now";

        long days = TimeUnit.MILLISECONDS.toDays(time);
        long hours = TimeUnit.MILLISECONDS.toHours(time) - (days * 24);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - (TimeUnit.MILLISECONDS.toHours(time) * 60);
        long second = TimeUnit.MILLISECONDS.toSeconds(time) - (TimeUnit.MILLISECONDS.toMinutes(time) * 60);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(days == 1 ? " day" : " days");
        if (hours > 0) sb.append(days > 0 ? (minutes > 0 ? ", " : " and ") : "").append(hours).append(hours == 1 ? " hour" : " hours");
        if (minutes > 0) sb.append(days > 0 || hours > 0 ? (second > 0 ? ", " : " and ") : "").append(minutes).append(minutes == 1 ? " minute" : " minutes");
//        if (second > 0) sb.append(days > 0 || hours > 0 || minutes > 0 ? " and " : (sb.length() > 0 ? ", " : "")).append(second).append(second == 1 ? " second" : " seconds");

        String s = sb.toString();
        return s.isEmpty() ? "now" : s;
    }

    private static TimeUtils getTimeFromFormats(String format) {
        return Arrays.stream(TimeUtils.values())
                .filter(type -> Arrays.asList(type.formats).contains(format.toLowerCase()))
                .findFirst().orElse(null);
    }
}