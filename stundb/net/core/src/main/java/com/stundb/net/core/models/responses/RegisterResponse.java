package com.stundb.net.core.models.responses;

import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.requests.CRDTRequest;

import java.util.Collection;

public record RegisterResponse(Collection<Node> nodes, CRDTRequest state) {}
