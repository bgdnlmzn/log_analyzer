package backend.academy.log.analyzer.reporter.impl;

import backend.academy.log.analyzer.arguments.CliArguments;
import backend.academy.log.analyzer.reporter.Reporter;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация Reporter для создания отчета в формате Markdown
 */
@Slf4j
public class MarkdownReporter implements Reporter {

    private static final String PIPE_SPACE = " | ";

    private static final String TABLE_BODY_SEPARATOR_TWO_COLS =
        "|:-------------------:|:------------:|\n";

    private static final String TABLE_HEADER_SEPARATOR_THREE_COLS =
        "|:-------------------:|:----------------:|:-------------:|\n";

    private static final String SUFFIX_B = " b|\n";

    private static final String CELL_START = "| ";

    private static final String CELL_END = " |\n";

    private final LogStatisticsCollector statistics;

    /**
     * Конструктор для создания экземпляра {@link MarkdownReporter}.
     *
     * @param statistics Экземпляр {@link LogStatisticsCollector}, содержащий статистику для отчета.
     */
    @Inject
    public MarkdownReporter(LogStatisticsCollector statistics) {
        this.statistics = statistics;
    }

    /**
     * Форматирует отчет в формате Markdown и записывает его в указанный путь.
     *
     * @param outputPath Путь, по которому будет записан сформированный отчет в формате Markdown.
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
            Files.writeString(Path.of(outputPath), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error when writing a report to a file: {}", outputPath);
        }
    }

    /**
     * Добавляет в отчет раздел с общей информацией.
     *
     * @param sb      {@link StringBuilder}, в который добавляется информация для отчета.
     * @param cliArgs Аргументы командной строки {@link CliArguments} для получения информации о фильтрации.
     */
    private void appendGeneralInfo(StringBuilder sb, CliArguments cliArgs) {
        sb.append("#### Общая информация\n\n")
            .append("|        Метрика        |     Значение      |\n")
            .append(TABLE_BODY_SEPARATOR_TWO_COLS)
            .append("|       Файл(-ы)        | `")
            .append(String.join(", ", statistics.getFileNames())).append("`|\n")
            .append("|    Начальная дата     | ")
            .append(cliArgs.from() != null ? cliArgs.from() : "-").append(CELL_END)
            .append("|     Конечная дата     | ")
            .append(cliArgs.to() != null ? cliArgs.to() : "-").append(CELL_END)
            .append("|  Поле для фильтрации  | ")
            .append(cliArgs.filterField() != null ? cliArgs.filterField() : "-").append(CELL_END)
            .append("|Значение для фильтрации| ")
            .append(cliArgs.filterValue() != null ? cliArgs.filterValue() : "-").append(CELL_END)
            .append("|  Количество запросов  | ")
            .append(statistics.totalRequests()).append(CELL_END)
            .append("| Количество уникальных IP | ")
            .append(statistics.getUniqueIpCount()).append(CELL_END)
            .append("| Средний размер ответа | ")
            .append(Math.round(statistics.averageResponseSize())).append(SUFFIX_B)
            .append("|   95p размера ответа  | ")
            .append(statistics.percentile95ResponseSize()).append(SUFFIX_B);
    }

    /**
     * Добавляет в отчет раздел с запрашиваемыми ресурсами, включая название ресурса и количество запросов.
     *
     * @param sb {@link StringBuilder}, в который добавляется информация для отчета.
     */
    private void appendRequestedResources(StringBuilder sb) {
        sb.append("#### Запрашиваемые ресурсы\n\n")
            .append("|     Ресурс      | Количество |\n")
            .append(TABLE_BODY_SEPARATOR_TWO_COLS);
        statistics.getTopResources().forEach(entry -> sb.append(CELL_START).append(entry.getKey())
            .append(PIPE_SPACE)
            .append(entry.getValue())
            .append(CELL_END));
    }

    /**
     * Добавляет в отчет раздел с кодами ответов, включая код, имя и количество каждого кода ответа.
     *
     * @param sb {@link StringBuilder}, в который добавляется информация для отчета.
     */
    private void appendResponseCodes(StringBuilder sb) {
        sb.append("\n#### Коды ответа\n\n")
            .append("| Код |          Имя          | Количество |\n")
            .append(TABLE_HEADER_SEPARATOR_THREE_COLS);
        statistics.getTopStatuses().forEach(entry -> {
            String statusName = statistics.getStatusName(entry.getKey());
            sb.append(CELL_START).append(entry.getKey())
                .append(PIPE_SPACE)
                .append(String.format("%-20s", statusName))
                .append(PIPE_SPACE)
                .append(entry.getValue())
                .append(CELL_END);
        });
    }

    /**
     * Добавляет в отчет раздел с методами запросов, включая название метода и количество его вхождений.
     *
     * @param sb {@link StringBuilder}, в который добавляется информация для отчета.
     */
    private void appendRequestMethods(StringBuilder sb) {
        sb.append("\n#### Методы запросов\n\n")
            .append("| Метод | Количество |\n")
            .append(TABLE_BODY_SEPARATOR_TWO_COLS);
        statistics.getMethods().forEach(entry -> {
            sb.append(CELL_START).append(entry.getKey())
                .append(PIPE_SPACE)
                .append(entry.getValue())
                .append(CELL_END);
        });
    }
}
