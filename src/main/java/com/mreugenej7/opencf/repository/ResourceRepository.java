package com.mreugenej7.opencf.repository;

import com.mreugenej7.opencf.model.Resource;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResourceRepository extends MongoRepository<Resource, ObjectId> {
    List<Resource> findAllByService(ObjectId serviceId);
}
