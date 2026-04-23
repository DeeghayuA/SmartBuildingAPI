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

// The @Provider annotation is CRITICAL. It tells JAX-RS to load this automatically.
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // 1. Log the actual error to the server console so YOU (the developer) can still debug it
        System.err.println("CRITICAL SERVER ERROR CAUGHT BY GLOBAL MAPPER: ");
        exception.printStackTrace();
        
        // 2. Hide the error from the user/hacker! Build a highly polished, secure JSON response
        String secureErrorJson = "{\n" +
                "  \"error\": \"Internal Server Error\",\n" +
                "  \"message\": \"An unexpected system error occurred. Our engineering team has been notified.\",\n" +
                "  \"status\": 500\n" +
                "}";

        // Return the 500 Internal Server Error with the safe JSON body
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(secureErrorJson)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
