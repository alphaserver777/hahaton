package org.octagon.axiom.core;

import java.util.Map;

// Профиль авторизации (bearer/api-key)
public record AuthProfile(String type, String role, Map<String, String> params) {}

