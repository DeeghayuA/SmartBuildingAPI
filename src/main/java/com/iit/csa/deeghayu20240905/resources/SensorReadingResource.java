/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.deeghayu20240905.resources;

/**
 *
 * @author deegh
 */

import com.iit.csa.deeghayu20240905.dao.Database;
import com.iit.csa.deeghayu20240905.models.Sensor;
import com.iit.csa.deeghayu20240905.models.SensorReading;
import com.iit.csa.deeghayu20240905.exceptions.LinkedResourceNotFoundException; 
import com.iit.csa.deeghayu20240905.exceptions.SensorUnavailableException; 


import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class SensorReadingResource {
    // The sub-resource holds onto the parent ID passed from SensorResource
    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // 1. GET /api/v1/sensors/{sensorId}/readings (Fetch historical telemetry data)
    @GET
    public Response getHistoricalReadings() {
        // Verify the parent sensor actually exists
        if (!Database.sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found.\"}")
                    .build();
        }

        // Return the historical list of readings
        List<SensorReading> history = Database.sensorReadings.get(sensorId);
        return Response.ok(history).build();
    }

    // 2. POST /api/v1/sensors/{sensorId}/readings (Append logs + Trigger Side Effect)
    @POST
    public Response addReading(SensorReading newReading, @Context UriInfo uriInfo) {
        // Verify the parent sensor actually exists
        Sensor parentSensor = Database.sensors.get(sensorId);
        
        if (parentSensor == null) {
            throw new LinkedResourceNotFoundException("Cannot add reading. Parent sensor not found.");
        }
        
        // Throw 403 Forbidden if the sensor is not active
        if ("INACTIVE".equalsIgnoreCase(parentSensor.getStatus()) || "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is currently " + parentSensor.getStatus() + ". Telemetry cannot be updated.");
        }

        // Generate ID and set the server timestamp for the reading
        String readingId = UUID.randomUUID().toString();
        newReading.setId(readingId);
        
        // If the client didn't provide a timestamp, generate a realistic one (current Epoch time)
        if (newReading.getTimestamp() == 0) {
            newReading.setTimestamp(System.currentTimeMillis());
        }

        // Save the reading into the historical list
        Database.sensorReadings.get(sensorId).add(newReading);

        // =================================================================
        // THE SIDE-EFFECT LOGIC: Update the parent Sensor's current value
        // =================================================================
        parentSensor.setCurrentValue(newReading.getValue());
        // =================================================================

        // Return 201 Created with Location Header
        URI uri = uriInfo.getAbsolutePathBuilder().path(readingId).build();
        return Response.created(uri).entity(newReading).build();
    }
    
}
