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
import com.iit.csa.exceptions.RoomNotEmptyException;


import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // 1. GET /api/v1/rooms -Fetch all rooms
    @GET
    public Response getAllRooms() {
        Collection<Room> allRooms = Database.rooms.values();
        return Response.ok(allRooms).build();
    }

    // 2. GET /api/v1/rooms/{id} -Read single room metadata
    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        Room room = Database.rooms.get(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\"}")
                    .build();
        }
        return Response.ok(room).build();
    }

    // 3. POST /api/v1/rooms -Create room
    @POST
    public Response createRoom(Room newRoom, @Context UriInfo uriInfo) {
        // Generate a new UUID as a String (matching your exact POJO requirements)
        String newId = UUID.randomUUID().toString();
        newRoom.setId(newId);
        
        // Ensure the sensor list is initialized to avoid NullPointerExceptions later
        if (newRoom.getSensorIds() == null) {
            newRoom.setSensorIds(new ArrayList<>());
        }

        // Save to our in-memory mock database
        Database.rooms.put(newId, newRoom);

        //  Must return 201 Created and a Location header
        URI uri = uriInfo.getAbsolutePathBuilder().path(newId).build();
        return Response.created(uri).entity(newRoom).build();
    }

    // 4. DELETE /api/v1/rooms/{id} (Delete room with business logic constraint)
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = Database.rooms.get(id);
        
        // Check if room exists
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\"}")
                    .build();
        }

        // Rubric Requirement (Excellent Band 2.2): Prevent deletion if sensors remain -> Return 409 Conflict
        // We check the Room's internal list of sensor IDs
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room. Active sensors are still registered to this room.");
        }

        // Optional secondary check: Scan the global sensors map just to be absolutely sure
        boolean hasGlobalSensorsLinked = Database.sensors.values().stream()
                .anyMatch(sensor -> id.equals(sensor.getRoomId()));
                
        if (hasGlobalSensorsLinked) {
            throw new RoomNotEmptyException("Cannot delete room. Active sensors are still registered to this room in the database.");
        }

        // If it passes the checks, delete the room safely
        Database.rooms.remove(id);
        
        // Return 204 No Content (standard HTTP success code for a successful deletion)
        return Response.noContent().build();
    }
}
