package com.srcsolution.things.onem2m_client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ResourceBuilder <R extends Resource> {

    protected List<String> labels;

    public R create() {
        return create(null);
    }

    public abstract R create(String name);

    public ResourceBuilder addLabels(String... labels) {
        if (labels != null) {
            if (this.labels == null) {
                this.labels = new ArrayList<>();
            }
            Collections.addAll(this.labels, labels);
        }
        return this;
    }

    public ResourceBuilder setLabels(List<String> labels) {
        this.labels = labels;
        return this;
    }

}
