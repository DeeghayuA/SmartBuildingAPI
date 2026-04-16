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

// The @Provider annotation is CRITICAL. It tells JAX-RS to load this automatically.
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // 1. Log the real error to your NetBeans console so YOU can debug it
        exception.printStackTrace();

        // 2. Hide the error from the user! Build a clean, secure JSON response.
        String secureErrorJson = "{\n" +
                "  \"errorCode\": 500,\n" +
                "  \"errorMessage\": \"An internal server error occurred. Our team has been notified.\"\n" +
                "}";

        // Return the 500 Internal Server Error with the safe JSON body
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(secureErrorJson)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
