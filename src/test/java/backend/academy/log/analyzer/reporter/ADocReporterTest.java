package backend.academy.log.analyzer.reporter;

import backend.academy.log.analyzer.arguments.CliArguments;
import backend.academy.log.analyzer.reporter.impl.ADocReporter;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Тесты для ADocReporter")
public class ADocReporterTest {

    @Mock
    private LogStatisticsCollector statistics;

    @Mock
    private CliArguments cliArgs;

    private ADocReporter reporter;

    private Path tempFile;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        reporter = new ADocReporter(statistics);
        tempFile = Files.createTempFile("test-adoc-report", ".adoc");
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("Проверка генерации общего отчета")
    public void shouldGenerateGeneralReport() throws Exception {
        when(cliArgs.path()).thenReturn("/logs");
        when(cliArgs.from()).thenReturn("2024-01-01");
        when(cliArgs.to()).thenReturn("2024-12-31");
        when(cliArgs.filterField()).thenReturn("status");
        when(cliArgs.filterValue()).thenReturn("200");

        when(statistics.getFileNames()).thenReturn("log1.txt, log2.txt");
        when(statistics.totalRequests()).thenReturn(10L);
        when(statistics.getUniqueIpCount()).thenReturn(5);
        when(statistics.averageResponseSize()).thenReturn(512.5);
        when(statistics.percentile95ResponseSize()).thenReturn(1024L);

        reporter.formatReport(tempFile.toString(), cliArgs);

        String reportContent = Files.readString(tempFile, StandardCharsets.UTF_8);

        String expectedReport = """
            |===
            | Метрика | Значение
            | Файл(-ы) | `log1.txt, log2.txt`
            | Начальная дата | 2024-01-01
            | Конечная дата | 2024-12-31
            |  Поле для фильтрации  | status
            |Значение для фильтрации| 200
            | Количество запросов | 10
            | Количество уникальных IP | 5
            | Средний размер ответа | 513b
            | 95p размера ответа | 1024b
            |===""";

        assertThat(reportContent).contains(expectedReport);
    }

    @Test
    @DisplayName("Проверка добавления запрашиваемых ресурсов")
    public void shouldIncludeRequestedResources() throws Exception {
        when(statistics.getTopResources()).thenReturn(List.of(
            Map.entry("/index.html", 5L),
            Map.entry("/api/data", 3L)
        ));

        reporter.formatReport(tempFile.toString(), cliArgs);

        String reportContent = Files.readString(tempFile, StandardCharsets.UTF_8);

        assertThat(reportContent).contains("""
            ==== Запрашиваемые ресурсы

            |===
            | Ресурс | Количество
            | /index.html | 5
            | /api/data | 3
            |===

            """);
    }

    @Test
    @DisplayName("Проверка добавления кодов ответа")
    public void shouldIncludeResponseCodes() throws Exception {
        when(statistics.getTopStatuses()).thenReturn(List.of(
            Map.entry(200, 7L),
            Map.entry(404, 2L)
        ));
        when(statistics.getStatusName(200)).thenReturn("OK");
        when(statistics.getStatusName(404)).thenReturn("Not Found");

        reporter.formatReport(tempFile.toString(), cliArgs);

        String reportContent = Files.readString(tempFile, StandardCharsets.UTF_8);

        assertThat(reportContent).contains("""
            ==== Коды ответа

            |===
            | Код | Имя | Количество
            | 200 | OK | 7
            | 404 | Not Found | 2
            |===

            """);
    }

    @Test
    @DisplayName("Проверка добавления методов запросов")
    public void shouldIncludeRequestMethods() throws Exception {
        when(statistics.getMethods()).thenReturn(List.of(
            Map.entry("GET", 8L),
            Map.entry("POST", 2L)
        ));

        reporter.formatReport(tempFile.toString(), cliArgs);

        String reportContent = Files.readString(tempFile, StandardCharsets.UTF_8);

        assertThat(reportContent).contains(
            """
            |===
            | Метод | Количество
            | GET | 8
            | POST | 2
            |===
            """);
    }

    @Test
    @DisplayName("Обработка ошибок при записи файла")
    public void shouldHandleFileWriteError() {
        Path invalidPath = Path.of("/invalid/path/report.adoc");

        reporter.formatReport(invalidPath.toString(), cliArgs);

        assertThat(Files.exists(invalidPath)).isFalse();
    }
}
