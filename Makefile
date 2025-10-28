build:
	./gradlew build

install:
	./gradlew :axiom-cli:installDist

scan:
	./axiom-cli/build/install/axiom-cli/bin/axiom-cli scan --openapi specs/example.yaml --base-url $${BASE_URL} --config axiom.yaml --out dist/out

ui:
	docker compose up --build
