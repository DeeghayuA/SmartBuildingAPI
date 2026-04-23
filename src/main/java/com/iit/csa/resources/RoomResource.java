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
        String id = newRoom.getId();
        
        // If no ID provided, generate one (fallback)
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
            newRoom.setId(id);
        }

        // Rubric/Test Requirement: Check for duplicates -> 409 Conflict
        if (Database.rooms.containsKey(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Room with ID " + id + " already exists.\"}")
                    .build();
        }
        
        // Ensure the sensor list is initialized
        if (newRoom.getSensorIds() == null) {
            newRoom.setSensorIds(new ArrayList<>());
        }

        // Save to our in-memory mock database
        Database.rooms.put(id, newRoom);

        // Return 201 Created and a Location header
        URI uri = uriInfo.getAbsolutePathBuilder().path(id).build();
        return Response.created(uri).entity(newRoom).build();
    }

    // 4. DELETE /api/v1/rooms/{id} (Delete room with business logic constraint)
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = Database.rooms.get(id);
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\"}")
                    .build();
        }

        // Prevent deletion if sensors remain -> 409 Conflict
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room. Active sensors are still registered.");
        }

        Database.rooms.remove(id);
        
        // Test case expects 200 OK for success
        return Response.ok("{\"message\": \"Room deleted successfully\"}").build();
    }
}
