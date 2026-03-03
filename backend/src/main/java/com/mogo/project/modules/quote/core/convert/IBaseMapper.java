package com.mogo.project.modules.quote.core.convert;

import org.mapstruct.InheritInverseConfiguration;

import java.util.List;

/**
 * 通用mapper接口
 * @param <D> vo/dto
 * @param <E> entity
 */
public interface IBaseMapper <D,E>{
    // Entity -> VO
    D toVo(E entity);

    // EntityList -> VoList
    List<D> toVoList(List<E> list);

    // VO -> Entity
    @InheritInverseConfiguration
    E toEntity(D dto);

    // VoList -> EntityList
    List<E> toEntityList(List<D> list);
}
