package com.stundb.net.core.models.responses;

import com.stundb.net.core.models.Node;

import java.util.Collection;

public record ListNodesResponse(Collection<Node> nodes) {}
