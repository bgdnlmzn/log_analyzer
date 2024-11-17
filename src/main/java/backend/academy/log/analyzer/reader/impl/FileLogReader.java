package backend.academy.log.analyzer.reader.impl;

import backend.academy.log.analyzer.parser.LogParser;
import backend.academy.log.analyzer.reader.LogReader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация интерфейса {@link LogReader}, считывающая логи из файловой системы.
 */
@SuppressFBWarnings("PATH_TRAVERSAL_IN")
@Slf4j
public class FileLogReader implements LogReader {

    private static final String UNKNOWN = "Unknown"; // Константа для неизвестных имен файлов

    private final List<String> fileNames = new ArrayList<>(); // Список имен файлов, из которых считаны логи

    /**
     * Читает логи из указанного пути. Если путь является шаблоном (содержит '*', '?', '[', ']'),
     * будет применен glob-шаблон.
     *
     * @param path путь к файлу или шаблон для поиска логов
     * @return поток строк, представляющий логи
     * @throws IOException в случае проблем при чтении логов
     */
    @Override
    public Stream<String> readLogs(String path) throws IOException {
        if (isGlobPattern(path)) {
            return readLogsWithGlob(path);
        } else {
            return readSingleLogFile(path);
        }
    }

    /**
     * Определяет, является ли путь шаблоном glob.
     *
     * @param path путь
     * @return true, если путь содержит символы шаблона, иначе false
     */
    private boolean isGlobPattern(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[") || path.contains("]");
    }

    /**
     * Читает логи из единственного файла.
     *
     * @param path путь к файлу
     * @return поток строк, представляющий логи
     * @throws IOException в случае проблем при чтении файла
     */
    private Stream<String> readSingleLogFile(String path) throws IOException {
        Path filePath = Paths.get(path).toAbsolutePath();
        if (Files.isRegularFile(filePath) && containsLogData(filePath)) {
            fileNames.add(getFileName(filePath));
            log.info("The file was found directly: {}", filePath);
            return Files.lines(filePath, StandardCharsets.UTF_8);
        } else {
            log.error("The file was not found or does not contain logs: {}", filePath);
            return Stream.empty();
        }
    }

    /**
     * Читает файлы, определенные шаблоном glob.
     *
     * @param path путь-шаблон для поиска файлов
     * @return поток строк, представляющий логи
     */
    private Stream<String> readLogsWithGlob(String path) {
        Path rootPath = getRootPathForGlob(path);
        PathMatcher matcher = createPathMatcher(path);

        List<Path> matchedPaths = findMatchingPaths(rootPath, matcher);

        if (matchedPaths.isEmpty()) {
            log.error("No files were found using the {} template", path);
            return Stream.empty();
        }

        return matchedPaths.stream()
            .flatMap(this::readFileLines);
    }

    /**
     * Находит пути к файлам, соответствующие заданному шаблону glob.
     *
     * @param rootPath корневая директория поиска
     * @param matcher  объект сопоставления пути
     * @return список путей к файлам
     */
    private List<Path> findMatchingPaths(Path rootPath, PathMatcher matcher) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(matcher::matches)
                .filter(this::containsLogData)
                .peek(this::logAndStoreFileName)
                .toList();
        } catch (IOException e) {
            log.error("Error while traversing the directory, check the entered path");
        }
        return List.of();
    }

    /**
     * Читает строки из указанного файла.
     *
     * @param filePath путь к файлу
     * @return поток строк из файла
     */
    private Stream<String> readFileLines(Path filePath) {
        try {
            return Files.lines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading the file {}: ", filePath);
            return Stream.empty();
        }
    }

    /**
     * Логирует и добавляет имя файла в список найденных файлов.
     *
     * @param filePath путь к файлу
     */
    private void logAndStoreFileName(Path filePath) {
        if (filePath != null) {
            fileNames.add(getFileName(filePath));
            log.info("The file was found using a template: {}", filePath);
        }
    }

    /**
     * Возвращает список имен файлов, из которых считаны логи.
     *
     * @return список имен файлов
     */
    @Override
    public List<String> getFileNames() {
        return fileNames;
    }

    /**
     * Возвращает корневую директорию для шаблона glob.
     *
     * @param path путь-шаблон
     * @return корневая директория
     */
    private Path getRootPathForGlob(String path) {
        int firstSymbol = getFirstGlobSymbol(path);
        if (firstSymbol > 0 && path.lastIndexOf('/') > 0) {
            int lastSlashBeforeWildcard = path.lastIndexOf('/', firstSymbol);
            String rootPath = path.substring(0, lastSlashBeforeWildcard);
            return Path.of(rootPath);
        }

        return Path.of(".");
    }

    /**
     * Определяет индекс первого символа шаблона в пути.
     *
     * @param path путь-шаблон
     * @return индекс первого символа шаблона
     */
    private static int getFirstGlobSymbol(String path) {
        int indexOfAsterisk = path.indexOf('*');
        if (indexOfAsterisk == -1) {
            indexOfAsterisk = path.length();
        }

        int indexOfQuestionMark = path.indexOf('?');
        if (indexOfQuestionMark == -1) {
            indexOfQuestionMark = path.length();
        }

        int indexOfSquareBracket = path.indexOf('[');
        if (indexOfSquareBracket == -1) {
            indexOfSquareBracket = path.length();
        }

        return Math.min(Math.min(indexOfAsterisk, indexOfQuestionMark), indexOfSquareBracket);
    }

    /**
     * Создает матчер путей для шаблона glob.
     *
     * @param path путь-шаблон
     * @return матчер путей
     */
    private PathMatcher createPathMatcher(String path) {
        String adjustedPattern = "glob:" + path.replace('\\', '/');
        return FileSystems.getDefault().getPathMatcher(adjustedPattern);
    }

    /**
     * Возвращает имя файла из пути.
     *
     * @param filePath путь к файлу
     * @return имя файла или "Unknown", если имя не удалось получить
     */
    private String getFileName(Path filePath) {
        if (filePath == null) {
            return UNKNOWN;
        }
        Path fileName = filePath.getFileName();
        return (fileName != null) ? fileName.toString() : UNKNOWN;
    }

    /**
     * Проверяет, содержат ли данные в файле логи.
     *
     * @param filePath путь к файлу
     * @return true, если файл содержит данные логов, иначе false
     */
    private boolean containsLogData(Path filePath) {
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            return lines.anyMatch(line -> LogParser.LOG_PATTERN.matcher(line).matches());
        } catch (UncheckedIOException e) {
            log.warn("File {} skipped due to unsupported encoding", filePath);
            return false;
        } catch (IOException e) {
            log.error("Error checking the file {}: ", filePath);
            return false;
        }
    }
}
