package dev.mmatokovic.pantrio.storage;

import dev.mmatokovic.pantrio.storage.dto.StorageRequest;
import dev.mmatokovic.pantrio.storage.dto.StorageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StorageMapper {
    StorageResponse toResponse(Storage storage);

    List<StorageResponse> toResponseList(List<Storage> storages);

    @Mapping(target = "id", ignore = true)
    Storage toEntity(StorageRequest request);

}
