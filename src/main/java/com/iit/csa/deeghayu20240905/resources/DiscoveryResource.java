/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.deeghayu20240905.resources;

/**
 *
 * @author deegh
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        Map<String, Object> metadata = new HashMap<>(); // Main map to hold all discovery information
        metadata.put("version", "1.0");
        metadata.put("developer", "Deeghayu");
        metadata.put("contact", "student@my.westminster.ac.uk");

        Map<String, String> endpoints = new HashMap<>(); // Application State Resource Map.
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");

        metadata.put("resources", endpoints);

        return Response.ok(metadata).build();
    }

}
