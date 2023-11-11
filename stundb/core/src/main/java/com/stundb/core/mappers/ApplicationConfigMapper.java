package com.stundb.core.mappers;

import com.stundb.core.configuration.ApplicationProperties;
import com.stundb.core.models.ApplicationConfig;
import com.stundb.core.models.Capacity;
import com.stundb.core.models.TcpClient;
import com.stundb.core.models.Timeouts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper
public interface ApplicationConfigMapper {

    ApplicationConfigMapper INSTANCE = Mappers.getMapper(ApplicationConfigMapper.class);

    @Mapping(target = "capacity", expression = "java(mapCapacity(properties.getCapacities()))")
    @Mapping(target = "timeouts", expression = "java(mapTimeouts(properties.getTimeouts()))")
    @Mapping(target = "tcpClient", expression = "java(mapTcpClient(properties.getTcpClient()))")
    ApplicationConfig map(ApplicationProperties properties);

    default Capacity mapCapacity(Map<String, String> capacities) {
        return new Capacity(capacities);
    }

    default Timeouts mapTimeouts(Map<String, String> timeouts) {
        return new Timeouts(timeouts);
    }

    default TcpClient mapTcpClient(Map<String, String> tcpClient) {
        return new TcpClient(tcpClient);
    }
}
