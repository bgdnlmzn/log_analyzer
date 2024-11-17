package backend.academy.log.analyzer.arguments;

import com.beust.jcommander.Parameter;
import lombok.Getter;

/**
 * Класс для хранения аргументов командной строки.
 * Используется для настройки пути к логам, диапазона дат, формата вывода и фильтров.
 */
@Getter
public class CliArguments {

    /**
     * Путь к логам (локальный файл или URL).
     */
    @Parameter(names = "--path", description = "The path to the logs (local or URL)", required = true)
    private String path;

    /**
     * Начальная дата в формате ISO8601.
     */
    @Parameter(names = "--from", description = "Start date in ISO8601 format")
    private String from;

    /**
     * Конечная дата в формате ISO8601.
     */
    @Parameter(names = "--to", description = "End date in ISO8601 format")
    private String to;

    /**
     * Формат вывода результата: markdown или adoc.
     * По умолчанию используется "markdown".
     */
    @Parameter(names = "--format", description = "Output format: markdown или adoc")
    private String format = "markdown";

    /**
     * Поле для фильтрации.
     */
    @Parameter(names = "--filter-field", description = "Field for filtering")
    private String filterField;

    /**
     * Значение для фильтрации.
     */
    @Parameter(names = "--filter-value", description = "Value for filtering")
    private String filterValue;
}
