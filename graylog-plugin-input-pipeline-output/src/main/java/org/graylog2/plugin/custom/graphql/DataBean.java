package org.graylog2.plugin.custom.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataBean<T>{
    private List<T> assets;

    public DataBean(){

    }

    public DataBean(List<T> assets){
        this.assets = assets;
    }

    public List<T> getAssets() {
        return assets;
    }

    public void setAssets(List<T> assets) {
        this.assets = assets;
    }

}
