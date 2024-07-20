package com.rrd.c1ux.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.rrd.c1ux.api.models.ServiceVersion;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("utilsServiceVersionController")
@RequestMapping(RouteConstants.VERSION)
@Tag(name = "version")
public class VersionController {

    @Cacheable("apiVersion")
    @GetMapping(
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(summary = "Service Version", 
        description = "Returns the current project version")
    public ServiceVersion get() {

        // the following does NOT work in debug mode
        String version = getClass().getPackage().getImplementationVersion();
        return new ServiceVersion(version);
    }
}
