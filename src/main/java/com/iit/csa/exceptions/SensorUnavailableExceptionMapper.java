/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.exceptions;

/**
 *
 * @author deegh
 */
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider

public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        String jsonError = "{\"error\": \"Forbidden\", \"message\": \"" + exception.getMessage() + "\"}";
        return Response.status(Response.Status.FORBIDDEN) // 403
                .entity(jsonError)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
