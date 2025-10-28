package org.octagon.axiom.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.octagon.axiom.core.Report;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

// Рендер HTML через Thymeleaf
public final class HtmlReportRenderer {
  private final TemplateEngine engine;

  public HtmlReportRenderer() {
    var resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setTemplateMode("HTML");
    this.engine = new TemplateEngine();
    this.engine.setTemplateResolver(resolver);
  }

  public void render(Report report, Path out) throws IOException {
    Files.createDirectories(out.getParent());
    var ctx = new Context();
    ctx.setVariable("report", report);
    String html = engine.process("index", ctx);
    Files.writeString(out, html, StandardCharsets.UTF_8);
  }
}

