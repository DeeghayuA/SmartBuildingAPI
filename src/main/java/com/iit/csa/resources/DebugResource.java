package com.iit.csa.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/debug")
public class DebugResource {

    @GET
    @Path("/error")
    @Produces(MediaType.APPLICATION_JSON)
    public void triggerError() {
        // This will be caught by the GenericExceptionMapper
        throw new RuntimeException("Test Internal Server Error");
    }
}
