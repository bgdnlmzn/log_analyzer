# Анализатор логов (Log Analyzer)

## Описание проекта
Программа для анализа логов NGINX с возможностью фильтрации, подсчёта статистики и форматированного вывода результата.
### Основные функции
- Подсчёт общего количества запросов. 
- Подсчет количества уникальных IP.
- Определение наиболее часто запрашиваемых ресурсов. 
- Определение наиболее часто встречающихся кодов ответа. 
- Определение наиболее часто встречающихся методов запроса. 
- Расчёт среднего размера ответа сервера. 
- Расчет 95p размера ответа сервера. 
- Фильтрация логов по указанным полям (например, User-Agent). 
- Форматированный вывод отчётов: Markdown или AsciiDoc.

## Использование
### Параметры командной строки:
`--path` - Путь к лог-файлам: локальный путь (с поддержкой glob) или URL.

`--from` - Начальная дата для фильтрации логов в формате *ISO8601* (опционально).

`--to` - Конечная дата для фильтрации логов в формате *ISO8601* (опционально).

`--format` - Формат вывода отчёта *markdown* (по умолчанию) или *adoc*.

`--filter-field` - Поле для фильтрации.

`--filter-value` - Значение для фильтрации.
### Поддерживаемые поля для фильтрации:
- `remote_addr` 
- `remote_user`
- `time_local` 
- `request` 
- `status` 
- `body_bytes_sent` 
- `http_referer` 
- `http_user_agent`


## Примеры запуска
1. Анализ всех логов, отфильтровав по дате и выведя результат в формате Markdown:
```shell
java -jar target/analyzer-1.0.0.jar --path logs/2024* --from 2024-08-31 --format markdown
```
2. Анализ удалённых логов с результатом в формате AsciiDoc:
```shell
java -jar target/analyzer-1.0.0.jar --path https://example.com/nginx_logs --format adoc
```
3. Фильтрация логов по User-Agent:
```shell
java -jar target/analyzer-1.0.0.jar --path logs/**/2024-08-31.txt --filter-field http_user_agent --filter-value "Mozilla*"
```

### Сборка и запуск
Убедитесь, что что консоль поддерживает кодировку UTF-8 (для Windows):
```shell
chcp 65001
```

Сборка проекта(создание fat-jar):
```shell
./mvnw clean verify
```
Запуск (jar):
```shell
java -jar target/analyzer-1.0.0.jar <параметры>
```

Запуск тестов:

```shell
./mvnw test
```

Запуск линтеров:

```shell
./mvnw checkstyle:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check
```
