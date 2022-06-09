package org.graylog2.plugin.custom.timeseriesdb;

/**
 * 性能数据子表
 */
public class TS_ValueChildObj {
    /**
     * 构造
     *
     * @param ipAddress   ip地址
     * @param f_NetIn     网卡进
     * @param f_NetOut    网卡出
     * @param macAddress  mac地址
     * @param macName     mac名称
     * @param NetIsEnable 网卡是否活跃（1活跃，0否）
     */
    public TS_ValueChildObj(String ipAddress,
                            Object f_NetIn,
                            Object f_NetOut,
                            Object macAddress,
                            Object macName,
                            Object NetIsEnable) {
        IpAddress = ipAddress;
        this.f_NetIn = f_NetIn;
        this.f_NetOut = f_NetOut;
        MacAddress = macAddress;
        MacName = macName;
        this.NetIsEnable = NetIsEnable;
    }

    public TS_ValueChildObj() {
    }

    public String IpAddress = "";                                                       //IP地址
    public Object f_NetIn = 0;                                                          //网卡进
    public Object f_NetOut = 0;                                                         //网卡出
    public Object MacAddress = "";                                                      //网卡地址
    public Object MacName = "";                                                         //网卡名称
    public Object NetIsEnable = 0;                                                      //网卡是否活跃（1活跃，0否）
}

