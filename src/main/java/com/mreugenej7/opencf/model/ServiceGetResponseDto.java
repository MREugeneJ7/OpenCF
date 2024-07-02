package com.mreugenej7.opencf.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceGetResponseDto {
    @JsonSerialize(using= ToStringSerializer.class)
    ObjectId id;

    String name;

    String description;

    Map<String, Type> params;
}
