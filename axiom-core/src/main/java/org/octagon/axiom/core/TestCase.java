package org.octagon.axiom.core;

import java.util.Map;
import java.util.Optional;

public record TestCase(
    String id,
    ApiRoute route,
    Map<String, String> headers,
    Optional<String> body,
    Optional<String> tokenRole,
    Map<String, Object> meta
) {}

