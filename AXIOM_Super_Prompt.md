
# 🚀 AXIOM — Super Prompt для Codex (Full Repo Generator)

> Этот промпт максимально детализирован для ИИ‑генерации полноценного репозитория проекта **AXIOM**: много‑модульный Java‑инструмент (Java 21, совместим с 17+) для автоматизированного анализа безопасности и корректности REST API. Включает архитектуру, кодовые скелеты, CI/CD (GitLab), Docker, минимальный web‑UI, отчёты HTML/PDF/JSON, фаззинг, плагинность, примеры спецификаций, демо‑скрипт и контроль качества.  
> Цель: получить **готовый проект**, который можно собрать, запустить и показать на демо хакатона.

---

## 0) Роль и поведение Codex
- Ты — **архитектор, тимлид, девопс и разработчик** в одном лице.
- Генерируй **рабочий код**, а не только заглушки, при этом держись принципов: минимально необходимая сложность, высокая читаемость, модульность и расширяемость (плагины).
- Соблюдай структуру и версии зависимостей. Добавляй **осмысленные тесты** и **примеры** (specs/wordlists).
- Везде, где уместно, снабжай код комментариями на **русском** (краткими и по делу).
- Сразу добавляй **скрипты запуска**, **Makefile**, **Dockerfile**, **docker‑compose**, **.gitlab-ci.yml**, **README**, **docs/GETTING_STARTED.md**, пример **axiom.yaml**.

---

## 1) Общие параметры проекта
- Организация: **Octagon**
- Название: **AXIOM**
- Лицензия: **Apache‑2.0** (файл `LICENSE` + шапки в исходниках опционально)
- Язык кода/документации/отчётов: **RU**
- Java: **21** (совместим с требованием 17+)
- Сборка: **Gradle Kotlin DSL (gradle‑kts)**, wrapper включить
- Репозиторий: **https://github.com/teslacorer/hahaton** (описать в README)
- Контейнеры: **Docker** обязателен (slim runtime)
- CI/CD: **GitLab CI**
- UI: **лёгкий web‑просмотр HTML‑отчётов** (Spring Boot)

### 1.1 Нефункциональные требования
- Время анализа среднего API (20–30 эндпоинтов): **≤ 5 минут**
- Минимальные зависимости, отсутствие нативных расширений
- Параллельность запросов, rate‑limit, таймауты
- Конфиги — через YAML (`axiom.yaml`) с поддержкой env‑переменных

---

## 2) Что должен уметь проект (MVP+)
1. **Вход**: URL/файл **OpenAPI 3.1+** или **Swagger 2.0**, `--base-url`, `--config`
2. **Валидация контракта**: статусы, заголовки, `content-type`, соответствие JSON‑схеме (type/format/required/enums/oneOf/anyOf), error‑schema
3. **Проверки OWASP API Top 10** (минимум):  
   - API1: **BOLA/IDOR** (High)  
   - API2: **Broken Authentication** (High/Med)  
   - API3: **Excessive Data Exposure** (Med)  
   - API8: **Injection** (High) — SQL/NoSQL/Path (короткий набор)  
   - API4/API5/API6 (частично): Rate‑Limit, Security Misconfig, BOPLA (замечания/med)  
4. **Фаззинг**: генерация негативов и полезных инъекций по словарям
5. **Discovery**: короткий словарь путей (вежливый режим)
6. **Отчёты**: JSON + HTML + PDF, **severity** (High/Med/Low) + **confidence**
7. **CLI**: `scan`, `fuzz`, `ci` (exit‑codes 0/1/2)
8. **UI**: просмотр HTML‑отчётов из `dist/out`
9. **Плагины**: SPI через `ServiceLoader` для добавления новых проверок
10. **Docker** и **GitLab CI** (артефакты: отчёты)

---

## 3) Архитектура модулей (Gradle multi‑project)
```
axiom/
  settings.gradle.kts
  build.gradle.kts
  gradle/ (wrapper)
  README.md
  LICENSE
  CHANGELOG.md
  CODE_OF_CONDUCT.md
  CONTRIBUTING.md
  Makefile
  axiom.yaml                 # пример конфига
  docs/
    GETTING_STARTED.md
    DEMO_SCRIPT.md
    ARCHITECTURE.md
    OWASP_MAPPING.md
  specs/
    example.yaml             # мини-спека для демонстрации
  wordlists/
    paths.txt
    injections.txt
  axiom-core/
  axiom-openapi/
  axiom-checks/
  axiom-fuzzer/
  axiom-report/
  axiom-cli/
  axiom-ui/
  plugins/axiom-sample-plugin/
  docker/
    Dockerfile
    docker-compose.yml
  .gitignore
  .editorconfig
  .gitlab-ci.yml
```

### 3.1 Версии и зависимости (укажи в корневом build.gradle.kts)
- Kotlin DSL Gradle
- JUnit 5, AssertJ
- SLF4J + Logback
- Picocli
- openapi4j + swagger‑parser‑v3 + networknt json‑schema validator
- Thymeleaf + openhtmltopdf (PDF)
- Jackson (databind + yaml) для конфигов/JSON‑сериализации
- Spring Boot (только для `axiom-ui`), web + thymeleaf (минимально)
- Spotless + Google Java Format (code‑style), Checkstyle (минимальные правила)

---

## 4) Детали доменной модели и движка

### 4.1 Базовые модели (`axiom-core`)
```java
// Severity и Confidence
public enum Severity { HIGH, MEDIUM, LOW }
public enum Confidence { HIGH, MEDIUM, LOW }

// Представление эндпоинта и кейса
public record ApiRoute(String method, String path) {}
public record TestCase(
    String id,
    ApiRoute route,
    Map<String, String> headers,
    Optional<String> body,
    Optional<String> tokenRole,
    Map<String, Object> meta // произвольные пометки (вариант фаззинга и т.д.)
) {}

public record HttpExchange(
    int status,
    Map<String, List<String>> headers,
    Optional<String> bodyBytesBase64,     // безопасно хранить любые байты
    String contentType,
    long durationMs
) {}

public record TestResult(TestCase testCase, HttpExchange exchange, List<String> contractIssues) {}

public record Finding(
    String id,
    String title,
    Severity severity,
    Confidence confidence,
    ApiRoute route,
    Map<String, Object> evidence,   // request/response сниппеты, отличия схемы
    List<String> remediation,       // рекомендации
    List<String> tags               // напр. ["OWASP-API1","Authorization"]
) {}

public record ReportSummary(int high, int medium, int low, long durationMs, int endpoints) {}

public record Report(
    Map<String, Object> meta,
    ReportSummary summary,
    List<Finding> findings
) {}
```

### 4.2 Исполнитель HTTP (`HttpExecutor`)
- На базе `java.net.http.HttpClient` с пулом подключений, HTTP/2, таймаутами, редиректами запрещено.
- Поддержка Bearer token, API‑key (через конфиг), proxy (на будущее).
- Rate‑limit (скользящее окно), retry (1 раз на сетевые сбои), cancellation по таймауту.

### 4.3 Движок сканера (`ScanEngine`)
```java
public final class ScanEngine {
  private final HttpExecutor http;
  private final List<AxiomCheck> checks;
  private final ContractValidator contractValidator;

  public Report run(OpenApiModel oas, TargetContext ctx) {
    long t0 = System.currentTimeMillis();
    var testCases = checks.stream().flatMap(c -> c.generate(oas, ctx)).toList();
    var results = testCases.parallelStream()
        .map(tc -> http.execute(tc))
        .map(res -> res.withContract(contractValidator.validate(tc, res)))
        .toList();
    var findings = checks.stream().flatMap(c -> c.analyze(results.stream(), ctx)).toList();
    var summary = summarize(findings, t0);
    return new Report(meta(oas, ctx), summary, findings);
  }
}
```

### 4.4 Контракт‑валидация (`axiom-openapi`)
- Парсинг OAS: `swagger-parser` → нормализация → `openapi4j` для валидации.
- Проверки: допустимый **status**, корректный **Content‑Type**, соответствие **JSON‑схеме** (включая `oneOf/anyOf`, `required`, `enum`, `format`), проверка **error‑schema**.
- **additionalProperties**: строгий режим по умолчанию (флаг в конфиге).

---

## 5) Проверки OWASP (axiom-checks)

### 5.1 SPI интерфейс
```java
public interface AxiomCheck extends AutoCloseable {
  String id();                    // например, "BOLA"
  String title();                 // "BOLA/IDOR: доступ к чужому ресурсу"
  Severity defaultSeverity();
  Stream<TestCase> generate(OpenApiModel model, TargetContext ctx);
  Stream<Finding> analyze(Stream<TestResult> results, TargetContext ctx);
}
```
- Реализации регистрируются в `META-INF/services/...` и подхватываются `ServiceLoader`.

### 5.2 BOLA / IDOR (High)
Стратегия:
- Найти пути вида `/resource/{id}`; собрать по 2 разных ID (из OAS примеров, из фикстур, эвристики).
- Выполнить запрос к `id=A` с токеном `role=B` (и наоборот).
- Если статус 2xx/304/204 и тело содержит данные **не текущего субъекта** — Finding.
- Evidence: `request` (метод, путь, роль), `response.status`, `snippet` (обрезок тела), список отличий владельца/ID.
- Ремедиации: “проверять владельца ресурса на уровне домена”, “фильтровать по subject claim (`sub`)”.

### 5.3 Broken Authentication (High/Med)
- Для всех эндпоинтов с security‑схемами: запрос **без токена**, с **мусорным токеном**, с **ролью ниже заявленной**.
- 2xx → High; 401/403 → ок; слабые ответы/утечки заголовков → замечание.

### 5.4 Excessive Data Exposure (Med)
- Сравнить фактический JSON с объявленной схемой: лишние поля/PII.
- Если поле присутствует, но **не описано** в схеме → Finding (Med).

### 5.5 Injection (SQL/NoSQL/Path) (High)
- Набор компактных payload’ов (см. `wordlists/injections.txt`): `' OR '1'='1`, `") or 1=1 --`, `{"$ne": ""}`, `../../etc/passwd` и т.д.
- Подставлять в query/header/path/body (аккуратно). Признаки: 5xx/DB errors/аномалии.

### 5.6 Rate‑Limit (Med), Security Misconfig (Low/Med)
- 20 запросов коротким окном → отсутствие `429` → замечание.
- CORS `*` с `Access-Control-Allow-Credentials:true` → Med; отсутствие HSTS → Low (если https).

---

## 6) Фаззинг (axiom-fuzzer)
- Генерация значений по типам: `string/email/uuid/date-time`, `int32/int64`, `boolean`.
- Негативы: пустые, слишком длинные (>1024), не тот тип, NaN/Infinity (если допустимы), массивы с дубликатами, большие числа.
- Политика: уровень `medium` по умолчанию; лимиты кейсов на эндпоинт; останов по времени.

---

## 7) Конфигурация (`axiom.yaml`)
```yaml
target:
  baseUrl: ${BASE_URL:https://api.bankingapi.ru}
  openapi: ./specs/example.yaml
  auth:
    type: bearer
    tokens:
      - role: user
        value: ${AUTH_TOKEN_USER:}
      - role: admin
        value: ${AUTH_TOKEN_ADMIN:}
execution:
  concurrency: 8
  rateLimitRps: 8
  timeoutMs: 8000
  retries: 1
checks:
  severityThreshold: MEDIUM
  enableDiscovery: true
  wordlists:
    paths: ./wordlists/paths.txt
    injections: ./wordlists/injections.txt
contract:
  strictAdditionalProperties: true
  validateErrorSchemas: true
report:
  outDir: ./dist/out
  formats: [json, html, pdf]
ui:
  enabled: true
  bind: 0.0.0.0
  port: 8080
```

### 7.1 Приоритет конфигов
- Параметры CLI > переменные окружения > `axiom.yaml`.
- Все секреты — только из переменных/CI.

---

## 8) CLI (axiom-cli)
Команды:
```
axiom scan --openapi <pathOrUrl> --base-url <url> --config axiom.yaml --out dist/out
axiom fuzz --routes GET:/v1/users,POST:/v1/orders --base-url <url> --config axiom.yaml
axiom ci --report dist/out/report.json --fail-on-severity HIGH --max-medium 0
```
Exit‑codes:
- `0` — успех
- `1` — техническая ошибка/исключение
- `2` — нарушена политика (например, HIGH ≥ 1 или MEDIUM > max‑medium)

---

## 9) Отчёты (axiom-report)

### 9.1 JSON‑схема (упрощённая)
```json
{
  "meta": {"target":"","timestamp":"","version":"1.0.0"},
  "summary": {"high":0,"medium":0,"low":0,"durationMs":0,"endpoints":0},
  "findings": [{
    "id":"BOLA-001",
    "title":"BOLA: доступ к чужому ресурсу /v1/users/{id}",
    "severity":"HIGH",
    "confidence":"HIGH",
    "route":{"method":"GET","path":"/v1/users/{id}"},
    "evidence":{
      "request":{"method":"GET","path":"/v1/users/123","tokenRole":"user[B]"},
      "response":{"status":200,"bodySnippet":"{\"id\":123,\"email\":\"a@b\"}"}
    },
    "remediation":[
      "Проверять владельца ресурса на уровне домена (policy check).",
      "Не доверять ID из клиента; фильтровать по subject claim (sub)."
    ],
    "tags":["OWASP-API1","Authorization"]
  }]
}
```

### 9.2 HTML
- Навбар: сводка по severity, фильтры (checkbox High/Med/Low), поиск
- Карточки findings с раскрывающимися evidence
- Кнопка **Export PDF** (через openhtmltopdf) и **Download JSON**

### 9.3 PDF
- Экспорт текущего HTML. Гарантируй корректный рендер кириллицы (встрои шрифт).

---

## 10) UI (axiom-ui)
- Spring Boot (web + thymeleaf), порты из `axiom.yaml`.
- Роуты:
  - `GET /` — список отчётов в `dist/out`
  - `GET /report/{name}` — показ HTML‑отчёта
  - `GET /dl/{name}.pdf|json|html` — скачивание
- Без БД; хранение файлов — локально.

---

## 11) Тестовые ресурсы

### 11.1 Спеки и стенды
- OpenAPI справка: https://swagger.io/specification/
- Песочницы:  
  - https://open.bankingapi.ru/ (пароль **321**)  
  - Док‑порталы: https://abank.open.bankingapi.ru/docs, https://sbank.open.bankingapi.ru/docs, https://vbank.open.bankingapi.ru/docs
- Добавь `docs/GETTING_STARTED.md`: как получить `access_token` по `client_credentials` (пример `curl`), куда подставить токены, как запустить `scan`.

### 11.2 Пример спецификации `specs/example.yaml`
- Мини‑модель: `/users`, `/users/{id}`, `/orders`, аутентификация bearer, поля `id`, `email`, `role` — для демонстрации BOLA/EDE.

### 11.3 Словари
- `wordlists/paths.txt`: `/admin`, `/internal`, `/debug`, `/health`, `/status`, `/v1/private`… (20–50 строк)
- `wordlists/injections.txt`: компактный список SQL/NoSQL/Path payloads (20–40 строк).

---

## 12) Производительность и безопасность
- Параллельно до `concurrency=8`, `rateLimitRps=8`, таймаут 8s, retries=1
- Запрет SSRF/XXE/скан внешних сетей по умолчанию
- Не эскалировать нагрузку — бережный фаззинг, кап на кейсы/эндпоинт

---

## 13) Интеграция в GitLab CI

### 13.1 `.gitlab-ci.yml`
- Образ: `eclipse-temurin:21`
- Кэш Gradle
- Job `scan`:
  1) `./gradlew :axiom-cli:installDist`
  2) `./axiom-cli/build/install/axiom/bin/axiom scan --openapi specs/example.yaml --base-url $BASE_URL --config axiom.yaml --out dist/out`
  3) `./axiom-cli/build/install/axiom/bin/axiom ci --report dist/out/report.json --fail-on-severity HIGH --max-medium 0`
- Артефакты: `dist/out`
- Переменные (в GitLab → Settings → CI/CD → Variables):
  - `BASE_URL`
  - `AUTH_TOKEN_USER`
  - `AUTH_TOKEN_ADMIN`

---

## 14) Docker

### 14.1 `docker/Dockerfile`
- Stage 1 (build): `eclipse-temurin:21-jdk` → `./gradlew installDist`
- Stage 2 (run): `eclipse-temurin:21-jre` → копировать дистрибутив `axiom-cli` и скрипт запуска
- ENTRYPOINT по умолчанию: `axiom scan ...` (читаем env/конфиг)

### 14.2 `docker-compose.yml`
- Сервис `ui`: монтирует `./dist/out`, публикует порт `8080`, зависит от собранного образа.

---

## 15) Качество кода и стиль
- **Spotless** (Google Java Format), **Checkstyle** (минимальный набор)
- **Conventional Commits**, `CHANGELOG.md`
- `.editorconfig` (отступы, кодировка UTF‑8)

---

## 16) Логи и наблюдаемость
- Логи: JSON‑строка на одну запись (уровень, ts, модуль, сообщение, durationMs, route)
- Метрики (минимум): общее количество запросов, средняя длительность, коды статусов

---

## 17) Политика fail (CLI `ci`)
- По умолчанию: **High ≥ 1** или **Medium > 0** → exit 2
- Параметры через флаги и/или env (например, `MAX_MEDIUM=0`)

---

## 18) Документация
- `README.md`: обзор, установка, быстрый старт, команды, политика, ссылки на OWASP
- `docs/GETTING_STARTED.md`: пошаговая настройка токенов, запуск скана, разбор отчёта
- `docs/ARCHITECTURE.md`: схемы модулей, SPI, поток данных
- `docs/OWASP_MAPPING.md`: таблица соответствия проверок OWASP API Top 10

---

## 19) Демо и презентация
- `docs/DEMO_SCRIPT.md`: готовый сценарий на 2 минуты  
  1) `axiom scan ...` на `specs/example.yaml`
  2) показать HTML‑отчёт (2–3 findings)
  3) экспорт PDF
  4) запустить UI/открыть в браузере
  5) показать `.gitlab-ci.yml` и условие fail
- Слайды: проблема → решение AXIOM → архитектура → проверки → отчёты/UX → CI → Roadmap

---

## 20) Roadmap (после хакатона)
- Поддержка **AsyncAPI 2.6+** (опциональные проверки для событийных API)
- Интеграция с **ZAP** как внешний плагин
- Расширенный фаззинг (астративные стратегии, stateful‑кейсы)
- Глубокий анализ авторизации (BFLA, complex policy checks)
- Авто‑генерация фикстур через сценарии создания сущностей

---

## 21) Скелеты классов, которые нужно сгенерировать (минимум)

### 21.1 `axiom-core`
- `TargetContext`, `ExecutionProfile`, `AuthProfile`
- `HttpExecutor` (+ unit‑тест с mock web server)
- `ScanEngine`, `ReportAggregator`

### 21.2 `axiom-openapi`
- `OpenApiLoader` (URL/файл + `$ref`), `ContractValidator`
- `CaseGenerator` (happy‑path по каждому `path`/`method`)

### 21.3 `axiom-checks`
- `BolaCheck`, `BrokenAuthCheck`, `InjectionCheck`, `ExcessiveDataExposureCheck`, `RateLimitCheck`, `SecurityHeadersCheck`

### 21.4 `axiom-fuzzer`
- `ValueFuzzer` (по типам/форматам), `PayloadLibrary` (чтение `wordlists`)

### 21.5 `axiom-report`
- `JsonReportWriter`, `HtmlReportRenderer`, `PdfExporter`
- Шаблоны Thymeleaf: `index.html`, `finding.html`, общий `layout.html`

### 21.6 `axiom-cli`
- Команды на Picocli: `ScanCommand`, `FuzzCommand`, `CiCommand`
- Парсинг `axiom.yaml`, обработка env‑переменных

### 21.7 `axiom-ui`
- Spring Boot `ReportController`, `ReportsListService`
- Шаблоны: список отчётов и просмотр (встраивание HTML)

### 21.8 Плагин
- `plugins/axiom-sample-plugin` с реализацией `AxiomCheck` и `META-INF/services`

---

## 22) Файлы и шаблоны, которые нужно добавить

### 22.1 `README.md`
- Вставь ссылки: https://owasp.org/www-project-api-security/ и https://swagger.io/specification/
- Добавь раздел **«Стенды/песочницы»** (open.bankingapi.ru, пароль **321**) с предупреждениями о нагрузке.

### 22.2 `docs/GETTING_STARTED.md` (пример блоков)
- Как получить `access_token` (`client_credentials`), примеры `curl`
- Как указать токены в `axiom.yaml`/env и запустить `scan`

### 22.3 `axiom.yaml` (см. выше)
### 22.4 `.gitlab-ci.yml` (см. выше)
### 22.5 `docker/Dockerfile`, `docker-compose.yml`
### 22.6 `wordlists/*.txt`, `specs/example.yaml`
### 22.7 `Makefile`
```makefile
build: 
\t./gradlew build
install:
\t./gradlew :axiom-cli:installDist
scan:
\t./axiom-cli/build/install/axiom/bin/axiom scan --openapi specs/example.yaml --base-url $${BASE_URL} --config axiom.yaml --out dist/out
ui:
\tdocker compose up --build
```

---

## 23) Политики и юридические заметки
- Лицензия Apache‑2.0; добавить копирайт Octagon в README.
- Уважать условия песочниц: бережный фаззинг, не запускать SSRF/скан внешних сетей.
- Чувствительные данные в отчётах маскировать (email/phone → `***@***`).

---

## 24) Критерии приёмки (Definition of Done)
- `./gradlew build` проходит локально (Java 21)
- `axiom scan` генерирует **JSON + HTML + PDF** в `dist/out`
- HTML‑отчёт открыт через `axiom-ui` или как файл
- `.gitlab-ci.yml` собирает артефакты и фейлит pipeline при High≥1/Mid>0
- Есть `specs/example.yaml`, `wordlists/*.txt`, `axiom.yaml`, `README`, `docs/*`
- Есть хотя бы **2 unit‑теста** (parser + report writer) и **1 интеграционный** (scan example.yaml)

---

## 25) Привязка к критериям хакатона
- **Практическая применимость** — one‑click scan + CI + отчёты
- **Глубина/релевантность** — OWASP API Top 10 ключевые проверки
- **Точность** — контракт‑валидация + confidence + дедупликация
- **Скорость** — параллельный запуск, ограничение фаззинга
- **Автоматизация и интеграция** — CLI + GitLab CI + Docker
- **Архитектура** — модули + SPI плагины
- **UX** — HTML‑отчёт + UI просмотр

---

## 26) Особые указания по внешним материалам
Если в проектную папку приложен файл `Гайд для хакатона API.docx` — добавь ссылку на него в `README` и процитируй безопасные части (инструкции подключения), **не включай** приватные ключи/пароли в репозиторий.

---

## 27) Заключительные указания Codex
1. Сгенерируй весь проект строго по структуре выше.  
2. Убедись, что локальный запуск (build → scan → report → pdf → ui) работает.  
3. Добавь комментарии и примеры использования.  
4. Проверь, что дефолт‑конфиги не ломают стенды (бережные лимиты).  
5. Выложи подробный `README` и `docs/*` для мгновенного онбординга.
