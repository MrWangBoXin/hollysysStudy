package org.graylog2.plugin.custom.graphql;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
@JsonIgnoreProperties(ignoreUnknown = true)
public class DevStatusBean {
    private String ip;
    private String online;
    private String cpuUnit;
    private Integer cpuUsageRate;
    private String ramUnit;
    private Integer ramUsageRate;
    private Integer ramSize;
    private Integer ramUsage;
    private Integer ramIdle;
    private String diskUnit;
    private Integer diskUsageRate;
    private Integer diskSize;
    private Integer diskUsage;
    private Integer diskIdle;
    private String netUnit;
    private Integer netInput;
    private Integer netOutput;
    private String netPorts;
    @JsonProperty("inPorts")
    private List<Long> inPorts;
    @JsonProperty("outPorts")
    private List<Long> outPorts;

    ArrayList<NetInterfaceBean> interfaceList;
    @JsonProperty("max")
    private Long max;//Counter,Gauge

    public DevStatusBean(){

    }

    public DevStatusBean(String ip, String online, String cpuUnit, Integer cpuUsageRate, String ramUnit, Integer ramUsageRate, Integer ramSize, Integer ramUsage, Integer ramIdle, String diskUnit, Integer diskUsageRate, Integer diskSize, Integer diskUsage, Integer diskIdle, String netUnit, Integer netInput, Integer netOutput, String netPorts) {
        this.ip = ip;
        this.online = online;
        this.cpuUnit = cpuUnit;
        this.cpuUsageRate = cpuUsageRate;
        this.ramUnit = ramUnit;
        this.ramUsageRate = ramUsageRate;
        this.ramSize = ramSize;
        this.ramUsage = ramUsage;
        this.ramIdle = ramIdle;
        this.diskUnit = diskUnit;
        this.diskUsageRate = diskUsageRate;
        this.diskSize = diskSize;
        this.diskUsage = diskUsage;
        this.diskIdle = diskIdle;
        this.netUnit = netUnit;
        this.netInput = netInput;
        this.netOutput = netOutput;
        this.netPorts = netPorts;
    }

    public DevStatusBean(String ip, Integer cpuUsageRate, Integer ramUsageRate, Integer diskIdle){
        this.ip = ip;
        this.online = "true";
        this.cpuUnit = "%";
        this.ramUnit = "MB";
        this.diskUnit = "GB";
        this.netUnit = "kbps";
        this.cpuUsageRate = cpuUsageRate;
        this.ramIdle = -1;
        this.ramSize = -1;
        this.ramUsage = -1;
        this.ramUsageRate = ramUsageRate;
        this.diskIdle = diskIdle;
        this.diskSize = -1;
        this.diskUsage = -1;
        this.diskUsageRate = -1;
        this.netInput = -1;
        this.netOutput = -1;
        this.netPorts = "";
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public ArrayList<NetInterfaceBean> getInterfaceList() {
        return interfaceList;
    }

    public void setInterfaceList(ArrayList<NetInterfaceBean> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getCpuUnit() {
        return cpuUnit;
    }

    public void setCpuUnit(String cpuUnit) {
        this.cpuUnit = cpuUnit;
    }

    public Integer getCpuUsageRate() {
        return cpuUsageRate;
    }

    public void setCpuUsageRate(Integer cpuUsageRate) {
        this.cpuUsageRate = cpuUsageRate;
    }

    public String getRamUnit() {
        return ramUnit;
    }

    public void setRamUnit(String ramUnit) {
        this.ramUnit = ramUnit;
    }

    public Integer getRamUsageRate() {
        return ramUsageRate;
    }

    public void setRamUsageRate(Integer ramUsageRate) {
        this.ramUsageRate = ramUsageRate;
    }

    public Integer getRamSize() {
        return ramSize;
    }

    public void setRamSize(Integer ramSize) {
        this.ramSize = ramSize;
    }

    public Integer getRamUsage() {
        return this.ramUsage;
    }

    public void setRamUsage(Integer getRamUsage) {
        this.ramUsage = getRamUsage;
    }

    public Integer getRamIdle() {
        return ramIdle;
    }

    public void setRamIdle(Integer ramIdle) {
        this.ramIdle = ramIdle;
    }

    public String getDiskUnit() {
        return diskUnit;
    }

    public void setDiskUnit(String diskUnit) {
        this.diskUnit = diskUnit;
    }

    public Integer getDiskUsageRate() {
        return diskUsageRate;
    }

    public void setDiskUsageRate(Integer diskUsageRate) {
        this.diskUsageRate = diskUsageRate;
    }

    public Integer getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(Integer diskSize) {
        this.diskSize = diskSize;
    }

    public Integer getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Integer diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Integer getDiskIdle() {
        return diskIdle;
    }

    public void setDiskIdle(Integer diskIdle) {
        this.diskIdle = diskIdle;
    }

    public String getNetUnit() {
        return netUnit;
    }

    public void setNetUnit(String netUnit) {
        this.netUnit = netUnit;
    }

    public Integer getNetInput() {
        return netInput;
    }

    public void setNetInput(Integer netInput) {
        this.netInput = netInput;
    }

    public Integer getNetOutput() {
        return netOutput;
    }

    public void setNetOutput(Integer netOutput) {
        this.netOutput = netOutput;
    }

    public String getNetPorts() {
        return netPorts;
    }

    public void setNetPorts(String netPorts) {
        this.netPorts = netPorts;
    }
    public List<Long> getInPorts() {
        return inPorts;
    }

    public void setInPorts(List<Long> in) {
        this.inPorts = in;
    }

    public List<Long> getOutPorts() {
        return outPorts;
    }

    public void setOutPorts(List<Long> out) {
        this.outPorts = out;
    }

}
