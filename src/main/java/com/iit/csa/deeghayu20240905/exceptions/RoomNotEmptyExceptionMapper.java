/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.deeghayu20240905.exceptions;

/**
 *
 * @author deegh
 */
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        String jsonError = "{\"error\": \"Conflict\", \"message\": \"" + exception.getMessage() + "\"}";
        return Response.status(Response.Status.CONFLICT) // 409
                .entity(jsonError)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
