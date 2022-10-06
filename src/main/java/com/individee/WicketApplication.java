package com.individee;

import com.individee.async.DownloadFileResource;
import com.individee.async.PageToRender;
import com.individee.async.TaskService;
import com.individee.behavior.DownloadResource;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.SharedResourceReference;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 *
 * @see com.individee.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
    private final TaskService taskManager = new TaskService();
    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();

        // needed for the styling used by the quickstart
        getCspSettings().blocking()
                .add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.SELF)
                .add(CSPDirective.STYLE_SRC, "https://fonts.googleapis.com/css")
                .add(CSPDirective.FONT_SRC, "https://fonts.gstatic.com");

        // add your configuration here
        getSharedResources().add("asyncFile", new DownloadFileResource());
        mountResource("/download/${type}/${file}", new SharedResourceReference("asyncFile"));

        getSharedResources().add("download", new DownloadResource());
        mountResource("/get/${tid}/${file}", new SharedResourceReference("download"));

        mountPage("test", PageToRender.class);

    }

    public TaskService getTaskManager() {
        return taskManager;
    }
}
