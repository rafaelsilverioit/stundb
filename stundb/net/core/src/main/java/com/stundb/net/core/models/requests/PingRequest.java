package com.stundb.net.core.models.requests;

import java.util.Map;

public record PingRequest(Map<String, Long> versionClock) {}
