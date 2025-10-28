# Stage 1: build
# Stage 1: build with Gradle preinstalled
FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :axiom-cli:installDist --no-daemon

# Stage 2: run
FROM eclipse-temurin:21-jre
WORKDIR /work
COPY --from=build /app/axiom-cli/build/install/axiom-cli /work/axiom-cli
COPY axiom.yaml /work/axiom.yaml
COPY specs /work/specs
COPY wordlists /work/wordlists
VOLUME ["/work/dist/out"]

# Use shell form to allow env var expansion for BASE_URL
ENV BASE_URL="http://localhost"
ENTRYPOINT ["/bin/sh","-lc","/work/axiom-cli/bin/axiom-cli scan --openapi /work/specs/example.yaml --base-url ${BASE_URL} --config /work/axiom.yaml --out /work/dist/out"]
