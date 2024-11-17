package backend.academy.log.analyzer.reader.factory;

import backend.academy.log.analyzer.reader.LogReader;
import backend.academy.log.analyzer.reader.impl.FileLogReader;
import backend.academy.log.analyzer.reader.impl.UrlLogReader;

/**
 * Фабрика для создания объектов {@link LogReader} в зависимости от источника данных (файл или URL).
 */
public class LogReaderFactory {

    /**
     * Создает объект {@link LogReader} на основе пути.
     * Если путь начинается с "http://", "https://" или "ftp://", создается объект {@link UrlLogReader}.
     * Иначе создается объект {@link FileLogReader}.
     *
     * @param path путь к логам (может быть URL или путь к файлу)
     * @return объект {@link LogReader} для чтения логов
     */
    public LogReader create(String path) {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("ftp://")) {
            return new UrlLogReader();
        } else {
            return new FileLogReader();
        }
    }
}

