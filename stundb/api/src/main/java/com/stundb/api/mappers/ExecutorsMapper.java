package com.stundb.api.mappers;

import com.stundb.api.models.Executor;
import com.stundb.api.models.Executors;

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
    String SCHEDULER = "scheduler";
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
    @Mapping(
            target = SCHEDULER,
            expression = "java(mapExecutor(properties.get(\"" + SCHEDULER + "\")))")
    Executors mapExecutors(Map<String, Map<String, Integer>> properties);

    default Executor mapExecutor(Map<String, Integer> data) {
        return new Executor(data.get("threads"));
    }
}
