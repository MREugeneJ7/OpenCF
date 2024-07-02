package com.mreugenej7.opencf.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceListDto {
    @JsonSerialize(using= ToStringSerializer.class)
    ObjectId id;

    String name;

    String description;
}
