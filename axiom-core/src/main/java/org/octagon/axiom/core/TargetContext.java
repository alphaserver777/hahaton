package org.octagon.axiom.core;

import java.net.URI;
import java.util.List;
import java.util.Map;

// Контекст цели: базовый URL, профили авторизации, параметры выполнения
public record TargetContext(
    URI baseUrl,
    List<AuthProfile> auth,
    ExecutionProfile exec,
    Map<String, Object> extras
) {}

