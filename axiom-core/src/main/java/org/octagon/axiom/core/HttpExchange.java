package org.octagon.axiom.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HttpExchange(
    int status,
    Map<String, List<String>> headers,
    Optional<String> bodyBytesBase64,
    String contentType,
    long durationMs
) {}

