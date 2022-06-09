package org.graylog2.plugin.custom.graphql;

public class AssetStatBean {
    private String id;
    private String updatedAt;
    private Boolean online;

    public AssetStatBean(){

    }

    public AssetStatBean(String id,String updatedAt,Boolean online) {
        this.id = id;
        this.updatedAt = updatedAt;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
}
