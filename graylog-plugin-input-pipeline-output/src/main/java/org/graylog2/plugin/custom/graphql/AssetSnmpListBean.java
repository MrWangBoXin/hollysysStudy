package org.graylog2.plugin.custom.graphql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetSnmpListBean {
    private DataBean<AssetSnmpBean> data;

    public AssetSnmpListBean(){

    }

    public AssetSnmpListBean(DataBean<AssetSnmpBean> data){
        this.data = data;
    }

    public DataBean<AssetSnmpBean> getData() {
        return data;
    }

    public void setData(DataBean<AssetSnmpBean> data) {
        this.data = data;
    }
}
