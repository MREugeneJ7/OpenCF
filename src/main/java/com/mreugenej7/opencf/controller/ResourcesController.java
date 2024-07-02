package com.mreugenej7.opencf.controller;

import com.mreugenej7.opencf.model.*;
import com.mreugenej7.opencf.service.ResourcesService;
import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/resource")
@Log4j2
public class ResourcesController {
    @Autowired
    ResourcesService service;


    @GetMapping
    public List<ResourceListDto> list(@RequestParam(name = "serviceId", required = false) ObjectId serviceId){
        log.info("User requested resource list for service {}", serviceId);
        return service.list(serviceId);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public org.springframework.core.io.Resource download(@PathVariable ObjectId id){
        log.info("User requested file represented by resource with id {}", id);
        return service.download(id);
    }

    @PostMapping
    public ResourceCreateResponseDto create(ResourceCreateRequestDto resourceCreateRequestDto, @RequestParam("file") MultipartFile file){
        return this.service.create(resourceCreateRequestDto, file);
    }
}
