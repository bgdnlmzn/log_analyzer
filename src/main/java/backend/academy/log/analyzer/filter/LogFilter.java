package backend.academy.log.analyzer.filter;

import backend.academy.log.analyzer.entry.LogEntry;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

/**
 * Класс для фильтрации записей логов.
 * Позволяет проверять соответствие записей заданным полям, шаблонам и диапазону дат.
 */
public class LogFilter {

    /**
     * Мапа функций для извлечения значений из полей записи лога.
     */
    private static final Map<String, Function<LogEntry, String>> FIELD_EXTRACTORS = Map.of(
        "remote_addr", LogEntry::remoteAddr,
        "remote_user", LogEntry::remoteUser,
        "time_local", entry -> entry.timeLocal().toString(),
        "request", LogEntry::request,
        "status", entry -> String.valueOf(entry.status()),
        "body_bytes_sent", entry -> String.valueOf(entry.bodyBytesSent()),
        "http_referer", LogEntry::httpReferer,
        "http_user_agent", LogEntry::httpUserAgent
    );

    /**
     * Проверяет, соответствует ли запись лога заданному полю и регулярному выражению.
     *
     * @param entry        запись лога
     * @param field        имя поля для проверки
     * @param valuePattern регулярное выражение для проверки значения поля
     * @return true, если значение поля соответствует шаблону; иначе false
     */
    public boolean matches(LogEntry entry, String field, String valuePattern) {
        String value = getFieldValue(entry, field);
        if (value == null) {
            return false;
        }
        return value.matches(valuePattern);
    }

    /**
     * Проверяет, находится ли время записи в заданном диапазоне дат.
     *
     * @param logTime время записи лога
     * @param from    начальная дата диапазона (включительно, может быть null)
     * @param to      конечная дата диапазона (включительно, может быть null)
     * @return true, если время записи находится в диапазоне; иначе false
     */
    public boolean isWithinDateRange(ZonedDateTime logTime, LocalDate from, LocalDate to) {
        LocalDate logDate = logTime.toLocalDate();
        return (from == null || !logDate.isBefore(from)) && (to == null || !logDate.isAfter(to));
    }

    /**
     * Извлекает значение заданного поля из записи лога.
     *
     * @param entry запись лога
     * @param field имя поля
     * @return значение поля в виде строки или null, если поле не найдено
     */
    private String getFieldValue(LogEntry entry, String field) {
        Function<LogEntry, String> extractor = FIELD_EXTRACTORS.get(field);
        return extractor != null ? extractor.apply(entry) : null;
    }
}

