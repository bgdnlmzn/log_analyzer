package backend.academy.log.analyzer.arguments;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс для проверки валидности аргументов командной строки.
 * Выполняет проверки пути, дат, формата вывода и параметров фильтрации.
 */
@Slf4j
public class Validator {

    /**
     * Список поддерживаемых форматов вывода.
     */
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("markdown", "adoc");

    /**
     * Список поддерживаемых полей для фильтрации.
     */
    private static final List<String> SUPPORTED_FILTER_FIELDS =
        Arrays.asList(
            "remote_addr",
            "remote_user",
            "time_local",
            "request",
            "status",
            "body_bytes_sent",
            "http_referer",
            "http_user_agent"
        );

    /**
     * Выполняет валидацию всех параметров командной строки.
     *
     * @param cliArgs объект с аргументами командной строки
     * @return true, если все параметры валидны; иначе false
     */
    public boolean validate(CliArguments cliArgs) {
        return validatePath(cliArgs.path())
            && validateDate(cliArgs.from())
            && validateDate(cliArgs.to())
            && validateFormat(cliArgs.format())
            && validateFilter(cliArgs.filterField(), cliArgs.filterValue());
    }

    /**
     * Проверяет валидность пути к логам.
     *
     * @param path путь к логам
     * @return true, если путь задан; иначе false
     */
    private boolean validatePath(String path) {
        if (path == null || path.isEmpty()) {
            log.error("The path cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Проверяет валидность формата даты (ISO8601).
     *
     * @param date строка с датой
     * @return true, если дата валидна или отсутствует; иначе false
     */
    private boolean validateDate(String date) {
        if (date == null) {
            return true;
        }
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            log.error("Date format is incorrect. Use ISO8601");
            return false;
        }
    }

    /**
     * Проверяет валидность формата вывода.
     *
     * @param format формат вывода
     * @return true, если формат поддерживается; иначе false
     */
    private boolean validateFormat(String format) {
        if (format == null || format.isEmpty()) {
            log.error("Output format cannot be empty");
            return false;
        }
        if (!SUPPORTED_FORMATS.contains(format.toLowerCase())) {
            log.error("Invalid format. Available formats: markdown, adoc");
            return false;
        }
        return true;
    }

    /**
     * Проверяет валидность фильтров.
     *
     * @param filterField поле для фильтрации
     * @param filterValue значение для фильтрации
     * @return true, если фильтры валидны; иначе false
     */
    private boolean validateFilter(String filterField, String filterValue) {
        if ((filterField == null || filterField.isEmpty()) && (filterValue == null || filterValue.isEmpty())) {
            return true;
        }

        if ((filterField == null || filterField.isEmpty()) || (filterValue == null || filterValue.isEmpty())) {
            log.error("Both filter field and filter value must be provided together or both must be empty.");
            return false;
        }

        if (!SUPPORTED_FILTER_FIELDS.contains(filterField.toLowerCase())) {
            log.error("Invalid field for the filter. Available fields: {}", SUPPORTED_FILTER_FIELDS);
            return false;
        }

        return true;
    }
}
