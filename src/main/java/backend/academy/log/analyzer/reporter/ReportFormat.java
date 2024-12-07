package backend.academy.log.analyzer.reporter;

import backend.academy.log.analyzer.reporter.impl.ADocReporter;
import backend.academy.log.analyzer.reporter.impl.MarkdownReporter;
import backend.academy.log.analyzer.statistics.LogStatisticsCollector;
import java.util.Arrays;
import java.util.function.Function;

public enum ReportFormat {
    MARKDOWN("markdown", MarkdownReporter::new),
    ASCIIDOC("adoc", ADocReporter::new);

    private final String format;
    private final Function<LogStatisticsCollector, Reporter> reporterFactory;

    ReportFormat(String format, Function<LogStatisticsCollector, Reporter> reporterFactory) {
        this.format = format;
        this.reporterFactory = reporterFactory;
    }

    public static ReportFormat fromString(String format) {
        return Arrays.stream(values())
            .filter(f -> f.format.equalsIgnoreCase(format))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));
    }

    public Reporter createReporter(LogStatisticsCollector statistics) {
        return reporterFactory.apply(statistics);
    }
}

