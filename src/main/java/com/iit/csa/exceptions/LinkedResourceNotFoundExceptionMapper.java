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
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException>{
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        String jsonError = "{\"error\": \"Unprocessable Entity\", \"message\": \"" + exception.getMessage() + "\"}";
        return Response.status(422) // 422 Unprocessable Entity
                .entity(jsonError)
                .type(MediaType.APPLICATION_JSON)
                .build();
    
}}
