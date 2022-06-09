package org.graylog2.plugin.custom.graphql;

import java.util.List;

//tcpudpPorts:{"tcpPorts":["135 svchost.exe","445 System.exe","139 System.exe"],"udpPorts":["123 svchost.exe","1900 svchost.exe","137 System.exe","138 System.exe"]}
public class AssetRiskPortBean {
    private List<String> tcpPorts;
    private List<String> udpPorts;

    public AssetRiskPortBean(){

    }

    public AssetRiskPortBean(List<String> tcpPorts, List<String> udpPorts) {
        this.tcpPorts = tcpPorts;
        this.udpPorts = udpPorts;
    }

    public List<String> getTcpPorts() {
        return tcpPorts;
    }

    public void setTcpPorts(List<String> tcpPorts) {
        this.tcpPorts = tcpPorts;
    }

    public List<String> getUdpPorts() {
        return udpPorts;
    }

    public void setUdpPorts(List<String> udpPorts) {
        this.udpPorts = udpPorts;
    }
}
