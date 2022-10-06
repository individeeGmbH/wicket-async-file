package com.individee;

import com.individee.async.FileTask;
import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;

import java.util.UUID;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        add(new AjaxLink<>("generateHtml") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                String taskId = UUID.randomUUID().toString();
                ((WicketApplication) Application.get()).getTaskManager().execute(taskId, new FileTask(taskId, "rendered.html"));
            }
        }.setOutputMarkupId(true));

        add(new AjaxLink<>("downloadHtml") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                String taskId = UUID.randomUUID().toString();
                ((WicketApplication) Application.get()).getTaskManager().execute(taskId, new FileTask(taskId, "rendered.html"));
            }
        }.setOutputMarkupId(true));

        add(new AjaxLink<>("downloadDelayed") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                download("delayed.txt", ajaxRequestTarget);
            }
        }.setOutputMarkupId(true));

        add(new AjaxLink<>("downloadNonDelayed") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                download("non-delayed.txt", ajaxRequestTarget);
            }
        }.setOutputMarkupId(true));

        // TODO Add your page's components here

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WicketApplication.class, "js/download.js")));
    }

    private void download(String file, AjaxRequestTarget ajaxRequestTarget) {
        PageParameters parameters = new PageParameters();
        parameters.add("file", file);
        parameters.add("type", "_TYPE_");
        parameters.add("no-cache", System.currentTimeMillis());

        RequestCycle reqCycle = RequestCycle.get();
        String url = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(reqCycle.urlFor(new SharedResourceReference("asyncFile"), parameters).toString()));
        ajaxRequestTarget.appendJavaScript("individee.initiateDownload('" + url + "', '" + getMarkupId() + "', " +
                30000 + ", 'This is taking long', 'This timed out');");
    }
}
