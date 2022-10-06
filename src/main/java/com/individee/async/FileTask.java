/*
 * Copyright (C) individee GmbH - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written 9 2020
 */

package com.individee.async;

import com.individee.WicketApplication;
import org.apache.commons.io.FileUtils;
import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * A {@link Callable} that is the basis for all tasks.
 *
 */
public class FileTask implements Callable<File> {

    private final String taskId;
    private final String file;
    private final WicketApplication application;
    private final WebSession session;
    private final RequestCycle requestCycle;


    /**
     * Instantiates a new Abstract task.
     *
     * @param taskId   the task id
     * @param file
     */
    public FileTask(String taskId, String file) {
        super();
        this.taskId = taskId != null ? taskId : UUID.randomUUID().toString();
        this.file = file;
        application = Application.exists() ? (WicketApplication) Application.get() : null;
        session = WebSession.exists() ? WebSession.get() : null;
        requestCycle = RequestCycle.get();
    }

    @Override
    public File call() {
        try {
            ThreadContext.setApplication(application);
            ThreadContext.setSession(session);
            ThreadContext.setRequestCycle(requestCycle);
            return executeTask();
        } catch (Exception ex) {
            LoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Execute task t.
     *
     * @return the t
     * @throws Exception the exception
     */
    protected File executeTask() throws Exception{
        File tmpFile = File.createTempFile("async", ".tmp");
        if(file.equalsIgnoreCase("rendered.html")){
            FileUtils.write(tmpFile, ComponentRenderer.renderPage(new PageProvider(PageToRender.class, new PageParameters())).toString(), StandardCharsets.UTF_8);
        }else if(file.equalsIgnoreCase("delayed.txt")
        || file.equalsIgnoreCase("non-delayed.txt")){
            LoadableModel model = new LoadableModel(Model.of("Lorem ipsum"));
            if(file.equalsIgnoreCase("delayed.txt")){
                Thread.sleep(10000);
            }
            FileUtils.write(tmpFile, model.getObject(), StandardCharsets.UTF_8);
        }
        LoggerFactory.getLogger(getClass()).info("Done with file creation");
        return tmpFile;
    }

    /**
     * Gets task id.
     *
     * @return the task id
     */
    public String getTaskId() {
        return taskId;
    }
}
