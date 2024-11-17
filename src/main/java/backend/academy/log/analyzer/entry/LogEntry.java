package backend.academy.log.analyzer.entry;

import java.time.ZonedDateTime;

/**
 * Запись, представляющая одну строку лога.
 * Хранит основные поля лога, включая IP-адрес, пользователя, время, запрос, статус и другие данные.
 *
 * @param remoteAddr    IP-адрес клиента
 * @param remoteUser    Имя удаленного пользователя (если задано)
 * @param timeLocal     Локальное время запроса в формате ZonedDateTime
 * @param request       Текст HTTP-запроса
 * @param status        Код ответа HTTP
 * @param bodyBytesSent Количество отправленных байт тела ответа
 * @param httpReferer   Поле Referer из заголовков HTTP (если задано)
 * @param httpUserAgent Поле User-Agent из заголовков HTTP
 */
public record LogEntry(

    String remoteAddr,

    String remoteUser,

    ZonedDateTime timeLocal,

    String request,

    int status,

    long bodyBytesSent,

    String httpReferer,

    String httpUserAgent
) {
}
