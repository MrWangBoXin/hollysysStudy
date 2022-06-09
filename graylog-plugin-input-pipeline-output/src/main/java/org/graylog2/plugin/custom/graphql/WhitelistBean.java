package org.graylog2.plugin.custom.graphql;


public class WhitelistBean {
    private DataBean<AssetBean> data;

    public WhitelistBean(){

    }

    public WhitelistBean(DataBean<AssetBean> data){
        this.data = data;
    }

    public DataBean<AssetBean> getData() {
        return data;
    }

    public void setData(DataBean<AssetBean> data) {
        this.data = data;
    }
}
