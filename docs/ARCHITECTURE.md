# Архитектура AXIOM

- axiom-core: модели, HTTP-исполнитель, движок сканера, SPI AxiomCheck
- axiom-openapi: загрузка и нормализация OpenAPI, минимальная контракт-валидация
- axiom-checks: базовые проверки OWASP API Top 10 (MVP)
- axiom-fuzzer: генерация значений и негативов, словари
- axiom-report: JSON/HTML/PDF отчёты (Thymeleaf + openhtmltopdf)
- axiom-cli: Picocli команды scan/fuzz/ci
- axiom-ui: Spring Boot UI для просмотра отчётов
- plugins/axiom-sample-plugin: пример расширения через ServiceLoader

Поток данных: OpenAPI → CaseGenerator → HttpExecutor → ContractValidator → Checks.analyze → ReportWriter.

