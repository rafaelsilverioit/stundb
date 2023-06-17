package com.stundb.mappers;

import com.stundb.models.ApplicationConfig;
import com.stundb.configuration.ApplicationProperties;
import com.stundb.models.Capacity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface ApplicationConfigMapper {

    ApplicationConfigMapper INSTANCE = Mappers.getMapper(ApplicationConfigMapper.class);

    @Mapping(target = "capacity", expression = "java(mapCapacity(properties.getCapacities()))")
    ApplicationConfig map(ApplicationProperties properties);

    default Capacity mapCapacity(Map<String, String> capacities) {
        return new Capacity(capacities);
    }
}
