package backend.academy.log.analyzer.parser;

import backend.academy.log.analyzer.entry.LogEntry;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс для парсинга строк логов в форматах, соответствующих стандарту Nginx.
 * Преобразует строку лога в объект {@link LogEntry}.
 */
@Slf4j
public class LogParser {

    /**
     * Шаблон для извлечения данных из строки лога.
     */
    public static final Pattern LOG_PATTERN = Pattern.compile(
        "(?<remoteAddr>\\S+) - (?<remoteUser>-|\\S*) \\[(?<timeLocal>[^]]+)] \"(?<request>[^\"]*)\" "
            + "(?<status>\\d{3}) (?<bodyBytesSent>\\d+) \"(?<httpReferer>[^\"]*)\" \"(?<httpUserAgent>[^\"]*)\""
    );

    /**
     * Формат для парсинга времени в строках логов.
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
        .ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    /**
     * Парсит строку лога и возвращает объект {@link LogEntry}.
     * Если строка не соответствует ожидаемому формату, возвращает null.
     *
     * @param logLine строка лога
     * @return объект LogEntry, если строка лога валидна; иначе null
     */
    public LogEntry parse(String logLine) {
        Matcher matcher = LOG_PATTERN.matcher(logLine);
        if (!matcher.matches()) {
            return null;
        }

        try {
            return parseLogEntry(matcher);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Извлекает данные из совпавшей строки и создает объект {@link LogEntry}.
     *
     * @param matcher сопоставленный объект Matcher со строкой лога
     * @return объект LogEntry с разобранными значениями
     */
    private LogEntry parseLogEntry(Matcher matcher) {
        return new LogEntry(
            matcher.group("remoteAddr"),
            parseOptionalField(matcher, "remoteUser"),
            parseTime(matcher.group("timeLocal")),
            parseOptionalField(matcher, "request"),
            parseIntField(matcher, "status"),
            parseIntField(matcher, "bodyBytesSent"),
            parseOptionalField(matcher, "httpReferer"),
            parseOptionalField(matcher, "httpUserAgent")
        );
    }

    /**
     * Парсит опциональные поля, которые могут быть пустыми или содержать значение "-".
     * Если поле пустое или содержит "-", возвращает null.
     *
     * @param matcher   сопоставленный объект Matcher со строкой лога
     * @param fieldName имя поля для извлечения
     * @return значение поля или null, если оно пустое или равно "-"
     */
    private String parseOptionalField(Matcher matcher, String fieldName) {
        String field = matcher.group(fieldName);
        return (field == null || field.isEmpty() || "-".equals(field)) ? null : field;
    }

    /**
     * Преобразует строку с датой и временем в объект {@link ZonedDateTime}.
     *
     * @param time строка с временем в формате "dd/MMM/yyyy:HH:mm:ss Z"
     * @return объект ZonedDateTime
     */
    private ZonedDateTime parseTime(String time) {
        return ZonedDateTime.parse(time, TIME_FORMATTER);
    }

    /**
     * Преобразует строку в целочисленное значение.
     *
     * @param matcher   сопоставленный объект Matcher со строкой лога
     * @param fieldName имя поля для извлечения
     * @return целочисленное значение
     */
    private int parseIntField(Matcher matcher, String fieldName) {
        return Integer.parseInt(matcher.group(fieldName));
    }
}
