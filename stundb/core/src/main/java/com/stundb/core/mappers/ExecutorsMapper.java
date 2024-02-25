package com.stundb.core.mappers;

import com.stundb.core.models.Executor;
import com.stundb.core.models.Executors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface ExecutorsMapper {

    String TCP_CLIENT = "tcpClient";
    String MAIN_SERVER_LOOP = "mainServerLoop";
    String SECONDARY_SERVER_LOOP = "secondaryServerLoop";
    String INITIALIZER = "initializer";
    ExecutorsMapper INSTANCE = Mappers.getMapper(ExecutorsMapper.class);

    @Mapping(
            target = TCP_CLIENT,
            expression = "java(mapExecutor(properties.get(\"" + TCP_CLIENT + "\")))")
    @Mapping(
            target = MAIN_SERVER_LOOP,
            expression = "java(mapExecutor(properties.get(\"" + MAIN_SERVER_LOOP + "\")))")
    @Mapping(
            target = SECONDARY_SERVER_LOOP,
            expression = "java(mapExecutor(properties.get(\"" + SECONDARY_SERVER_LOOP + "\")))")
    @Mapping(
            target = INITIALIZER,
            expression = "java(mapExecutor(properties.get(\"" + INITIALIZER + "\")))")
    Executors mapExecutors(Map<String, Map<String, Integer>> properties);

    default Executor mapExecutor(Map<String, Integer> data) {
        return new Executor(data.get("threads"));
    }
}
