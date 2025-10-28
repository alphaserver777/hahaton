# DEMO SCRIPT (2 минуты)

1) Установка и сборка
```bash
./gradlew :axiom-cli:installDist
```

2) Сканирование примера
```bash
./axiom-cli/build/install/axiom-cli/bin/axiom-cli scan \
  --openapi specs/example.yaml \
  --base-url ${BASE_URL} \
  --config axiom.yaml \
  --out dist/out
```

3) Показ HTML‑отчёта (2–3 findings в реальном стенде)

4) Экспорт PDF

5) Запуск UI / просмотр
```bash
./gradlew :axiom-ui:bootRun
```

6) Показ `.gitlab-ci.yml` и условия fail (High≥1/Medium>0)
