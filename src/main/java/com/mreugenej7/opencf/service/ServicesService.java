package com.mreugenej7.opencf.service;

import com.mreugenej7.opencf.model.ServiceCreateResponseDto;
import com.mreugenej7.opencf.model.*;
import com.mreugenej7.opencf.repository.ResourceRepository;
import com.mreugenej7.opencf.repository.ServiceRepository;
import com.mreugenej7.opencf.util.StreamGobbler;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

import static com.mreugenej7.opencf.model.LaunchMethod.MPI;
import static java.time.Instant.now;

@Service
@Log4j2
public class ServicesService {

    @Autowired
    ServiceRepository repository;

    @Autowired
    ResourceRepository resourceRepository;

    public List<ServiceListDto> list() {
        var allServices = repository.findAll();
        log.debug("Correctly retrieved {} services", allServices.size());
        return allServices.stream().map(this::toListDto).toList();
    }

    private ServiceListDto toListDto(com.mreugenej7.opencf.model.Service s) {
        return new ServiceListDto(s.id(), s.name(), s.description());
    }

    public ServiceGetResponseDto get(ObjectId id) {
        return repository.findById(id).map(this::toResponseDto).orElse(null);
    }

    private ServiceGetResponseDto toResponseDto(com.mreugenej7.opencf.model.Service s) {
        return new ServiceGetResponseDto(s.id(), s.name(), s.description(), s.params());
    }

    public ServiceCreateResponseDto create(ServiceCreateRequestDto service, MultipartFile file) {
        if(MPI.equals(service.getLaunchMethod()) && !service.getParams().containsKey("np")){
            log.error("Service request is of type MPI and does not contain an np param");
            return null;
        }
        Path root = Paths.get("binaries");
        if(!file.isEmpty()){
            var destination = root.resolve(Paths.get(now().toString() + "-" + file.getOriginalFilename())).normalize().toAbsolutePath();
            log.debug("Binary to be saved in {}", destination.toString());
            if(destination.getParent().equals(root.toAbsolutePath())){
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destination,
                            StandardCopyOption.REPLACE_EXISTING);
                    log.debug("Ensuring binary is executable");
                    new File(destination.toString()).setExecutable(true);
                    var serviceToSave = toService(service);
                    serviceToSave.binaryLocation(destination.toString());
                    log.info("All preparations went successfully saving service to database");
                    var savedService = repository.save(serviceToSave);
                    log.info("Service saved successfully creating repository for resources");
                    new File("resources/" + savedService.id().toString()).mkdirs();
                    return toCreateResponseDto(savedService);
                } catch (IOException e) {
                    log.error("Something went wrong while saving binary or creating repository");
                    return null;
                }
            }
        }
        log.warn("Provided binary is not valid");
        return null;
    }

    private ServiceCreateResponseDto toCreateResponseDto(com.mreugenej7.opencf.model.Service savedService) {
        return new ServiceCreateResponseDto(savedService.id(), savedService.name(), savedService.description(), savedService.launchMethod(), savedService.params());
    }

    private com.mreugenej7.opencf.model.Service toService(ServiceCreateRequestDto service) {
        return new com.mreugenej7.opencf.model.Service(null, service.getName(), service.getDescription(), service.getLaunchMethod(), null, createParamStringIfNotPresent(service), service.getParams());
    }

    private String createParamStringIfNotPresent(ServiceCreateRequestDto service) {
        if(Objects.isNull(service.getParamString())){
            log.debug("param string not provided, creating one");
            var paramString = new StringBuilder();
            service.getParams().forEach((k,v) -> {
                paramString.append(String.format(" <param[%s]>", k));
            });
            log.debug("Created param string: {}", paramString.toString());
            return paramString.toString();
        }
        //TODO: validate maybe?
        return service.getParamString();
    }

    @SneakyThrows
    public ServiceExecuteResponse execute(ServiceExecuteRequest execution, ObjectId id) {
        var serviceOptional = repository.findById(id);
        if(serviceOptional.isPresent()){
            log.info("Service loaded successfully, proceeding to prepare execution");
            var service = serviceOptional.get();
            log.debug("Building launch command: Adding binary location");
            var launchCommand = service.launchMethod().getLaunchCommand().replace("<location>", service.binaryLocation());
            log.debug("Building launch command: Appending paramString");
            launchCommand = launchCommand.concat(service.paramString());
            for(var param : execution.getParams().entrySet()){
                if(validParamAndType(param, service.params())){
                    log.debug("""
                               Param is valid
                               Building launch command: Adding param location
                               """);
                    if(Type.RESOURCE.equals(service.params().get(param.getKey()))) {
                        log.debug("param is of type Resource, retrieving resource location from ID");
                        launchCommand = launchCommand.replace(String.format("<param[%s]>", param.getKey()),
                                resourceRepository.findById(new ObjectId(param.getValue()))
                                        .map(Resource::file).get());
                    }
                    else
                        launchCommand = launchCommand.replace(String.format("<param[%s]>", param.getKey()),
                                param.getValue());
                }
            }
            log.debug("""
                       Launch command built
                       Creating working dir
                       """);
            Path workdir = Paths.get("resources/" + service.id().toString() + "/" + now().toString());
            workdir.toFile().mkdirs();
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(workdir.toFile());
            builder.command("sh", "-c", launchCommand);
            log.info("All preparations were successful, executing");
            var process =builder.start();
            var input = new StringBuilder();
            var inputCollector = new StreamGobbler(process.getInputStream(), (s) -> input.append(s).append("\n"));
            var errorOutput = new StringBuilder();
            var errorOutputCollector = new StreamGobbler(process.getErrorStream(), (s) -> errorOutput.append(s).append("\n"));
            var ex = Executors.newFixedThreadPool(3);
            var inputFuture = ex.submit(inputCollector);
            var errorFuture = ex.submit(errorOutputCollector);
            int exitCode = process.waitFor();
            errorFuture.get();
            inputFuture.get();
            var outputFiles = Files.walk(workdir).filter(Files::isRegularFile)
                    .map(a -> createAndSaveResource(a, service.id())).map(this::toResourceListDto).toList();
            return new ServiceExecuteResponse(exitCode, errorOutput.toString(), input.toString(), outputFiles);
        }
        log.error("Service not found");
        return new ServiceExecuteResponse(-1, "Service Does Not Exist", "", Collections.emptyList());
    }

    private ResourceListDto toResourceListDto(Resource resource) {
        return new ResourceListDto(resource.id(), resource.name(), resource.description());
    }

    private Resource createAndSaveResource(Path a, ObjectId id) {
        return resourceRepository.save(new Resource(null,
                a.getFileName().toString(),
                "Output",
                a.toAbsolutePath().toString(),
                id,
                true));
    }

    private boolean validParamAndType(Map.Entry<String, String> param, Map<String, Type> params) {
        log.debug("Checking Validity of param {}-{}", param.getKey(), param.getValue());
        if(params.containsKey(param.getKey())){
            log.debug("Param name exists in service, checking type validity");
            return switch (params.get(param.getKey())){
                case STRING -> true;
                case INT -> {
                    try{
                        Integer.parseInt(param.getValue());
                        yield true;
                    }catch (Exception e){
                        log.error("Param {} is not of type int", param.getKey());
                        yield false;
                    }
                }
                case FLOAT -> {
                    try{
                        Float.parseFloat(param.getValue());
                        yield true;
                    }catch (Exception e){
                        log.error("Param {} is not of type float", param.getKey());
                        yield false;
                    }
                }
                case RESOURCE -> resourceRepository.existsById(new ObjectId(param.getValue()));
            };
        }
        log.error("Param name does not exist in service");
        return false;
    }
}
