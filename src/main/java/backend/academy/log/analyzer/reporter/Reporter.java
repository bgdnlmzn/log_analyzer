package backend.academy.log.analyzer.reporter;

import backend.academy.log.analyzer.arguments.CliArguments;

/**
 * Интерфейс для формирования отчетов в различных форматах.
 */
public interface Reporter {

    /**
     * Форматирует отчет в нужном формате и записывает его в указанный файл.
     *
     * @param outputPath Путь к файлу, в который будет записан отчет.
     * @param cliArgs    Аргументы командной строки {@link CliArguments}
     */
    void formatReport(String outputPath, CliArguments cliArgs);
}

