package me.diegomcha.autoparte.api.config;

import me.diegomcha.autoparte.api.config.dto.ConfigDtoRequest;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoResponse;
import me.diegomcha.autoparte.domain.Configuration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
abstract class ConfigMapper {

    public abstract ConfigDtoResponse toResponse(Configuration config);

    public abstract void fromUpdate(ConfigDtoRequest dto, @MappingTarget Configuration configuration);
}
