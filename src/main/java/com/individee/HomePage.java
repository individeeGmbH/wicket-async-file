package com.individee;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

import com.individee.async.FileTask;
import org.apache.wicket.Application;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        String taskId = UUID.randomUUID().toString();

        DownloadLink downloadHtmlLink = new DownloadLink("downloadHtml", new File(System.getProperty("java.io.tmpdir"), taskId + ".html"));
        downloadHtmlLink.setVisible(false);
        downloadHtmlLink.setOutputMarkupPlaceholderTag(true);
        add(downloadHtmlLink);

        final AjaxLink<Void> generateHtmlLink = new AjaxLink<>("generateHtml") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                String pageUrl = getRequest().getClientUrl().toString(Url.StringMode.FULL);
                ((WicketApplication) Application.get()).getTaskManager().execute(taskId, new FileTask(taskId, "render", pageUrl));
            }
        };
        add(generateHtmlLink.setOutputMarkupId(true));
        generateHtmlLink.add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if (downloadHtmlLink.getModelObject().exists()) {
                    downloadHtmlLink.setVisible(true);
                    target.add(downloadHtmlLink);
                    stop(target);
                }
            }
        });


        add(new AjaxLink<>("downloadDelayed") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                download("delayed.txt", ajaxRequestTarget, getMarkupId());
            }
        }.setOutputMarkupId(true));

        add(new AjaxLink<>("downloadNonDelayed") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                download("non-delayed.txt", ajaxRequestTarget, getMarkupId());
            }
        }.setOutputMarkupId(true));

        // TODO Add your page's components here

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(WicketApplication.class, "js/download.js")));
    }

    private void download(String file, AjaxRequestTarget ajaxRequestTarget, String btnMarkupId) {
        PageParameters parameters = new PageParameters();
        parameters.add("file", file);
        parameters.add("type", "_TYPE_");
        parameters.add("no-cache", System.currentTimeMillis());

        RequestCycle reqCycle = RequestCycle.get();
        String url = reqCycle.getUrlRenderer().renderFullUrl(Url.parse(reqCycle.urlFor(new SharedResourceReference("asyncFile"), parameters).toString()));
        System.err.println("--- url: " + url);
        ajaxRequestTarget.appendJavaScript("individee.initiateDownload('" + url + "', '" + btnMarkupId + "', " +
                30000 + ", 'This is taking long', 'This timed out');");
    }
}
