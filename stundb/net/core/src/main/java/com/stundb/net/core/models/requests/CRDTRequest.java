package com.stundb.net.core.models.requests;

import com.stundb.api.crdt.Entry;

import java.util.Collection;

public record CRDTRequest(Collection<Entry> added, Collection<Entry> removed) {}
