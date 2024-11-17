package backend.academy.log.analyzer.reader.impl;

import backend.academy.log.analyzer.reader.LogReader;
import backend.academy.log.analyzer.statistics.HttpStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация интерфейса {@link LogReader}, считывающая логи из URL.
 */
@Slf4j
public class UrlLogReader implements LogReader {

    private final List<String> logFileName = new ArrayList<>(); // Список имен логов, считанных с URL

    /**
     * Читает логи из указанного URL.
     *
     * @param urlPath путь к URL
     * @return поток строк, представляющий логи
     */
    @Override
    public Stream<String> readLogs(String urlPath) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlPath))
            .GET()
            .build();

        log.info("A request is being made to the URL: {}", urlPath);

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != HttpStatus.OK.code()) {
                log.error("Failed to get data from URL: {}. Status: {}", urlPath, response.statusCode());
                return Stream.empty();
            }

            logFileName.add("File from URL: " + extractFileName(urlPath));

            Path tempFile = Files.createTempFile("log_data_", ".tmp");

            try (InputStream inputStream = response.body();
                 OutputStream outputStream = Files.newOutputStream(tempFile)) {
                inputStream.transferTo(outputStream); // Копирование данных
            }

            return Files.lines(tempFile).onClose(() -> {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.error("Error deleting temporary file: {}", tempFile, e);
                }
            });

        } catch (InterruptedException | IOException e) {
            log.error("Error when reading data from the URL: {}", urlPath);
            return Stream.empty();
        }
    }

    /**
     * Возвращает список имен файлов, полученных из URL.
     *
     * @return список имен файлов
     */
    @Override
    public List<String> getFileNames() {
        return logFileName;
    }

    /**
     * Извлекает имя файла из URL.
     *
     * @param urlPath путь к URL
     * @return имя файла из URL или "unknown", если имя не удалось извлечь
     */
    private String extractFileName(String urlPath) {
        int lastSlashIndex = urlPath.lastIndexOf('/');
        return (lastSlashIndex != -1 && lastSlashIndex < urlPath.length() - 1)
            ? urlPath.substring(lastSlashIndex + 1)
            : "unknown";
    }
}
