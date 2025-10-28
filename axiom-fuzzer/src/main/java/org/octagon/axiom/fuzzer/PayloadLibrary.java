package org.octagon.axiom.fuzzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Загрузка payload'ов из файлов
public final class PayloadLibrary {
  public List<String> load(Path path) throws IOException {
    return Files.readAllLines(path).stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty() && !s.startsWith("#"))
        .toList();
  }
}

