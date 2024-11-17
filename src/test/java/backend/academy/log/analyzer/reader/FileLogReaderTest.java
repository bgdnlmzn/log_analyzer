package backend.academy.log.analyzer.reader;

import backend.academy.log.analyzer.reader.impl.FileLogReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Тесты FileLogReader")
public class FileLogReaderTest {

    private final FileLogReader fileLogReader = new FileLogReader();

    @Test
    @DisplayName("Чтение логов из одного файла")
    public void testReadSingleLogFile() throws IOException {
        Path tempLogFile = Files.createTempFile("test_log_", ".log");
        Files.writeString(tempLogFile,
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET / HTTP/1.1\" 200 123 \"-\" \"Test-Agent\"");

        try (Stream<String> logStream = fileLogReader.readLogs(tempLogFile.toString())) {
            List<String> logs = logStream.toList();

            assertEquals(1, logs.size());
            assertEquals("93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET / HTTP/1.1\" 200 123 \"-\" \"Test-Agent\"",
                logs.getFirst());
        } finally {
            Files.deleteIfExists(tempLogFile);
        }
    }

    @Test
    @DisplayName("Чтение логов с использованием глоб-паттерна")
    public void testReadLogsWithGlobPattern() throws IOException {
        Path tempDirectory = Files.createTempDirectory("test_logs_");
        Path tempLogFile1 = tempDirectory.resolve("test_log1.log");
        Path tempLogFile2 = tempDirectory.resolve("test_log2.log");
        Files.writeString(tempLogFile1,
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET / HTTP/1.1\" 200 123 \"-\" \"Some-Agent\"");
        Files.writeString(tempLogFile2,
            "192.168.1.1 - - [18/May/2015:08:05:32 +0000] \"POST /data HTTP/1.1\" 404 456 \"-\" \"Some-Agent\"");

        String globPattern = tempDirectory + "/*.log";

        try (Stream<String> logStream = fileLogReader.readLogs(globPattern)) {
            List<String> logs = logStream.toList();

            assertEquals(2, logs.size());
            assertTrue(logs.contains(
                "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET / HTTP/1.1\" 200 123 \"-\" \"Some-Agent\""));
            assertTrue(logs.contains(
                "192.168.1.1 - - [18/May/2015:08:05:32 +0000] \"POST /data HTTP/1.1\" 404 456 \"-\" \"Some-Agent\""));
        } finally {
            Files.deleteIfExists(tempLogFile1);
            Files.deleteIfExists(tempLogFile2);
            Files.deleteIfExists(tempDirectory);
        }
    }

    @Test
    @DisplayName("Обработка пустой директории с глоб-паттерном")
    public void testReadLogsFromEmptyDirectoryWithGlob() throws IOException {
        Path tempDirectory = Files.createTempDirectory("empty_logs_");
        String globPattern = tempDirectory.toString() + "/*.log";

        try (Stream<String> logStream = fileLogReader.readLogs(globPattern)) {
            List<String> logs = logStream.toList();

            assertTrue(logs.isEmpty());
        } finally {
            Files.deleteIfExists(tempDirectory);
        }
    }

    @Test
    @DisplayName("Чтение из несуществующего файла")
    public void testReadLogsFromNonexistentFile() {
        String nonexistentPath = "nonexistent_file.log";

        try (Stream<String> logStream = fileLogReader.readLogs(nonexistentPath)) {
            List<String> logs = logStream.toList();

            assertTrue(logs.isEmpty());
        } catch (IOException e) {
            fail("Ошибка не должна выбрасываться для несуществующего файла");
        }
    }
}
