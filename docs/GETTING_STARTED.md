# GETTING STARTED

Требования: JDK 21 (совместим с 17+), доступ в интернет для зависимостей.

## Установка
```bash
./gradlew build
./gradlew :axiom-cli:installDist
```

## Токены (client_credentials)
Пример получения `access_token` (замените параметры на свои):
```bash
curl -u "$CLIENT_ID:$CLIENT_SECRET" \
  -d 'grant_type=client_credentials' \
  -d 'scope=api.read' \
  https://auth.example.com/oauth2/token
```
Экспортируйте в окружение:
```bash
export AUTH_TOKEN_USER=eyJhbGciOi...
export AUTH_TOKEN_ADMIN=eyJhbGciOi...
export BASE_URL=https://open.bankingapi.ru
```

## Конфигурация
Отредактируйте `axiom.yaml` при необходимости. Параметры из ENV имеют приоритет.

## Запуск сканирования
```bash
./axiom-cli/build/install/axiom-cli/bin/axiom-cli scan \
  --openapi specs/example.yaml \
  --base-url ${BASE_URL} \
  --config axiom.yaml \
  --out dist/out
```

## Просмотр отчёта
- Откройте HTML файл из `dist/out` или запустите UI:
```bash
./gradlew :axiom-ui:bootRun
```
