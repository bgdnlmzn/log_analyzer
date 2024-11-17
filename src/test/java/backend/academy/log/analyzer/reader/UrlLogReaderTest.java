package backend.academy.log.analyzer.reader;

import backend.academy.log.analyzer.reader.impl.UrlLogReader;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты UrlLogReader")
public class UrlLogReaderTest {

    private final UrlLogReader logReader = new UrlLogReader();

    @Test
    @DisplayName("Чтение логов из URL")
    public void testReadLogsFromUrl() {
        String validUrl =
            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs";

        try (Stream<String> logStream = logReader.readLogs(validUrl)) {
            List<String> logs = logStream.toList();

            assertFalse(logs.isEmpty());
            assertEquals(
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
                logs.getFirst());

            List<String> fileNames = logReader.getFileNames();
            assertFalse(fileNames.isEmpty());
            assertTrue(fileNames.getFirst().contains("nginx_logs"));
        }
    }

    @Test
    @DisplayName("Чтение логов из некорректного URL")
    public void testReadLogsFromInvalidUrl() {
        String invalidUrl = "https://invalid.url";

        try (Stream<String> logStream = logReader.readLogs(invalidUrl)) {
            List<String> logs = logStream.toList();

            assertTrue(logs.isEmpty());

            List<String> fileNames = logReader.getFileNames();
            assertTrue(fileNames.isEmpty());
        }
    }
}

