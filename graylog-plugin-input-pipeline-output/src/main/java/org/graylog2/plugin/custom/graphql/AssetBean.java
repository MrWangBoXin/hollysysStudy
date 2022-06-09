package org.graylog2.plugin.custom.graphql;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetBean {
    private String ip;

    private String assetType;

    @JsonProperty("AssetStat")
    private AssetStatBean AssetStat;

    @JsonProperty("assetExtend")
    private AssetExtendBean assetExtend;

    /***b/c/las新增**/
    @JsonProperty("assetNetwork")
    private List<AssetNetwork> assetNetwork;

    public AssetBean(){

    }

    public AssetBean(String ip, String assetType,AssetStatBean AssetStat,AssetExtendBean assetExtend){
        this.ip = ip;
        this.assetType = assetType;
        this.AssetStat = AssetStat;
        this.assetExtend = assetExtend;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public AssetStatBean getAssetStat() {
        return AssetStat;
    }

    public void setAssetStat(AssetStatBean assetStat) {
        AssetStat = assetStat;
    }

    public AssetExtendBean getAssetExtend() {
        return assetExtend;
    }

    public void setAssetExtend(AssetExtendBean assetExtend) {
        this.assetExtend = assetExtend;
    }

    public List<AssetNetwork> getAssetNetwork() {
        return assetNetwork;
    }

    public void setAssetNetwork(List<AssetNetwork> assetNetwork) {
        this.assetNetwork = assetNetwork;
    }
}
