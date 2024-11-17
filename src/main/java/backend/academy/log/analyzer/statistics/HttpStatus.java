package backend.academy.log.analyzer.statistics;

import lombok.Getter;

/**
 * Перечисление, представляющее стандартные HTTP-статусы и их описания.
 */
@Getter
public enum HttpStatus {

    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    PARTIAL_CONTENT(206, "Partial Content"),

    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout");

    private final int code;

    private final String description;

    /**
     * Конструктор для инициализации кода и описания статуса.
     *
     * @param code        Код состояния HTTP.
     * @param description Описание статуса.
     */
    HttpStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Получить HttpStatus по коду состояния.
     *
     * @param code Код состояния HTTP.
     * @return Соответствующий HttpStatus.
     * @throws IllegalArgumentException если код не соответствует ни одному статусу.
     */
    public static HttpStatus fromCode(int code) {
        for (HttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException();
    }
}
