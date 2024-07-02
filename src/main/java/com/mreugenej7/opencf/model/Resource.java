package com.mreugenej7.opencf.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("resource")
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    @JsonSerialize(using= ToStringSerializer.class)
    ObjectId id;

    String name;

    String description;

    String file;

    ObjectId service;

    Boolean isOutput;
}
