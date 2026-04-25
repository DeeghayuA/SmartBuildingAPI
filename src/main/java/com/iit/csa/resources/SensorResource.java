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
import com.iit.csa.exceptions.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // 1. GET /api/v1/sensors?type=... (Retrieve sensors with optional type filtering)
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> resultList = new ArrayList<>(Database.sensors.values());
        
        if (type != null && !type.trim().isEmpty()) {
            resultList = resultList.stream()
                    .filter(sensor -> sensor.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        
        return Response.ok(resultList).build();
    }

    // 1b. GET /api/v1/sensors/{id} (Retrieve single sensor)
    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = Database.sensors.get(id);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // 2. POST /api/v1/sensors (Register a new sensor)
    @POST
    public Response createSensor(Sensor newSensor, @Context UriInfo uriInfo) {
        
        String targetRoomId = newSensor.getRoomId();

        if (targetRoomId == null || !Database.rooms.containsKey(targetRoomId)) {
            // Rubric Part 5.2: Throw custom exception -> ExceptionMapper returns 422
            throw new LinkedResourceNotFoundException("Invalid roomId. The specified room does not exist.");
        }

        // Use provided ID or generate one
        String id = newSensor.getId();
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
            newSensor.setId(id);
        }

        // Save the sensor
        Database.sensors.put(id, newSensor);

        // Update the Room's internal list
        Room room = Database.rooms.get(targetRoomId);
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        room.getSensorIds().add(id);

        // Prepare the historical Readings list
        Database.sensorReadings.put(id, new ArrayList<>());

        // Return 201 Created
        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(newSensor).build();
    }
    
    // (Note: Part 4 Sub-Resource Locator will also go in this file later)
    // PART 4.1: The Sub-Resource Locator Pattern
    // Notice there is NO @GET or @POST here. It simply delegates the request.
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        // We pass the sensorId into the new resource so it knows which sensor it belongs to
        return new SensorReadingResource(sensorId);
    }
}

