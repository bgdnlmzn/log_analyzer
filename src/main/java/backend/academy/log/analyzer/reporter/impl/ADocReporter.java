package backend.academy.log.analyzer.reporter.impl;

import backend.academy.log.analyzer.arguments.CliArguments;
import backend.academy.log.analyzer.reporter.Reporter;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация Reporter для создания отчета в формате AsciiDoc
 */
@Slf4j
public class ADocReporter implements Reporter {
    private static final String TABLE_SEPARATOR = "|===\n";

    private static final String PIPE_SPACE = " | ";

    private static final String B_SUFFIX = "b\n";

    private static final String DOUBLE_NEWLINE = "\n\n";

    private final LogStatisticsCollector statistics;

    /**
     * Конструктор для создания экземпляра {@link ADocReporter}.
     *
     * @param statistics Экземпляр {@link LogStatisticsCollector}, содержащий статистику для отчета.
     */
    @Inject
    public ADocReporter(LogStatisticsCollector statistics) {
        this.statistics = statistics;
    }

    /**
     * Форматирует отчет в формате AsciiDoc и записывает его в указанный путь.
     *
     * @param outputPath Путь, по которому будет записан сформированный отчет в формате AsciiDoc.
     * @param cliArgs    Аргументы командной строки {@link CliArguments}.
     */
    @Override
    public void formatReport(String outputPath, CliArguments cliArgs) {
        StringBuilder sb = new StringBuilder();

        appendGeneralInfo(sb, cliArgs);
        appendRequestedResources(sb);
        appendResponseCodes(sb);
        appendRequestMethods(sb);

        try {
            Files.writeString(Path.of(outputPath), sb.toString());
        } catch (IOException e) {
            log.error("Error when writing a report to a file: {}", outputPath, e);
        }
    }

    /**
     * Добавляет в отчет раздел с общей информацией.
     *
     * @param sb      {@link StringBuilder}, в который добавляется информация для отчета.
     * @param cliArgs Аргументы командной строки {@link CliArguments} для получения информации о фильтрации.
     */
    private void appendGeneralInfo(StringBuilder sb, CliArguments cliArgs) {
        sb.append("==== Общая информация\n\n")
            .append(TABLE_SEPARATOR)
            .append("| Метрика | Значение\n")
            .append("| Файл(-ы) | `")
            .append(statistics.getFileNames()).append("`\n")
            .append("| Начальная дата | ")
            .append(cliArgs.from() != null ? cliArgs.from() : '-').append('\n')
            .append("| Конечная дата | ")
            .append(cliArgs.to() != null ? cliArgs.to() : '-').append('\n')
            .append("|  Поле для фильтрации  | ")
            .append(cliArgs.filterField() != null ? cliArgs.filterField() : "-").append('\n')
            .append("|Значение для фильтрации| ")
            .append(cliArgs.filterValue() != null ? cliArgs.filterValue() : "-").append('\n')
            .append("| Количество запросов | ")
            .append(statistics.totalRequests()).append('\n')
            .append("| Количество уникальных IP | ")
            .append(statistics.getUniqueIpCount()).append('\n')
            .append("| Средний размер ответа | ")
            .append(Math.round(statistics.averageResponseSize())).append(B_SUFFIX)
            .append("| 95p размера ответа | ")
            .append(statistics.percentile95ResponseSize()).append(B_SUFFIX)
            .append(TABLE_SEPARATOR).append(DOUBLE_NEWLINE);
    }

    /**
     * Добавляет в отчет раздел с запрашиваемыми ресурсами, включая название ресурса и количество запросов.
     *
     * @param sb {@link StringBuilder}, в который добавляется информация для отчета.
     */
    private void appendRequestedResources(StringBuilder sb) {
        sb.append("==== Запрашиваемые ресурсы\n\n")
            .append(TABLE_SEPARATOR)
            .append("| Ресурс | Количество\n");
        statistics.getTopResources().forEach(entry -> sb.append("| ").append(entry.getKey())
            .append(PIPE_SPACE)
            .append(entry.getValue()).append('\n'));
        sb.append(TABLE_SEPARATOR).append(DOUBLE_NEWLINE);
    }

    /**
     * Добавляет в отчет раздел с кодами ответов, включая код, имя и количество каждого кода ответа.
     *
     * @param sb {@link StringBuilder}, в который добавляется информация для отчета.
     */
    private void appendResponseCodes(StringBuilder sb) {
        sb.append("==== Коды ответа\n\n")
            .append(TABLE_SEPARATOR)
            .append("| Код | Имя | Количество\n");
        statistics.getTopStatuses().forEach(entry -> {
            String statusName = statistics.getStatusName(entry.getKey());
            sb.append("| ").append(entry.getKey()).append(PIPE_SPACE)
                .append(statusName).append(PIPE_SPACE)
                .append(entry.getValue()).append('\n');
        });
        sb.append(TABLE_SEPARATOR).append(DOUBLE_NEWLINE);
    }

    /**
     * Добавляет в отчет раздел с методами запросов, включая название метода и количество его вхождений.
     *
     * @param sb {@link StringBuilder}, в который добавляется информация для отчета.
     */
    private void appendRequestMethods(StringBuilder sb) {
        sb.append("==== Методы запросов\n\n")
            .append(TABLE_SEPARATOR)
            .append("| Метод | Количество\n");
        statistics.getMethods().forEach(entry -> {
            sb.append("| ").append(entry.getKey())
                .append(PIPE_SPACE)
                .append(entry.getValue())
                .append('\n');
        });
        sb.append(TABLE_SEPARATOR);
    }
}
