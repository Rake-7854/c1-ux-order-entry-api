package com.rrd.c1ux.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("HealthController")
@RequestMapping(RouteConstants.HEALTH)
@Tag(name = "health")
public class HealthController {
    
    @GetMapping
    @Operation(
        summary = "Get service health", description = "Gets the health of the service (online, offline, etc)")
    public String getHealth() {

        //TODO: if in maintenance mode, etc, return "offline"
        return "online";
    }
}
