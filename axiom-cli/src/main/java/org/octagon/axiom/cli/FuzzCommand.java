package org.octagon.axiom.cli;

import picocli.CommandLine;

@CommandLine.Command(name="fuzz", description = "Фаззинг указанных маршрутов (заглушка MVP)")
public class FuzzCommand implements Runnable {
  @CommandLine.Option(names = "--routes") String routes;
  @CommandLine.Option(names = "--base-url") String baseUrl;
  @CommandLine.Option(names = "--config", defaultValue = "axiom.yaml") String configPath;

  @Override public void run() {
    System.out.println("Fuzz stub: " + routes);
  }
}

