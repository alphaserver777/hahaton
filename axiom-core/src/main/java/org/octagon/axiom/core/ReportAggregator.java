package org.octagon.axiom.core;

import java.util.ArrayList;
import java.util.List;

// Простой агрегатор отчётов (на будущее)
public final class ReportAggregator {
  public Report merge(List<Report> reports) {
    var findings = new ArrayList<Finding>();
    int h=0, m=0, l=0, endpoints=0; long dur=0;
    for (var r : reports) {
      findings.addAll(r.findings());
      h += r.summary().high();
      m += r.summary().medium();
      l += r.summary().low();
      endpoints += r.summary().endpoints();
      dur += r.summary().durationMs();
    }
    var sum = new ReportSummary(h,m,l,dur,endpoints);
    return new Report(reports.isEmpty()? java.util.Map.of(): reports.get(0).meta(), sum, findings);
  }
}

