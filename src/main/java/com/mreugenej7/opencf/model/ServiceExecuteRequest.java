package com.mreugenej7.opencf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExecuteRequest {

    Map<String, String> params;
}
