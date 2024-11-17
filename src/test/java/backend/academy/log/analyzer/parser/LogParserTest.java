package backend.academy.log.analyzer.parser;

import backend.academy.log.analyzer.entry.LogEntry;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LogParserTest {

    private final LogParser logParser = new LogParser();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
        .ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    @Test
    @DisplayName("Корректный лог: успешный разбор всех полей")
    void testParseValidLog() {
        String logLine = "109.234.3.35 - - [02/Jun/2015:15:06:00 +0000] " +
            "\"GET /downloads/product_2 HTTP/1.1\" 404 336 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"";

        LogEntry entry = logParser.parse(logLine);

        assertNotNull(entry);
        assertEquals("109.234.3.35", entry.remoteAddr());
        assertNull(entry.remoteUser());
        assertEquals(ZonedDateTime.parse("02/Jun/2015:15:06:00 +0000", TIME_FORMATTER), entry.timeLocal());
        assertEquals("GET /downloads/product_2 HTTP/1.1", entry.request());
        assertEquals(404, entry.status());
        assertEquals(336, entry.bodyBytesSent());
        assertNull(entry.httpReferer());
        assertEquals("Debian APT-HTTP/1.3 (0.9.7.9)", entry.httpUserAgent());
    }

    @Test
    @DisplayName("Лог с недостающим полем: должен вернуть null")
    void testParseInvalidLogMissingField() {
        String logLine = "109.234.3.35 - - [02/Jun/2015:15:06:00 +0000] " +
            "\"GET /downloads/product_2 HTTP/1.1\" 404";

        LogEntry entry = logParser.parse(logLine);

        assertNull(entry);
    }

    @Test
    @DisplayName("Лог с некорректным временем: должен вернуть null")
    void testParseInvalidLogMalformedTime() {
        String logLine = "109.234.3.35 - - [invalid_time] " +
            "\"GET /downloads/product_2 HTTP/1.1\" 404 336 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"";

        LogEntry entry = logParser.parse(logLine);

        assertNull(entry);
    }

    @Test
    @DisplayName("Лог с некорректным статусом: должен вернуть null")
    void testParseInvalidLogMalformedStatus() {
        String logLine = "109.234.3.35 - - [02/Jun/2015:15:06:00 +0000] " +
            "\"GET /downloads/product_2 HTTP/1.1\" not_a_number 336 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"";

        LogEntry entry = logParser.parse(logLine);

        assertNull(entry);
    }

    @Test
    @DisplayName("Пустая строка: должен вернуть null")
    void testParseEmptyLog() {
        String logLine = "";

        LogEntry entry = logParser.parse(logLine);

        assertNull(entry);
    }

    @Test
    @DisplayName("Лог с корректным временем и отсутствующими необязательными полями")
    void testParseValidLogWithMissingOptionalFields() {
        String logLine = "109.234.3.35 - - [02/Jun/2015:15:06:00 +0000] " +
            "\"GET /downloads/product_2 HTTP/1.1\" 404 336 \"-\" \"-\"";

        LogEntry entry = logParser.parse(logLine);

        assertNotNull(entry);
        assertEquals("109.234.3.35", entry.remoteAddr());
        assertNull(entry.remoteUser());
        assertEquals(ZonedDateTime.parse("02/Jun/2015:15:06:00 +0000", TIME_FORMATTER), entry.timeLocal());
        assertEquals("GET /downloads/product_2 HTTP/1.1", entry.request());
        assertEquals(404, entry.status());
        assertEquals(336, entry.bodyBytesSent());
        assertNull(entry.httpReferer());
        assertNull(entry.httpUserAgent());
    }
}
