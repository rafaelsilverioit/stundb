package com.stundb.net.core.models.responses;

import com.stundb.core.models.Node;

import java.util.Collection;
import java.util.List;

public record ListNodesResponse(Collection<Node> nodes) {
}
