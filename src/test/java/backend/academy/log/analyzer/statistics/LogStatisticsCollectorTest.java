package backend.academy.log.analyzer.statistics;

import backend.academy.log.analyzer.entry.LogEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Тесты LogStatisticsCollector")
public class LogStatisticsCollectorTest {

    @Mock
    private LogEntry firstLogEntry;

    @Mock
    private LogEntry secondLogEntry;

    @Mock
    private LogEntry thirdLogEntry;

    private LogStatisticsCollector collector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collector = new LogStatisticsCollector();
    }

    @Test
    @DisplayName("Проверка корректного подсчета статистики")
    public void shouldComputeStatisticsCorrectly() {
        when(firstLogEntry.remoteAddr()).thenReturn("192.168.1.1");
        when(secondLogEntry.remoteAddr()).thenReturn("192.168.1.2");
        when(thirdLogEntry.remoteAddr()).thenReturn("192.168.1.1");

        when(firstLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");
        when(secondLogEntry.request()).thenReturn("POST /api/data HTTP/1.1");
        when(thirdLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");

        when(firstLogEntry.status()).thenReturn(200);
        when(secondLogEntry.status()).thenReturn(404);
        when(thirdLogEntry.status()).thenReturn(200);

        when(firstLogEntry.bodyBytesSent()).thenReturn(512L);
        when(secondLogEntry.bodyBytesSent()).thenReturn(972L);
        when(thirdLogEntry.bodyBytesSent()).thenReturn(256L);

        List<String> fileNames = List.of("test1.log", "test2.log");

        collector.computeStatistics(Stream.of(firstLogEntry, secondLogEntry, thirdLogEntry), fileNames);

        assertThat(collector.getUniqueIpCount()).isEqualTo(2);
        assertThat(collector.getFileNames()).isEqualTo("test1.log, test2.log");
        assertThat(collector.totalRequests()).isEqualTo(3);
        assertThat(collector.averageResponseSize()).isEqualTo(580);
        assertThat(collector.percentile95ResponseSize()).isEqualTo(926);
    }

    @Test
    @DisplayName("Корректное отображение топ ресурсов")
    public void shouldTrackTopResources() {
        when(firstLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");
        when(secondLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");
        when(thirdLogEntry.request()).thenReturn("POST /api/data HTTP/1.1");

        collector.computeStatistics(Stream.of(firstLogEntry, secondLogEntry, thirdLogEntry), List.of());

        List<Map.Entry<String, Long>> topResources = collector.getTopResources();
        assertThat(topResources)
            .hasSize(2)
            .containsExactly(
                Map.entry("/index.html", 2L),
                Map.entry("/api/data", 1L)
            );
    }

    @Test
    @DisplayName("Корректное отображение топ статусов")
    public void shouldTrackTopStatuses() {
        when(firstLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");
        when(secondLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");
        when(thirdLogEntry.request()).thenReturn("POST /api/data HTTP/1.1");

        when(firstLogEntry.status()).thenReturn(200);
        when(secondLogEntry.status()).thenReturn(200);
        when(thirdLogEntry.status()).thenReturn(404);

        collector.computeStatistics(Stream.of(firstLogEntry, secondLogEntry, thirdLogEntry), List.of());

        List<Map.Entry<Integer, Long>> topStatuses = collector.getTopStatuses();
        assertThat(topStatuses)
            .hasSize(2)
            .containsExactly(
                Map.entry(200, 2L),
                Map.entry(404, 1L)
            );
    }

    @Test
    @DisplayName("Корректное отображение топ методов")
    public void shouldTrackRequestMethods() {
        when(firstLogEntry.request()).thenReturn("GET /index.html HTTP/1.1");
        when(secondLogEntry.request()).thenReturn("POST /api/data HTTP/1.1");
        when(thirdLogEntry.request()).thenReturn("GET /about HTTP/1.1");

        collector.computeStatistics(Stream.of(firstLogEntry, secondLogEntry, thirdLogEntry), List.of());

        List<Map.Entry<String, Long>> methods = collector.getMethods();
        assertThat(methods)
            .hasSize(2)
            .containsExactly(
                Map.entry("GET", 2L),
                Map.entry("POST", 1L)
            );
    }

    @Test
    @DisplayName("Корректная обработка пустого стрима")
    public void shouldHandleEmptyInputStream() {
        collector.computeStatistics(Stream.empty(), List.of());

        assertThat(collector.totalRequests()).isEqualTo(0);
        assertThat(collector.averageResponseSize()).isEqualTo(0);
        assertThat(collector.percentile95ResponseSize()).isEqualTo(0);
        assertThat(collector.getUniqueIpCount()).isEqualTo(0);
        assertThat(collector.getTopResources()).isEmpty();
        assertThat(collector.getTopStatuses()).isEmpty();
        assertThat(collector.getMethods()).isEmpty();
    }

    @Test
    @DisplayName("Корректная обработка null полей в entry")
    public void shouldHandleNullValuesInLogEntry() {
        when(firstLogEntry.request()).thenReturn(null);
        when(firstLogEntry.remoteAddr()).thenReturn(null);
        when(firstLogEntry.status()).thenReturn(200);
        when(firstLogEntry.bodyBytesSent()).thenReturn(512L);

        collector.computeStatistics(Stream.of(firstLogEntry), List.of());

        assertThat(collector.getTopResources())
            .hasSize(1)
            .containsExactly(Map.entry("unknown", 1L));
        assertThat(collector.getUniqueIpCount()).isEqualTo(1);
    }
}
