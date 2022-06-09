package org.graylog2.plugin.custom.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @description: 测试提交到分支
 * @author lishengcai
 * @date 2022/5/10 9:21
 * @version 1.0
 */
public class AssetSnmpBean {
    private String ip;
    private String snmpVersion;
    /* 社团 */
    private String snmpCommunity;

    @JsonProperty("snmpPara")
    private SnmpParaBean snmpPara;

    public AssetSnmpBean(){

    }

    public AssetSnmpBean(String ip, SnmpParaBean snmpPara){
        this.ip = ip;
        this.snmpPara = snmpPara;
    }

    public String getSnmpVersion() {
        return snmpVersion;
    }

    public void setSnmpVersion(String snmpVersion) {
        this.snmpVersion = snmpVersion;
    }

    public String getSnmpCommunity() {
        return snmpCommunity;
    }

    public void setSnmpCommunity(String snmpCommunity) {
        this.snmpCommunity = snmpCommunity;
    }

    public SnmpParaBean getSnmpPara() {
        return snmpPara;
    }

    public void setSnmpPara(SnmpParaBean snmpPara) {
        this.snmpPara = snmpPara;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
