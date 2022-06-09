package org.graylog2.plugin.custom.graphql;

/**
 * @description: 资产网络连接信息
 * @author lishengcai
 * @date 2022/5/18 10:16
 * @version 1.0
 */
public class AssetNetwork {
    private String id;

    private String ipv4;

    public AssetNetwork() {
    }

    public AssetNetwork(String id, String ipv4) {
        this.id = id;
        this.ipv4 = ipv4;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }
}
