/*
 * Copyright (C) individee GmbH - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written 5 2022
 */

package com.individee.async;

import com.github.openjson.JSONObject;
import com.individee.WicketApplication;
import org.apache.wicket.Application;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * An {@link AbstractResource} as base for downloading Kettufy generated documents.
 *
 */
public class DownloadFileResource extends AbstractResource {

    /**
     * Instantiates a new Abstract download document resource.
     */
    public DownloadFileResource() {
        super();
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse resourceResponse = new ResourceResponse();
        try {
            String requestType = attributes.getParameters().get("type").toString();
            if (requestType != null) {
                switch (requestType) {
                    case "start":
                        startFileCreation(resourceResponse, attributes);
                        break;
                    case "abort":
                        abortFileCreation(resourceResponse, attributes);
                        break;
                    case "status":
                        getFileCreationStatus(resourceResponse, attributes);
                        break;
                    case "result":
                        getResult(resourceResponse, attributes);
                        break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resourceResponse;
    }

    private void getResult(ResourceResponse resourceResponse, Attributes attributes) throws Exception {
        String taskId = loadTaskId(attributes);
        resourceResponse.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                try {
                    File file = ((WicketApplication) Application.get()).getTaskManager().getResult(taskId, File.class);
                    FileResourceStream resource = new FileResourceStream(file);
                    ResourceStreamResource rsr = new ResourceStreamResource(resource);
                    rsr.setFileName(attributes.getParameters().get("file").toString());
                    rsr.setContentDisposition(ContentDisposition.ATTACHMENT);
                    Attributes a = new Attributes(attributes.getRequest(),
                            attributes.getResponse());
                    rsr.respond(a);
                    file.delete();
                } catch (Exception ex) {
                    sendSimpleResponse("stackError", ex.getMessage(), resourceResponse);
                }
            }
        });
    }

    private void getFileCreationStatus(ResourceResponse resourceResponse, Attributes attributes) {
        String taskId = loadTaskId(attributes);
        LoggerFactory.getLogger(getClass()).info("Checking status for " + taskId);
        sendSimpleResponse("done", ((WicketApplication) Application.get()).getTaskManager().isDone(taskId), resourceResponse);
    }

    private String loadTaskId(Attributes attributes) {
        String taskId = attributes.getParameters().get("tid").toString();
        if (taskId != null) {
            return taskId;
        }
        throw new AbortWithHttpErrorCodeException(404);
    }

    private void abortFileCreation(ResourceResponse resourceResponse, Attributes attributes) {
        String taskId = loadTaskId(attributes);
        ((WicketApplication) Application.get()).getTaskManager().cancel(taskId);
        sendSimpleResponse("status", "done", resourceResponse);
    }

    void sendSimpleResponse(String field, Object value, ResourceResponse resourceResponse) {
        resourceResponse.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                JSONObject response = new JSONObject();
                response.put(field, value);
                attributes.getResponse().write(response.toString());
            }
        });
        resourceResponse.setContentType("application/json");
    }

    void startFileCreation(ResourceResponse resourceResponse, Attributes attributes) throws Exception {
        String taskId = UUID.randomUUID().toString();
        ((WicketApplication) Application.get()).getTaskManager().execute(taskId,
                new FileTask(taskId, attributes.getParameters().get("file").toString()));
        sendSimpleResponse("taskId", taskId, resourceResponse);
    }
}
