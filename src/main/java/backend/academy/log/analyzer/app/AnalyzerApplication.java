package backend.academy.log.analyzer.app;

import backend.academy.log.analyzer.arguments.CliArguments;
import backend.academy.log.analyzer.arguments.Validator;
import backend.academy.log.analyzer.handler.LogHandler;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Основной класс приложения для анализа логов.
 * Обрабатывает разбор аргументов CLI, их валидацию и обработку логов.
 */
@Slf4j
public class AnalyzerApplication {

    private final JCommander jCommander;
    private final Validator validator;
    private final LogHandler logHandler;

    /**
     * Создает экземпляр AnalyzerApplication с зависимостями.
     *
     * @param jCommander экземпляр JCommander для разбора аргументов CLI
     * @param validator  валидатор для проверки аргументов CLI
     * @param logHandler обработчик логов
     */
    @Inject
    public AnalyzerApplication(JCommander jCommander, Validator validator, LogHandler logHandler) {
        this.jCommander = jCommander;
        this.validator = validator;
        this.logHandler = logHandler;
    }

    /**
     * Запускает приложение: разбирает и валидирует аргументы CLI,
     * затем передает обработку логов обработчику LogHandler.
     *
     * @param args аргументы CLI
     */
    public void run(String[] args) {
        CliArguments cliArgs = new CliArguments();
        jCommander.addObject(cliArgs);

        try {
            jCommander.parse(args);

            if (!validator.validate(cliArgs)) {
                jCommander.usage();
                return;
            }

            logHandler.handle(cliArgs);
        } catch (ParameterException e) {
            log.error("Error in command line arguments: \n"
                + "unsupported argument(s) or no value entered");
            jCommander.usage();
        }
    }
}
