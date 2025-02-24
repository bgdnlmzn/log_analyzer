package backend.academy.log.analyzer.reporter.factory;

import backend.academy.log.analyzer.reporter.ReportFormat;
import backend.academy.log.analyzer.reporter.Reporter;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;

/**
 * Фабрика для создания отчетов в различных форматах (Markdown, ADoc).
 */
public class ReporterFactory {

    /**
     * Создает отчет в зависимости от выбранного формата.
     *
     * @param statistics статистика для отчета
     * @param format     формат отчета
     * @return созданный репортер, который форматирует отчет в указанном формате
     */
    public Reporter createReporter(LogStatisticsCollector statistics, String format) {
        ReportFormat reportFormat = ReportFormat.fromString(format);
        return reportFormat.createReporter(statistics);
    }
}
