package com.mreugenej7.opencf.controller;

import com.mreugenej7.opencf.model.*;
import com.mreugenej7.opencf.service.ServicesService;
import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/service")
@Log4j2
public class ServicesController {
    @Autowired
    ServicesService service;


    @GetMapping
    public List<ServiceListDto> list(){
        log.info("User requested service list");
        return service.list();
    }

    @GetMapping("/{id}")
    public ServiceGetResponseDto get(@PathVariable ObjectId id){
        log.info("User requested details for service with id: {}", id);
        return service.get(id);
    }

    @PostMapping
    public ServiceCreateResponseDto create(ServiceCreateRequestDto service, @RequestParam("file") MultipartFile file){
        log.info("User creating service {}", service.toString());
        return this.service.create(service, file);
    }

    @PostMapping("/execute/{id}")
    public ServiceExecuteResponse execute(ServiceExecuteRequest execution, @PathVariable("id") ObjectId id){
        log.info("User trying to execute service {} with params: {}", id.toString(), execution.toString());
        return service.execute(execution, id);
    }
}
