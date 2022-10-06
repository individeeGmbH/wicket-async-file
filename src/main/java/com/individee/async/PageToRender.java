package com.individee.async;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.nio.charset.StandardCharsets;

public class PageToRender extends WebPage {

    public PageToRender(PageParameters parameters) {
        super(parameters);

        add(new Label("url", (IModel<String>) () -> getRequest().getClientUrl().toString(StandardCharsets.UTF_8)));
    }
}
