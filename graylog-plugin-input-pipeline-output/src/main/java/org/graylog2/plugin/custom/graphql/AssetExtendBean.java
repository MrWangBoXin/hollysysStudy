package org.graylog2.plugin.custom.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssetExtendBean {
    @JsonProperty("id")
    private String id;

    @JsonProperty("assetRole")
    private Integer assetRole;

    public AssetExtendBean(){

    }

    public AssetExtendBean(String id, Integer assetRole){
        this.id = id;
        this.assetRole = assetRole;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAssetRole() {
        return assetRole;
    }

    public void setAssetRole(Integer assetRole) {
        this.assetRole = assetRole;
    }
}
