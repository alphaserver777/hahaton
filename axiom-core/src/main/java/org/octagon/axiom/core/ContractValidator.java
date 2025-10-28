package org.octagon.axiom.core;

import java.util.List;

public interface ContractValidator {
  // Возвращает список нарушений контракта
  List<String> validate(TestCase tc, HttpExchange exchange);
}

