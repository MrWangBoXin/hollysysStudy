package org.graylog2.plugin.custom.graphql;

import java.util.List;

public class SiddhiBean {
    private List<String> stream;

    public SiddhiBean(){

    }

    public SiddhiBean(List<String> stream) {
        this.stream = stream;
    }

    public List<String> getStream() {
        return stream;
    }

    public void setStream(List<String> stream) {
        this.stream = stream;
    }
}
