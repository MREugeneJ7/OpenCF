package com.mreugenej7.opencf.service;

import com.mreugenej7.opencf.model.*;
import com.mreugenej7.opencf.repository.ResourceRepository;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;

@Service
@Log4j2
public class ResourcesService {

    @Autowired
    ResourceRepository repository;

    public List<ResourceListDto> list(ObjectId serviceId) {

        var resourceList = Optional.ofNullable(serviceId)
                .map(s -> repository.findAllByService(s))
                .orElse(repository.findAll());

        return resourceList.stream().map(this::toListDto).toList();
    }

    private ResourceListDto toListDto(Resource resource) {
        return new ResourceListDto(resource.id(), resource.name(), resource.description());
    }

    @SneakyThrows
    public org.springframework.core.io.Resource download(ObjectId id) {
        var resource = repository.findById(id);
        if(resource.isPresent()) {
            log.debug("Resource found, retrieving file");
            return new ByteArrayResource(Files.readAllBytes(Path.of(resource.get().file())));
        }
        return null;
    }

    public ResourceCreateResponseDto create(ResourceCreateRequestDto resourceCreateRequestDto, MultipartFile file) {
        Path root = Paths.get("resources/" + resourceCreateRequestDto.getService().toString());
        if(!file.isEmpty()){
            log.debug("Setting file directory to service's repository");
            var destination = root.resolve(Paths.get(now() + "-" + file.getOriginalFilename())).normalize().toAbsolutePath();
            if(destination.getParent().equals(root.toAbsolutePath())){
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destination,
                            StandardCopyOption.REPLACE_EXISTING);
                    var resource = toResource(resourceCreateRequestDto);
                    resource.file(destination.toString());
                    log.debug("File saved successfully saving resource to database");
                    var saved = repository.save(resource);
                    return toCreateResponseDto(saved);
                } catch (IOException e) {
                    log.error("File couldn't be saved");
                    return null;
                }
            }
        }
        log.warn("Sent empty resource");
        return null;
    }

    private ResourceCreateResponseDto toCreateResponseDto(Resource resource) {
        return new ResourceCreateResponseDto(resource.id(), resource.name(), resource.description());
    }

    private Resource toResource(ResourceCreateRequestDto resourceCreateRequestDto) {
        return new Resource(null, resourceCreateRequestDto.getName(), resourceCreateRequestDto.getDescription(),
                null, resourceCreateRequestDto.getService(), false);
    }
}
