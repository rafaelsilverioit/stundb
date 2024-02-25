package com.stundb.core.mappers;

import com.stundb.core.configuration.ApplicationProperties;
import com.stundb.core.models.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper(uses = ExecutorsMapper.class)
public interface ApplicationConfigMapper {

    ApplicationConfigMapper INSTANCE = Mappers.getMapper(ApplicationConfigMapper.class);

    @Mapping(target = "capacity", expression = "java(mapCapacity(properties.getCapacities()))")
    @Mapping(target = "timeouts", expression = "java(mapTimeouts(properties.getTimeouts()))")
    @Mapping(target = "backoffSettings", source = "backoffSettings")
    ApplicationConfig map(ApplicationProperties properties);

    default Capacity mapCapacity(Map<String, String> capacities) {
        return new Capacity(capacities);
    }

    default Timeouts mapTimeouts(Map<String, String> timeouts) {
        return new Timeouts(timeouts);
    }
}
