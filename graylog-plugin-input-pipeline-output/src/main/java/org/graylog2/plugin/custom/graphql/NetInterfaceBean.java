package org.graylog2.plugin.custom.graphql;

public class NetInterfaceBean implements Cloneable{
    private String oid;
    private Long inFlow;
    private Long outFlow;
    private Integer stat;

    public NetInterfaceBean(){

    }

    public NetInterfaceBean(String oid, Long inFlow, Long outFlow, Integer stat) {
        this.oid = oid;
        this.inFlow = inFlow;
        this.outFlow = outFlow;
        this.stat = stat;
    }

    public NetInterfaceBean(NetInterfaceBean bean){
        this.oid = bean.oid;
        this.inFlow = bean.inFlow;
        this.outFlow = bean.outFlow;
        this.stat = bean.stat;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Long getInFlow() {
        return inFlow;
    }

    public void setInFlow(Long inFlow) {
        this.inFlow = inFlow;
    }

    public Long getOutFlow() {
        return outFlow;
    }

    public void setOutFlow(Long outFlow) {
        this.outFlow = outFlow;
    }

    public Integer getStat() {
        return stat;
    }

    public void setStat(Integer stat) {
        this.stat = stat;
    }
}
