package org.graylog2.plugin.custom.graphql;

import com.alibaba.fastjson.annotation.JSONField;

public class BaselineCheckBean {
    @JSONField(name = "categoryId")
    private Integer checkType;
    @JSONField(serialize = false)
    private String checkTypeName;
    @JSONField(serialize = false)
    private String name;
    @JSONField(name="id")
    private String configCheckId;
    @JSONField(name="scanResult")
    private String scanResult;
    @JSONField(name="checkResult")
    private String checkResult;
    @JSONField(serialize = false)
    private Integer weight;

    public BaselineCheckBean() {
    }
    /***a/isa旧版构造方法**/
    public BaselineCheckBean(Integer checkType, String name, String scanResult, String checkResult, Integer weight) {
        this.checkType = checkType;
        this.name = name;
        this.scanResult = scanResult;
        this.checkResult = checkResult;
        this.weight = weight;
    }
    /***b/c/las版构造方法**/
    public BaselineCheckBean(Integer checkType, String checkTypeName,String name,String configCheckId, String scanResult, String checkResult, Integer weight) {
        this.checkType = checkType;
        this.checkTypeName = checkTypeName;
        this.name = name;
        this.configCheckId = configCheckId;
        this.scanResult = scanResult;
        this.checkResult = checkResult;
        this.weight = weight;
    }

    public Integer getCheckType() {
        return checkType;
    }

    public void setCheckType(Integer checkType) {
        this.checkType = checkType;
    }

    public String getCheckTypeName() {
        return checkTypeName;
    }

    public void setCheckTypeName(String checkTypeName) {
        this.checkTypeName = checkTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigCheckId() {
        return configCheckId;
    }

    public void setConfigCheckId(String configCheckId) {
        this.configCheckId = configCheckId;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
