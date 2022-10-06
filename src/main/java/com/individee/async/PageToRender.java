package com.individee.async;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class PageToRender extends WebPage {

    public PageToRender(String pageUrl) {

        add(new Label("url", pageUrl));
    }
}
