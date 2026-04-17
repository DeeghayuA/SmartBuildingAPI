/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iit.csa.filters;

/**
 *
 * @author deegh
 */
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider

public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter{
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("====== INCOMING REQUEST ======");
        LOGGER.info("Method: " + requestContext.getMethod());
        LOGGER.info("URI: " + requestContext.getUriInfo().getAbsolutePath());
    }
    
    // RUNS AFTER RESOURCE IS DONE, RIGHT BEFORE SENDING TO CLIENT
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("====== OUTGOING RESPONSE ======");
        LOGGER.info("Status Code: " + responseContext.getStatus());
        LOGGER.info("===============================\n");
    }
}
