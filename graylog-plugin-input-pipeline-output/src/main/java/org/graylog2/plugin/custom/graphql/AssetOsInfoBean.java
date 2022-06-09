package org.graylog2.plugin.custom.graphql;

import java.util.List;

public class AssetOsInfoBean {
    private String hostName;
    private String os;
    private String releaseId;
    private String arch;
    private String major;
    private String minor;
    private String role;
    private List<String> ipMac;
    private List<String> hotFixd;

    public AssetOsInfoBean(){

    }

    public AssetOsInfoBean(String hostName, String os, String releaseId, String arch, String major, String minor, String role, List<String> ipMac, List<String> hotFixd) {
        this.hostName = hostName;
        this.os = os;
        this.releaseId = releaseId;
        this.arch = arch;
        this.major = major;
        this.minor = minor;
        this.role = role;
        this.ipMac = ipMac;
        this.hotFixd = hotFixd;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getIpMac() {
        return ipMac;
    }

    public void setIpMac(List<String> ipMac) {
        this.ipMac = ipMac;
    }

    public List<String> getHotFixd() {
        return hotFixd;
    }

    public void setHotFixd(List<String> hotFixd) {
        this.hotFixd = hotFixd;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }


}
