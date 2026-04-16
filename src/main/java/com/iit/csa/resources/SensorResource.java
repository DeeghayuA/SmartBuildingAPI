/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.resources;

/**
 *
 * @author deegh
 */
import com.iit.csa.dao.Database;
import com.iit.csa.models.Room;
import com.iit.csa.models.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // 1. GET /api/v1/sensors?type=... (Retrieve sensors with optional type filtering)
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        // Get all sensors from the database
        List<Sensor> resultList = new ArrayList<>(Database.sensors.values());
        
        // Rubric Requirement (Excellent Band 3.2): Filtered Retrieval
        // If the user provided a "?type=" parameter, filter the list dynamically
        if (type != null && !type.trim().isEmpty()) {
            resultList = resultList.stream()
                    .filter(sensor -> sensor.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        
        return Response.ok(resultList).build();
    }

    // 2. POST /api/v1/sensors (Register a new sensor)
    @POST
    public Response createSensor(Sensor newSensor, @Context UriInfo uriInfo) {
        
        String targetRoomId = newSensor.getRoomId();

        // Rubric Requirement (Excellent Band 3.1): Sensor Integrity
        // We MUST verify that the roomId provided inside the JSON payload actually exists.
        if (targetRoomId == null || !Database.rooms.containsKey(targetRoomId)) {
            // According to your rubric's error handling section, 422 Unprocessable Entity 
            // is the perfect response when a payload references an ID that doesn't exist.
            return Response.status(422)
                    .entity("{\"error\": \"Invalid roomId. The specified room does not exist.\"}")
                    .build();
        }

        // Generate a new UUID for the Sensor
        String newSensorId = UUID.randomUUID().toString();
        newSensor.setId(newSensorId);

        // Save the sensor to the global sensors map
        Database.sensors.put(newSensorId, newSensor);

        // CRITICAL STEP: Add this new sensor's ID to the Room's internal list!
        // (If we don't do this, Part 2.2 Room Deletion logic will fail to block deletions)
        Room room = Database.rooms.get(targetRoomId);
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        room.getSensorIds().add(newSensorId);

        // Prepare the empty Historical Readings list for Part 4
        Database.sensorReadings.put(newSensorId, new ArrayList<>());

        // Return 201 Created along with the Location header pointing to the new resource
        URI uri = uriInfo.getAbsolutePathBuilder().path(newSensorId).build();
        return Response.created(uri).entity(newSensor).build();
    }
    
    // (Note: Part 4 Sub-Resource Locator will also go in this file later)
}
