package backend.academy.log.analyzer.handler;

import backend.academy.log.analyzer.arguments.CliArguments;
import backend.academy.log.analyzer.entry.LogEntry;
import backend.academy.log.analyzer.filter.LogFilter;
import backend.academy.log.analyzer.parser.LogParser;
import backend.academy.log.analyzer.reader.LogReader;
import backend.academy.log.analyzer.reader.factory.LogReaderFactory;
import backend.academy.log.analyzer.reporter.Reporter;
import backend.academy.log.analyzer.reporter.factory.ReporterFactory;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс для обработки логов, включая чтение, фильтрацию, парсинг и генерацию отчетов.
 * Использует фабрики для создания необходимых компонентов и выполняет обработку логов по заданным параметрам.
 */
@Slf4j
public class LogHandler {

    private final LogReaderFactory logReaderFactory;

    private final ReporterFactory reporterFactory;

    private final LogParser logParser;

    private final LogStatisticsCollector logStatisticsCollector;

    private final LogFilter logFilter;

    /**
     * Конструктор для инъекции зависимостей.
     *
     * @param logReaderFactory       фабрика для создания лог-ридеров
     * @param reporterFactory        фабрика для создания репортеров
     * @param logParser              парсер для обработки строк логов
     * @param logStatisticsCollector сборщик статистики по логам
     * @param logFilter              фильтр для фильтрации логов
     */
    @Inject
    public LogHandler(
        LogReaderFactory logReaderFactory,
        ReporterFactory reporterFactory,
        LogParser logParser,
        LogStatisticsCollector logStatisticsCollector,
        LogFilter logFilter
    ) {
        this.logReaderFactory = logReaderFactory;
        this.reporterFactory = reporterFactory;
        this.logParser = logParser;
        this.logStatisticsCollector = logStatisticsCollector;
        this.logFilter = logFilter;
    }

    /**
     * Обрабатывает логи, начиная с чтения и фильтрации, заканчивая генерацией отчета.
     *
     * @param cliArgs аргументы командной строки с параметрами для фильтрации и формата отчета
     */
    public void handle(CliArguments cliArgs) {
        try {
            processLogs(cliArgs);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Обрабатывает логи, фильтрует и парсит их, а затем генерирует отчет.
     *
     * @param cliArgs аргументы командной строки с параметрами для фильтрации
     * @throws IOException            если произошла ошибка при чтении файла
     * @throws DateTimeParseException если произошла ошибка при парсинге даты
     */
    private void processLogs(CliArguments cliArgs) throws IOException, DateTimeParseException {
        LogReader logReader = logReaderFactory.create(cliArgs.path());

        try (Stream<String> logLines = logReader.readLogs(cliArgs.path())) {
            LocalDate fromDate = parseDate(cliArgs.from());
            LocalDate toDate = parseDate(cliArgs.to());

            Stream<LogEntry> entries = prepareLogEntries(
                logLines,
                fromDate,
                toDate,
                cliArgs.filterField(),
                cliArgs.filterValue()
            );

            generateReport(entries, logReader.getFileNames(), cliArgs);
        }
    }

    /**
     * Парсит строку с датой в формате ISO8601 в объект LocalDate.
     *
     * @param date строка с датой
     * @return объект LocalDate или null, если дата не указана
     */
    private LocalDate parseDate(String date) {
        return date != null ? LocalDate.parse(date) : null;
    }

    /**
     * Фильтрует и парсит строки логов, применяя фильтры по датам и полям.
     *
     * @param logLines    потока строк логов
     * @param from        начальная дата диапазона
     * @param to          конечная дата диапазона
     * @param filterField поле для фильтрации
     * @param filterValue значение для фильтрации
     * @return поток объектов LogEntry
     */
    private Stream<LogEntry> prepareLogEntries(
        Stream<String> logLines,
        LocalDate from,
        LocalDate to,
        String filterField,
        String filterValue
    ) {
        return logLines
            .map(logParser::parse)
            .filter(Objects::nonNull)
            .filter(entry -> logFilter.isWithinDateRange(entry.timeLocal(), from, to))
            .filter(entry ->
                filterField == null
                    || filterValue == null
                    || logFilter.matches(entry, filterField, ".*" + filterValue + ".*"));
    }

    /**
     * Генерирует отчет по обработанным логам.
     *
     * @param entries   поток объектов LogEntry
     * @param fileNames список имен файлов логов
     * @param cliArgs   аргументы командной строки для формирования отчета
     */
    private void generateReport(
        Stream<LogEntry> entries,
        List<String> fileNames,
        CliArguments cliArgs
    ) {
        logStatisticsCollector.computeStatistics(entries, fileNames);

        Reporter reporter = reporterFactory.createReporter(logStatisticsCollector, cliArgs.format());
        String outputPath = "report." + cliArgs.format();

        reporter.formatReport(outputPath, cliArgs);
        log.info("The report is saved to a file: {}", outputPath);
    }

    /**
     * Обрабатывает исключения, возникающие в процессе работы.
     *
     * @param e исключение
     */
    private void handleException(Exception e) {
        if (e instanceof DateTimeParseException) {
            log.error("Date parsing error");
        } else if (e instanceof IOException) {
            log.error("File reading error");
        } else {
            log.error("Error processing logs");
        }
    }
}
