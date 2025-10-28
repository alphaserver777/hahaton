package org.octagon.axiom.core;

// Профиль выполнения (лимиты, таймауты)
public record ExecutionProfile(int concurrency, int rateLimitRps, int timeoutMs, int retries) {}

