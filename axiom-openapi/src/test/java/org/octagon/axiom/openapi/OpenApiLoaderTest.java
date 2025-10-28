package org.octagon.axiom.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.octagon.axiom.core.OpenApiModel;

public class OpenApiLoaderTest {
  @Test
  void loadsExampleSpec() throws Exception {
    var loader = new OpenApiLoader();
    OpenApiModel model = loader.load(Path.of("specs/example.yaml").toString());
    // Проверяем, что модель загружена и содержит маршруты
    assertThat(model).isInstanceOf(OpenApiLoader.SimpleOpenApiModel.class);
    var simple = (OpenApiLoader.SimpleOpenApiModel) model;
    assertThat(simple.routes).isNotEmpty();
  }
}

