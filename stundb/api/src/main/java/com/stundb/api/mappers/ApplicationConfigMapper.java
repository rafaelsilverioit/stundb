package com.stundb.api.mappers;

import com.stundb.api.configuration.ApplicationProperties;
import com.stundb.api.models.ApplicationConfig;
import com.stundb.api.models.Capacity;
import com.stundb.api.models.Timeouts;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper(uses = ExecutorsMapper.class)
public interface ApplicationConfigMapper {

    ApplicationConfigMapper INSTANCE = Mappers.getMapper(ApplicationConfigMapper.class);

    @Mapping(target = "capacity", expression = "java(mapCapacity(properties.getCapacities()))")
    @Mapping(target = "timeouts", expression = "java(mapTimeouts(properties.getTimeouts()))")
    ApplicationConfig map(ApplicationProperties properties);

    default Capacity mapCapacity(Map<String, String> capacities) {
        return new Capacity(capacities);
    }

    default Timeouts mapTimeouts(Map<String, String> timeouts) {
        return new Timeouts(timeouts);
    }
}
