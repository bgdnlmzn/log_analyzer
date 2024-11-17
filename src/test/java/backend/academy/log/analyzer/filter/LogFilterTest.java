package backend.academy.log.analyzer.filter;

import backend.academy.log.analyzer.entry.LogEntry;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogFilterTest {

    private final LogFilter logFilter = new LogFilter();

    @Test
    @DisplayName("Фильтрация по диапазону времени: оба значения null")
    public void testDateRangeBothNull() {
        ZonedDateTime logTime = parseLogTime("02/Jun/2015:15:06:00 +0000");
        assertTrue(logFilter.isWithinDateRange(logTime, null, null));
    }

    @Test
    @DisplayName("Фильтрация по диапазону времени: в пределах диапазона")
    public void testDateRangeWithinBounds() {
        ZonedDateTime logTime = parseLogTime("02/Jun/2015:15:06:00 +0000");
        LocalDate from = LocalDate.of(2015, 6, 1);
        LocalDate to = LocalDate.of(2015, 6, 3);
        assertTrue(logFilter.isWithinDateRange(logTime, from, to));
    }

    @Test
    @DisplayName("Фильтрация по диапазону времени: за пределами диапазона (до начала)")
    public void testDateRangeBeforeStart() {
        ZonedDateTime logTime = parseLogTime("01/Jun/2015:15:06:00 +0000");
        LocalDate from = LocalDate.of(2015, 6, 2);
        LocalDate to = LocalDate.of(2015, 6, 3);
        assertFalse(logFilter.isWithinDateRange(logTime, from, to));
    }

    @Test
    @DisplayName("Фильтрация по диапазону времени: за пределами диапазона (после конца)")
    public void testDateRangeAfterEnd() {
        ZonedDateTime logTime = parseLogTime("04/Jun/2015:15:06:00 +0000");
        LocalDate from = LocalDate.of(2015, 6, 2);
        LocalDate to = LocalDate.of(2015, 6, 3);
        assertFalse(logFilter.isWithinDateRange(logTime, from, to));
    }

    @Test
    @DisplayName("Фильтрация по диапазону времени: только начало диапазона")
    public void testDateRangeOnlyStart() {
        ZonedDateTime logTime = parseLogTime("02/Jun/2015:15:06:00 +0000");
        LocalDate from = LocalDate.of(2015, 6, 2);
        assertTrue(logFilter.isWithinDateRange(logTime, from, null));
    }

    @Test
    @DisplayName("Фильтрация по диапазону времени: только конец диапазона")
    public void testDateRangeOnlyEnd() {
        ZonedDateTime logTime = parseLogTime("02/Jun/2015:15:06:00 +0000");
        LocalDate to = LocalDate.of(2015, 6, 3);
        assertTrue(logFilter.isWithinDateRange(logTime, null, to));
    }

    @Test
    @DisplayName("Фильтрация по полю: совпадение значения")
    public void testMatchesFieldValueMatches() {
        LogEntry entry = createLogEntry(
            "109.234.3.35",
            "-",
            "02/Jun/2015:15:06:00 +0000",
            "GET /downloads/product_2 HTTP/1.1",
            404,
            336,
            "-",
            "Debian APT-HTTP/1.3"
        );
        assertTrue(logFilter.matches(entry, "remote_addr", "109.234.3.35"));
    }

    @Test
    @DisplayName("Фильтрация по полю: совпадение значения c паттерном")
    public void testMatchesFieldValuePatternMatches() {
        LogEntry entry = createLogEntry(
            "109.234.3.35",
            "-",
            "02/Jun/2015:15:06:00 +0000",
            "GET /downloads/product_2 HTTP/1.1",
            404,
            336,
            "-",
            "Debian APT-HTTP/1.3"
        );
        assertTrue(logFilter.matches(entry, "http_user_agent", ".*Debian.*"));
    }

    @Test
    @DisplayName("Фильтрация по полю: значение не совпадает")
    public void testMatchesFieldValueDoesNotMatch() {
        LogEntry entry = createLogEntry(
            "109.234.3.35",
            "-",
            "02/Jun/2015:15:06:00 +0000",
            "GET /downloads/product_2 HTTP/1.1",
            404,
            336,
            "-",
            "Debian APT-HTTP/1.3"
        );
        assertFalse(logFilter.matches(entry, "remote_addr", "52.52.52.52"));
    }

    @Test
    @DisplayName("Фильтрация по полю: поле отсутствует в записи")
    public void testMatchesFieldNotPresent() {
        LogEntry entry = createLogEntry(
            "109.234.3.35",
            "-",
            "02/Jun/2015:15:06:00 +0000",
            "GET /downloads/product_2 HTTP/1.1",
            404,
            336,
            "-",
            "Debian APT-HTTP/1.3"
        );
        assertFalse(logFilter.matches(entry, "non_existent_field", ".*"));
    }

    @Test
    @DisplayName("Фильтрация по полю: значение null")
    public void testMatchesFieldValueIsNull() {
        LogEntry entry = createLogEntry(
            null,
            "-",
            "02/Jun/2015:15:06:00 +0000",
            "GET /downloads/product_2 HTTP/1.1",
            404,
            336,
            "-",
            "Debian APT-HTTP/1.3"
        );
        assertFalse(logFilter.matches(entry, "remote_addr", ".*"));
    }

    private ZonedDateTime parseLogTime(String timeLocal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
        return ZonedDateTime.parse(timeLocal, formatter);
    }

    private LogEntry createLogEntry(
        String remoteAddr, String remoteUser, String timeLocal,
        String request, int status, long bodyBytesSent,
        String httpReferer, String httpUserAgent
    ) {
        ZonedDateTime time = parseLogTime(timeLocal);
        return new LogEntry(remoteAddr, remoteUser, time, request, status, bodyBytesSent, httpReferer, httpUserAgent);
    }
}

