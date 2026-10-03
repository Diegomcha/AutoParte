package me.diegomcha.autoparte.api.config;

import me.diegomcha.autoparte.api.config.dto.ConfigDtoRequest;
import me.diegomcha.autoparte.api.config.dto.ConfigDtoResponse;
import me.diegomcha.autoparte.api.config.dto.PublicConfigDtoResponse;
import me.diegomcha.autoparte.domain.Configuration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface ConfigMapper {

    ConfigDtoResponse toResponse(Configuration config);

    void fromUpdate(ConfigDtoRequest dto, @MappingTarget Configuration configuration);

    PublicConfigDtoResponse toPublicResponse(Configuration config);
}
