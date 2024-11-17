package backend.academy.log.analyzer.statistics;

import backend.academy.log.analyzer.entry.LogEntry;
import com.google.common.math.Quantiles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс для сбора и вычисления статистики по логам.
 * Этот класс анализирует данные, полученные из логов, и предоставляет информацию о таких метриках,
 * как количество уникальных IP-адресов,
 * частота использования ресурсов, кодов ответа, методов запросов и другие показатели.
 */
@Getter
@Slf4j
public class LogStatisticsCollector {

    /** Параметр для расчета 95-го процентиля размера ответа */
    private static final int PERCENTILE = 95;

    /** Лимит для отображения топ-ресурсов и топ-статусов */
    private static final int TOP_LIMIT = 3;

    /** Значение по умолчанию для неизвестных значений */
    private static final String UNKNOWN = "unknown";

    /** Общее количество запросов */
    private long totalRequests;

    /** Частота запросов по ресурсам */
    private final Map<String, Long> resourceFrequency = new HashMap<>();

    /** Частота кодов состояния HTTP */
    private final Map<Integer, Long> statusFrequency = new HashMap<>();

    /** Частота HTTP-методов */
    private final Map<String, Long> methodFrequency = new HashMap<>();

    /** Средний размер ответа */
    private double averageResponseSize;

    /** Размер ответа для 95-го процентиля */
    private long percentile95ResponseSize;

    /** Набор лог-файлов, которые были обработаны */
    private final Set<String> logFiles = new HashSet<>();

    /** Общая сумма размеров ответов */
    private long responseSizeSum = 0;

    /** Список всех размеров ответов */
    private final List<Long> responseSizes = new ArrayList<>();

    /** Множество уникальных IP-адресов */
    private final Set<String> uniqueIpAddresses = new HashSet<>();

    /**
     * Вычисляет статистику по логам.
     *
     * @param entries   Поток лог-записей для обработки.
     * @param fileNames Список имен файлов логов.
     */
    public void computeStatistics(
        Stream<LogEntry> entries,
        List<String> fileNames
    ) {
        logFiles.addAll(fileNames);

        entries.forEach(entry -> {
            totalRequests++;
            countUniqueIp(entry);
            trackResourceFrequency(entry);
            trackStatusFrequency(entry);
            trackMethodFrequency(entry);
            accumulateResponseSizes(entry);
        });

        calculateAverageResponseSize();
        calculatePercentileResponseSize();

        log.info("Statistics have been calculated successfully. {} requests processed.", totalRequests);
    }

    /**
     * Считывает уникальные IP-адреса из логов.
     *
     * @param entry Лог-запись.
     */
    private void countUniqueIp(LogEntry entry) {
        uniqueIpAddresses.add(entry.remoteAddr());
    }

    /**
     * Отслеживает частоту использования ресурсов.
     *
     * @param entry Лог-запись.
     */
    private void trackResourceFrequency(LogEntry entry) {
        String resource = extractResourcePath(entry.request());
        resourceFrequency.merge(resource, 1L, Long::sum);
    }

    /**
     * Отслеживает частоту кодов состояния.
     *
     * @param entry Лог-запись.
     */
    private void trackStatusFrequency(LogEntry entry) {
        statusFrequency.merge(entry.status(), 1L, Long::sum);
    }

    /**
     * Отслеживает частоту использования HTTP-методов.
     *
     * @param entry Лог-запись.
     */
    private void trackMethodFrequency(LogEntry entry) {
        String method = extractMethod(entry.request());
        methodFrequency.merge(method, 1L, Long::sum);
    }

    /**
     * Накапливает размеры ответов для дальнейшего анализа.
     *
     * @param entry Лог-запись.
     */
    private void accumulateResponseSizes(LogEntry entry) {
        long responseSize = entry.bodyBytesSent();
        responseSizes.add(responseSize);
        responseSizeSum += responseSize;
    }

    /**
     * Вычисляет средний размер ответа.
     */
    private void calculateAverageResponseSize() {
        averageResponseSize = totalRequests > 0 ? Math.round((double) responseSizeSum / totalRequests) : 0;
    }

    /**
     * Вычисляет размер ответа для 95-го процентиля.
     */
    private void calculatePercentileResponseSize() {
        if (!responseSizes.isEmpty()) {
            percentile95ResponseSize = (long) Quantiles.percentiles()
                .index(PERCENTILE)
                .compute(responseSizes);
        }
    }

    /**
     * Возвращает количество уникальных IP-адресов.
     *
     * @return Количество уникальных IP-адресов.
     */
    public int getUniqueIpCount() {
        return uniqueIpAddresses.size();
    }

    /**
     * Извлекает путь ресурса из запроса.
     *
     * @param request HTTP-запрос.
     * @return Путь ресурса.
     */
    private String extractResourcePath(String request) {
        if (request == null || request.isBlank()) {
            return UNKNOWN;
        }
        int startIdx = request.indexOf(' ');
        int endIdx = request.indexOf(' ', startIdx + 1);
        return (startIdx != -1 && endIdx != -1) ? request.substring(startIdx + 1, endIdx) : request;
    }

    /**
     * Извлекает метод из HTTP-запроса.
     *
     * @param request HTTP-запрос.
     * @return Метод запроса.
     */
    private String extractMethod(String request) {
        if (request == null || request.isBlank()) {
            return UNKNOWN;
        }

        int endIdx = request.indexOf(' ');
        return (endIdx != -1) ? request.substring(0, endIdx) : UNKNOWN;
    }

    /**
     * Получает топ-ресурсов по частоте запросов.
     *
     * @return Список топ-ресурсов.
     */
    public List<Map.Entry<String, Long>> getTopResources() {
        return resourceFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(TOP_LIMIT)
            .collect(Collectors.toList());
    }

    /**
     * Получает топ-статусов по частоте появления.
     *
     * @return Список топ-статусов.
     */
    public List<Map.Entry<Integer, Long>> getTopStatuses() {
        return statusFrequency.entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .limit(TOP_LIMIT)
            .collect(Collectors.toList());
    }

    /**
     * Получает список HTTP-методов с их частотой.
     *
     * @return Список методов запросов.
     */
    public List<Map.Entry<String, Long>> getMethods() {
        return methodFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(TOP_LIMIT)
            .collect(Collectors.toList());
    }

    /**
     * Получает описание статуса по его коду.
     *
     * @param statusCode Код состояния HTTP.
     * @return Описание статуса.
     */
    public String getStatusName(int statusCode) {
        try {
            return HttpStatus.fromCode(statusCode).description();
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * Возвращает имена лог-файлов, которые были обработаны.
     *
     * @return Строка с именами лог-файлов.
     */
    public String getFileNames() {
        return String.join(", ", logFiles);
    }
}
