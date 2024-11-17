package backend.academy.log.analyzer.reader;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Интерфейс для считывания логов из различных источников (файлы, URL).
 */
public interface LogReader {

    /**
     * Читает логи из указанного пути.
     *
     * @param path путь к файлу или URL
     * @return поток строк, представляющий логи
     * @throws IOException если произошла ошибка при чтении данных
     */
    Stream<String> readLogs(String path) throws IOException;

    /**
     * Возвращает список имен файлов, из которых были считаны логи.
     *
     * @return список имен файлов
     */
    List<String> getFileNames();
}
