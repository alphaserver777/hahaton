import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

plugins {
    // Глобальные плагины (без core-плагинов java/application здесь)
    id("checkstyle")
    id("com.diffplug.spotless") version "6.25.0"
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

val junitVersion = "5.10.3"
val slf4jVersion = "2.0.13"
val logbackVersion = "1.5.8"
val jacksonVersion = "2.17.2"
val thymeleafVersion = "3.1.2.RELEASE"
val picocliVersion = "4.7.6"
val openhtmltopdfVersion = "1.0.10"

allprojects {
    group = "org.octagon.axiom"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    // Применяем java-library ко всем подпроектам
    pluginManager.apply("java-library")

    // Настройка Java toolchain и артефактов
    extensions.configure(JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withJavadocJar()
        withSourcesJar()
    }

    // Компиляция/тесты
    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
        options.release.set(17) // совместим с требованием 17+
    }
    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }

    // Общие зависимости (через add, чтобы избежать DSL-конфликтов)
    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter:${junitVersion}")
        add("testImplementation", "org.assertj:assertj-core:3.26.3")
        add("api", "org.slf4j:slf4j-api:${slf4jVersion}")
        add("implementation", "ch.qos.logback:logback-classic:${logbackVersion}")
    }
}

// Code style
spotless {
    java {
        googleJavaFormat()
        target("**/*.java")
    }
}

checkstyle {
    toolVersion = "10.18.2"
}

// axiom-core
project(":axiom-core") {
    dependencies {
        add("api", "com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        add("api", "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
    }
}

// axiom-openapi
project(":axiom-openapi") {
    dependencies {
        add("api", project(":axiom-core"))
        add("implementation", "com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        add("implementation", "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
        // Заложено под реальный OAS-парсер; для MVP не тянем тяжёлые зависимости
        // implementation("io.swagger.parser.v3:swagger-parser-v3:2.1.22")
        // implementation("org.openapi4j:openapi-operation-validator:1.0.9")
    }
}

// axiom-checks
project(":axiom-checks") {
    dependencies {
        add("api", project(":axiom-core"))
        add("implementation", project(":axiom-openapi"))
    }
}

// axiom-fuzzer
project(":axiom-fuzzer") {
    dependencies {
        add("api", project(":axiom-core"))
    }
}

// axiom-report
project(":axiom-report") {
    dependencies {
        add("api", project(":axiom-core"))
        add("implementation", "org.thymeleaf:thymeleaf:${thymeleafVersion}")
        add("implementation", "com.openhtmltopdf:openhtmltopdf-pdfbox:${openhtmltopdfVersion}")
        add("implementation", "com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        add("implementation", "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
    }
}

// axiom-cli
project(":axiom-cli") {
    apply(plugin = "application")
    dependencies {
        add("implementation", project(":axiom-core"))
        add("implementation", project(":axiom-openapi"))
        add("implementation", project(":axiom-checks"))
        add("implementation", project(":axiom-fuzzer"))
        add("implementation", project(":axiom-report"))
        add("implementation", "info.picocli:picocli:${picocliVersion}")
        add("annotationProcessor", "info.picocli:picocli-codegen:${picocliVersion}")
        add("implementation", "com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
        add("implementation", "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
    }
    // Настройка application
    extensions.configure(org.gradle.api.plugins.JavaApplication::class.java) {
        mainClass.set("org.octagon.axiom.cli.Main")
    }
}

// axiom-ui
project(":axiom-ui") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        add("implementation", project(":axiom-core"))
        add("implementation", project(":axiom-report"))
        add("implementation", "org.springframework.boot:spring-boot-starter-web")
        add("implementation", "org.springframework.boot:spring-boot-starter-thymeleaf")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
    }
}

// plugins/axiom-sample-plugin
project(":plugins:axiom-sample-plugin") {
    dependencies {
        add("api", project(":axiom-core"))
    }
}
