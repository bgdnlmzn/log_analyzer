package backend.academy;

import backend.academy.log.analyzer.app.AnalyzerApplication;
import backend.academy.log.analyzer.config.LogAnalyzerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new LogAnalyzerModule());
        AnalyzerApplication app = injector.getInstance(AnalyzerApplication.class);
        app.run(args);
    }
}
