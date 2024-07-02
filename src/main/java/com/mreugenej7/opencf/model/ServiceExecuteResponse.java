package com.mreugenej7.opencf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExecuteResponse {
    int exitCode;
    String errors;
    String output;
    List<ResourceListDto> outputs;
}
