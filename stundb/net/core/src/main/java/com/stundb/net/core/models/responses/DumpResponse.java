package com.stundb.net.core.models.responses;

import java.util.List;
import java.util.Map;

public record DumpResponse(List<Map.Entry<String, Object>> dump) {}
