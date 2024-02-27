package com.stundb.net.core.models.responses;

import com.stundb.net.core.models.Node;
import com.stundb.net.core.models.requests.CRDTRequest;

import java.util.List;

public record RegisterResponse(List<Node> nodes, CRDTRequest state) {}
