package com.mreugenej7.opencf.repository;

import com.mreugenej7.opencf.model.Service;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceRepository extends MongoRepository<Service, ObjectId> {
}
