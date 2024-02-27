package com.stundb.net.core.models.requests;

import com.stundb.net.core.models.Node;

public record ElectedRequest(Node leader) {}
