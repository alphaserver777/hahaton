<img alt="AXIOM" src="docs/_assets/logo.txt" />

# AXIOM — анализ безопасности и корректности REST API

- Организация: Octagon
- Лицензия: Apache-2.0 (см. LICENSE)
- Репозиторий: https://github.com/teslacorer/hahaton

AXIOM — много‑модульный Java‑инструмент (Java 21, совместим с 17+) для автоматизированной проверки REST API: контракт‑валидация, ключевые проверки OWASP API Top 10, фаззинг, отчёты (JSON/HTML/PDF), лёгкий UI и интеграция в CI/CD.

Ссылки:
- OWASP API Security Top 10: https://owasp.org/www-project-api-security/
- OpenAPI Specification: https://swagger.io/specification/

Внимание: при работе со стендами (open.bankingapi.ru, пароль 321) используйте бережные лимиты (rate‑limit, таймауты). Не запускайте SSRF/скан внешних сетей.

## Быстрый старт

Требуется JDK 21 (поддерживается 17+). Gradle wrapper рекомендуется (или локальный Gradle).

```bash
# Сборка
./gradlew build

# Установка CLI
./gradlew :axiom-cli:installDist

# Запуск сканирования примера
./axiom-cli/build/install/axiom-cli/bin/axiom-cli scan \
  --openapi specs/example.yaml \
  --base-url ${BASE_URL} \
  --config axiom.yaml \
  --out dist/out

# Политика в CI
./axiom-cli/build/install/axiom-cli/bin/axiom-cli ci \
  --report dist/out/report.json \
  --fail-on-severity HIGH \
  --max-medium 0

# UI (локальный просмотр отчётов)
./gradlew :axiom-ui:bootRun
```

Подробности установки и сценарий демо см. `docs/GETTING_STARTED.md` и `docs/DEMO_SCRIPT.md`.

## Возможности (MVP)
- Вход: OpenAPI 3.1+/Swagger 2.0 (путь/URL), базовый URL, YAML‑конфиг
- Контракт‑валидация (минимальная в MVP) и проверка статуса/контента
- OWASP API Top 10: BOLA, Broken Auth, Excessive Data Exposure, Injection, Rate‑Limit, Security Headers (MVP‑уровень)
- Фаззинг значений и инъекций (по wordlists)
- Отчёты: JSON + HTML + PDF
- CLI: scan/fuzz/ci с exit‑кодами 0/1/2
- UI: просмотр HTML‑отчётов из `dist/out`
- Плагины: SPI через ServiceLoader

## Структура
См. `docs/ARCHITECTURE.md`. Основные директории:
- `axiom-*/` — модули
- `docs/` — документация
- `specs/` — пример OpenAPI
- `wordlists/` — словари для discovery/инъекций
- `docker/` — Dockerfile и compose

## Конфигурация
Файл `axiom.yaml` с поддержкой env‑переменных. Приоритет: CLI > ENV > YAML.

## CI/CD
`.gitlab-ci.yml` собирает проект, запускает `scan` и `ci`, сохраняет артефакты `dist/out` и фейлит пайплайн при High≥1/Medium>0.

## Юридическое
Копирайт Octagon. Лицензия Apache‑2.0. Чувствительные данные в отчётах маскируются.
