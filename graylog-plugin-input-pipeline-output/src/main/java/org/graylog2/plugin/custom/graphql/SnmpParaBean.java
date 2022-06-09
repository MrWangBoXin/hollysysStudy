package org.graylog2.plugin.custom.graphql;

import com.alibaba.fastjson.annotation.JSONField;

public class SnmpParaBean {
    String cpuUsageRateOid;
    String cpuUnit;
    String ramUsageRateOid;
    String ramUnit;
    String ramSizeOid;
    String ramUsageOid;
    String ramIdleOid;
    String diskUnit;
    String diskUsageRateOid;
    String diskSizeOid;
    String diskUsageOid;
    String diskIdleOid;
    String netUnit;
    String netInputOid;
    String netOutputOid;
    @JSONField(name="statusOid")
    String statusOid;
    String brand;
    String modelName;

    public SnmpParaBean(){

    }
    public SnmpParaBean(String cpuUsageRateOid, String cpuUnit, String ramUsageRateOid, String ramUnit, String ramSizeOid, String ramUsageOid, String ramIdleOid, String diskUnit, String diskUsageRateOid, String diskSizeOid, String diskUsageOid, String diskIdleOid, String netUnit, String netInputOid, String netOutputOid, String brand, String modelName) {
        this.cpuUsageRateOid = cpuUsageRateOid;
        this.cpuUnit = cpuUnit;
        this.ramUsageRateOid = ramUsageRateOid;
        this.ramUnit = ramUnit;
        this.ramSizeOid = ramSizeOid;
        this.ramUsageOid = ramUsageOid;
        this.ramIdleOid = ramIdleOid;
        this.diskUnit = diskUnit;
        this.diskUsageRateOid = diskUsageRateOid;
        this.diskSizeOid = diskSizeOid;
        this.diskUsageOid = diskUsageOid;
        this.diskIdleOid = diskIdleOid;
        this.netUnit = netUnit;
        this.netInputOid = netInputOid;
        this.netOutputOid = netOutputOid;
        this.brand = brand;
        this.modelName = modelName;
    }

    public SnmpParaBean(String cpuUsageRateOid, String cpuUnit, String ramUsageRateOid, String ramUnit, String ramSizeOid, String ramUsageOid, String ramIdleOid, String diskUnit, String diskUsageRateOid, String diskSizeOid, String diskUsageOid, String diskIdleOid, String netUnit, String netInputOid, String netOutputOid, String netStatOid, String brand, String modelName) {
        this.cpuUsageRateOid = cpuUsageRateOid;
        this.cpuUnit = cpuUnit;
        this.ramUsageRateOid = ramUsageRateOid;
        this.ramUnit = ramUnit;
        this.ramSizeOid = ramSizeOid;
        this.ramUsageOid = ramUsageOid;
        this.ramIdleOid = ramIdleOid;
        this.diskUnit = diskUnit;
        this.diskUsageRateOid = diskUsageRateOid;
        this.diskSizeOid = diskSizeOid;
        this.diskUsageOid = diskUsageOid;
        this.diskIdleOid = diskIdleOid;
        this.netUnit = netUnit;
        this.netInputOid = netInputOid;
        this.netOutputOid = netOutputOid;
        this.statusOid = netStatOid;
        this.brand = brand;
        this.modelName = modelName;
    }

    public String getStatusOid() {
        return statusOid;
    }

    public void setStatusOid(String statusOid) {
        this.statusOid = statusOid;
    }

    public String getCpuUsageRateOid() {
        return cpuUsageRateOid;
    }

    public void setCpuUsageRateOid(String cpuUsageRateOid) {
        this.cpuUsageRateOid = cpuUsageRateOid;
    }

    public String getCpuUnit() {
        return cpuUnit;
    }

    public void setCpuUnit(String cpuUnit) {
        this.cpuUnit = cpuUnit;
    }

    public String getRamUsageRateOid() {
        return ramUsageRateOid;
    }

    public void setRamUsageRateOid(String ramUsageRateOid) {
        this.ramUsageRateOid = ramUsageRateOid;
    }

    public String getRamUnit() {
        return ramUnit;
    }

    public void setRamUnit(String ramUnit) {
        this.ramUnit = ramUnit;
    }

    public String getRamSizeOid() {
        return ramSizeOid;
    }

    public void setRamSizeOid(String ramSizeOid) {
        this.ramSizeOid = ramSizeOid;
    }

    public String getRamUsageOid() {
        return ramUsageOid;
    }

    public void setRamUsageOid(String ramUsageOid) {
        this.ramUsageOid = ramUsageOid;
    }

    public String getRamIdleOid() {
        return ramIdleOid;
    }

    public void setRamIdleOid(String ramIdleOid) {
        this.ramIdleOid = ramIdleOid;
    }

    public String getDiskUnit() {
        return diskUnit;
    }

    public void setDiskUnit(String diskUnit) {
        this.diskUnit = diskUnit;
    }

    public String getDiskUsageRateOid() {
        return diskUsageRateOid;
    }

    public void setDiskUsageRateOid(String diskUsageRateOid) {
        this.diskUsageRateOid = diskUsageRateOid;
    }

    public String getDiskSizeOid() {
        return diskSizeOid;
    }

    public void setDiskSizeOid(String diskSizeOid) {
        this.diskSizeOid = diskSizeOid;
    }

    public String getDiskUsageOid() {
        return diskUsageOid;
    }

    public void setDiskUsageOid(String diskUsageOid) {
        this.diskUsageOid = diskUsageOid;
    }

    public String getDiskIdleOid() {
        return diskIdleOid;
    }

    public void setDiskIdleOid(String diskIdleOid) {
        this.diskIdleOid = diskIdleOid;
    }

    public String getNetUnit() {
        return netUnit;
    }

    public void setNetUnit(String netUnit) {
        this.netUnit = netUnit;
    }

    public String getNetInputOid() {
        return netInputOid;
    }

    public void setNetInputOid(String netInputOid) {
        this.netInputOid = netInputOid;
    }

    public String getNetOutputOid() {
        return netOutputOid;
    }

    public void setNetOutputOid(String netOutputOid) {
        this.netOutputOid = netOutputOid;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
