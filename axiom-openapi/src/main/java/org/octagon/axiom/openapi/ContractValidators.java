package org.octagon.axiom.openapi;

import java.util.ArrayList;
import java.util.List;
import org.octagon.axiom.core.ContractValidator;
import org.octagon.axiom.core.HttpExchange;
import org.octagon.axiom.core.TestCase;

// Набор простых контракт-валидаторов (MVP)
public final class ContractValidators {
  private ContractValidators() {}

  public static ContractValidator basic() {
    return new ContractValidator() {
      @Override public List<String> validate(TestCase tc, HttpExchange exchange) {
        var issues = new ArrayList<String>();
        if (exchange.status() >= 500) {
          issues.add("ServerError:" + exchange.status());
        }
        if (exchange.contentType() == null || exchange.contentType().isBlank()) {
          issues.add("MissingContentType");
        }
        return issues;
      }
    };
  }
}

