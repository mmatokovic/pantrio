package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.app.item.ItemMapper;
import dev.mmatokovic.pantrio.domain.instance.Instance;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = ItemMapper.class)
public interface StorageMapper {

    StorageResponse toResponse(Storage storage);

    List<StorageResponse> toResponseList(List<Storage> storages);

    StorageItemResponse toItemResponse(Instance instance);

    List<StorageItemResponse> toItemResponseList(List<Instance> instances);
}
