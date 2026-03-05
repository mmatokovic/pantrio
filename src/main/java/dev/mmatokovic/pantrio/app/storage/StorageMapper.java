package dev.mmatokovic.pantrio.app.storage;

import dev.mmatokovic.pantrio.app.item.ItemMapper;
import dev.mmatokovic.pantrio.domain.instance.Instance;
import dev.mmatokovic.pantrio.domain.storage.Storage;
import dev.mmatokovic.pantrio.domain.storage.StorageItemRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageItemResponse;
import dev.mmatokovic.pantrio.domain.storage.StorageRequest;
import dev.mmatokovic.pantrio.domain.storage.StorageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface StorageMapper {

    StorageResponse toResponse(Storage storage);

    List<StorageResponse> toResponseList(List<Storage> storages);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Storage toEntity(StorageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storage", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Instance toInstance(StorageItemRequest request);

    @Mapping(source = "item", target = "item")
    @Mapping(source = "storage", target = "storage")
    StorageItemResponse toItemResponse(Instance instance);

    List<StorageItemResponse> toItemResponseList(List<Instance> instances);
}
