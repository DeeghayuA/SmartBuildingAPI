/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.resources;

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
        metadata.put("developer", "Deeghayu (w2119769)");
        metadata.put("student_id", "20240905");
        metadata.put("contact", "w2119769@westminster.ac.uk");

        Map<String, String> endpoints = new HashMap<>(); 
        endpoints.put("rooms", "/api/v1/rooms");
        endpoints.put("sensors", "/api/v1/sensors");
        endpoints.put("debug", "/api/v1/debug/error");

        metadata.put("resources", endpoints);

        return Response.ok(metadata).build();
    }

}
