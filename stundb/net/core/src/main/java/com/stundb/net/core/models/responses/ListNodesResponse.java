package com.stundb.net.core.models.responses;

import com.stundb.core.models.Node;

import java.util.Collection;

public record ListNodesResponse(Collection<Node> nodes) {}
