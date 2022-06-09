package org.graylog2.plugin.custom.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetListBean {
    private DataBean<AssetBean> data;

    public AssetListBean(){

    }

    public AssetListBean(DataBean<AssetBean> data){
        this.data = data;
    }

    public DataBean<AssetBean> getData() {
        return data;
    }

    public void setData(DataBean<AssetBean> data) {
        this.data = data;
    }
}
