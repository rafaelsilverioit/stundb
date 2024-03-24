package com.stundb.net.core.models.responses;

import com.stundb.api.crdt.Entry;
import com.stundb.net.core.models.Node;

import java.util.Collection;

public record PingResponse(
        Collection<Entry> added, Collection<Entry> removed, Collection<Node> nodes) {}
