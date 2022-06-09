package org.graylog2.plugin.custom.timeseriesdb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * 性能数据主表
 */
public class TS_ValueObj implements Serializable {
    /**
     * 构造
     *
     * @param i_TimeStamp           时间戳
     * @param f_CPU_Percent         cpu百分比
     * @param f_Mem_Percent         内存百分比
     * @param f_Mem_Surplus         内存剩余量
     * @param f_Disk_Surplus        磁盘剩余量
     * @param f_Disk_Percent        磁盘用量百分比
     * @param ts_valueChildObjs     子表列表集合
     * @param isHost                是否主机（内容是127.0.0.1这是服务器主机，0 交换机，其它 服务器）
     * @param ts_valuePortChildObjs 端口列表集合
     */
    public TS_ValueObj(Date i_TimeStamp,
                       Object f_CPU_Percent,
                       Object f_Mem_Percent,
                       Object f_Mem_Surplus,
                       Object f_Disk_Surplus,
                       Object f_Disk_Percent,
                       ArrayList<TS_ValueChildObj> ts_valueChildObjs,
                       Object isHost,
                       ArrayList<TS_ValuePortChildObj> ts_valuePortChildObjs) {
        this.i_TimeStamp = i_TimeStamp;
        this.f_CPU_Percent = f_CPU_Percent;
        this.f_Mem_Percent = f_Mem_Percent;
        this.f_Mem_Surplus = f_Mem_Surplus;
        this.f_Disk_Surplus = f_Disk_Surplus;
        this.f_Disk_Percent = f_Disk_Percent;
        this.ts_valueChildObjs = ts_valueChildObjs;
        IsHost = isHost;
        this.ts_valuePortChildObjs = ts_valuePortChildObjs;
    }

    public Date i_TimeStamp;                                                            //时间戳
    public Object f_CPU_Percent = 0;                                                    //cpu百分比
    public Object f_Mem_Percent = 0;                                                    //内存百分比
    public Object f_Mem_Surplus = 0;                                                    //内存剩余量
    public Object f_Disk_Surplus = 0;                                                   //磁盘剩余量
    public Object f_Disk_Percent = 0;                                                   //磁盘用量百分比
    public ArrayList<TS_ValueChildObj> ts_valueChildObjs = new ArrayList<>();           //子表列表集合
    public ArrayList<TS_ValuePortChildObj> ts_valuePortChildObjs = new ArrayList<>();   //交换机端口
    public Object IsHost = "";                                                          //是否主机（内容是127.0.0.1这是服务器主机，0 交换机，其它 服务器）
}