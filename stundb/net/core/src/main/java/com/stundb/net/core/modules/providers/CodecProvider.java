package com.stundb.net.core.modules.providers;

import com.stundb.core.crdt.Entry;
import com.stundb.core.models.Node;
import com.stundb.net.core.codecs.Codec;
import com.stundb.net.core.codecs.fury.FuryCodec;
import com.stundb.net.core.models.Command;
import com.stundb.net.core.models.Type;
import com.stundb.net.core.models.Version;
import com.stundb.net.core.models.requests.*;
import com.stundb.net.core.models.responses.*;

import io.fury.Fury;
import io.fury.ThreadLocalFury;
import io.fury.config.Language;

import jakarta.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CodecProvider implements Provider<Codec> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Codec get() {
        var threadSafeFury =
                new ThreadLocalFury(
                        classLoader -> {
                            Fury fury =
                                    Fury.builder()
                                            .withLanguage(Language.JAVA)
                                            .requireClassRegistration(true)
                                            .withClassLoader(classLoader)
                                            .build();
                            fury.register(byte[].class);
                            fury.register(String.class);
                            fury.register(Type.class);
                            fury.register(Command.class);
                            fury.register(Version.class);
                            fury.register(com.stundb.net.core.models.Status.class);
                            fury.register(Request.class);
                            fury.register(Response.class);
                            fury.register(CRDTRequest.class);
                            fury.register(DelRequest.class);
                            fury.register(DeregisterRequest.class);
                            fury.register(ElectedRequest.class);
                            fury.register(ExistsRequest.class);
                            fury.register(GetRequest.class);
                            fury.register(RegisterRequest.class);
                            fury.register(SetRequest.class);
                            fury.register(CapacityResponse.class);
                            fury.register(DumpResponse.class);
                            fury.register(ErrorResponse.class);
                            fury.register(ExistsResponse.class);
                            fury.register(GetResponse.class);
                            fury.register(IsEmptyResponse.class);
                            fury.register(ListNodesResponse.class);
                            fury.register(RegisterResponse.class);
                            fury.register(Node.class);
                            fury.register(com.stundb.core.models.Status.class);
                            fury.register(com.stundb.core.models.Status.State.class);
                            fury.register(com.stundb.net.core.models.Status.class);
                            fury.register(Instant.class);
                            fury.register(Collection.class);
                            fury.register(Set.class);
                            fury.register(Map.class);
                            fury.register(Map.Entry.class);
                            fury.register(Entry.class);
                            try {
                                fury.register(
                                        Class.forName("java.util.ImmutableCollections$ListN"));
                                fury.register(
                                        Class.forName("java.util.ImmutableCollections$List12"));
                            } catch (ClassNotFoundException e) {
                                logger.error("Class not found", e);
                            }
                            return fury;
                        });
        return new FuryCodec(threadSafeFury);
    }
}
