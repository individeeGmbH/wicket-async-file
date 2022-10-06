package com.individee.async;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class LoadableModel extends LoadableDetachableModel<String> {

    private final IModel<String> textModel;

    public LoadableModel(IModel<String> textModel) {
        this.textModel = textModel;
    }

    @Override
    protected String load() {
        return textModel.getObject();
    }
}
