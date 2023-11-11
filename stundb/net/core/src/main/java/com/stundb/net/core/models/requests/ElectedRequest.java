package com.stundb.net.core.models.requests;

import com.stundb.core.models.Node;

public record ElectedRequest(Node leader) {
}
