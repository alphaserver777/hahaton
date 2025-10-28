package org.octagon.axiom.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import picocli.CommandLine;
import org.octagon.axiom.core.Report;

@CommandLine.Command(name="ci", description = "Проверка отчёта и политика fail")
public class CiCommand implements Runnable {
  @CommandLine.Option(names = "--report", required = true) String reportPath;
  @CommandLine.Option(names = "--fail-on-severity", defaultValue = "HIGH") String failOnSeverity;
  @CommandLine.Option(names = "--max-medium", defaultValue = "0") int maxMedium;

  private final ObjectMapper om = new ObjectMapper();

  @Override public void run() {
    try {
      Report r = om.readValue(Path.of(reportPath).toFile(), Report.class);
      int high = r.summary().high();
      int med = r.summary().medium();
      boolean fail = false;
      if ("HIGH".equalsIgnoreCase(failOnSeverity) && high >= 1) fail = true;
      if (med > maxMedium) fail = true;
      if (fail) {
        System.err.println("CI policy failed: high="+high+" medium="+med);
        System.exit(2);
      } else {
        System.out.println("CI policy OK");
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

