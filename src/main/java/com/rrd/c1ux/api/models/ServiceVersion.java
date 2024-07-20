package com.rrd.c1ux.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ServiceVersion", 
    title = "Service Version",
    description = "Holds the vesion of the API")
public class ServiceVersion {
    
    private String version;
}
