package me.diegomcha.autoparte.api.common;

import me.diegomcha.autoparte.domain.base.BaseEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    EntityDtoCreated toCreated(BaseEntity entity);

}
