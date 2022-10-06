package com.individee.behavior;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class DownloadResource extends AbstractResource {

    public DownloadResource() {
        super();
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse resourceResponse = new ResourceResponse();
        resourceResponse.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                try {
                    File file = new File("/Users/hannao/Downloads/" + attributes.getParameters().get("tid").toString());
                    FileResourceStream resource = new FileResourceStream(file);
                    ResourceStreamResource rsr = new ResourceStreamResource(resource);
                    rsr.setFileName(attributes.getParameters().get("file").toString());
                    rsr.setContentDisposition(ContentDisposition.ATTACHMENT);
                    Attributes a = new Attributes(attributes.getRequest(),
                            attributes.getResponse());
                    rsr.respond(a);
                    file.delete();
                } catch (Exception ex) {
                    LoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
                }
            }
        });
        return resourceResponse;
    }
}
