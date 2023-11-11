package com.stundb.net.core.modules.providers;

import com.stundb.core.codecs.Codec;
import com.stundb.core.codecs.kryo.DefaultKryoContext;
import com.stundb.core.crdt.Entry;
import com.stundb.core.models.Node;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CodecProvider implements Provider<Codec> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Codec get() {
        return DefaultKryoContext.newKryoContextFactory(kryo -> {
            kryo.setAutoReset(true);
            kryo.setReferences(true);
            kryo.register(byte[].class);
            kryo.register(String.class);
            kryo.register(Type.class);
            kryo.register(Command.class);
            kryo.register(Version.class);
            kryo.register(com.stundb.net.core.models.Status.class);
            kryo.register(Request.class);
            kryo.register(Response.class);
            kryo.register(CRDTRequest.class);
            kryo.register(DelRequest.class);
            kryo.register(ElectedRequest.class);
            kryo.register(ExistsRequest.class);
            kryo.register(GetRequest.class);
            kryo.register(RegisterRequest.class);
            kryo.register(SetRequest.class);
            kryo.register(CapacityResponse.class);
            kryo.register(DumpResponse.class);
            kryo.register(ExistsResponse.class);
            kryo.register(GetResponse.class);
            kryo.register(IsEmptyResponse.class);
            kryo.register(ListNodesResponse.class);
            kryo.register(RegisterResponse.class);
            kryo.register(ErrorResponse.class);
            kryo.register(Node.class);
            kryo.register(com.stundb.core.models.Status.class);
            kryo.register(com.stundb.core.models.Status.State.class);
            kryo.register(com.stundb.net.core.models.Status.class);
            kryo.register(Instant.class);
            kryo.register(Collection.class);
            kryo.register(Set.class);
            kryo.register(Map.class);
            kryo.register(Map.Entry.class);
            kryo.register(Entry.class);
            try {
                kryo.register(Class.forName("java.util.ImmutableCollections$ListN"));
                kryo.register(Class.forName("java.util.ImmutableCollections$List12"));
            } catch (ClassNotFoundException e) {
                logger.error("Class not found", e);
            }
        });
    }
}
