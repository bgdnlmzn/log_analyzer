package backend.academy.log.analyzer.config;

import backend.academy.log.analyzer.app.AnalyzerApplication;
import backend.academy.log.analyzer.arguments.Validator;
import backend.academy.log.analyzer.filter.LogFilter;
import backend.academy.log.analyzer.parser.LogParser;
import backend.academy.log.analyzer.reader.factory.LogReaderFactory;
import backend.academy.log.analyzer.reporter.factory.ReporterFactory;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;
import com.beust.jcommander.JCommander;
import com.google.inject.AbstractModule;

/**
 * Класс конфигурации зависимостей для приложения анализа логов.
 */
public class LogAnalyzerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LogParser.class).toInstance(new LogParser());

        bind(LogStatisticsCollector.class).toInstance(new LogStatisticsCollector());

        bind(LogFilter.class).toInstance(new LogFilter());

        bind(LogReaderFactory.class).toInstance(new LogReaderFactory());

        bind(Validator.class).toInstance(new Validator());

        bind(ReporterFactory.class).toInstance(new ReporterFactory());

        bind(JCommander.class).toInstance(JCommander.newBuilder().build());

        bind(AnalyzerApplication.class);
    }
}
