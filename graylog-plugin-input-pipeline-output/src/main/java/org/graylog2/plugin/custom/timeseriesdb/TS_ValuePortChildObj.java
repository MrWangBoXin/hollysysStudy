package org.graylog2.plugin.custom.timeseriesdb;

/**
 * 性能数据子表（针对交换机）
 */
public class TS_ValuePortChildObj {
    /**
     * 构造
     *
     * @param p_Name   端口名称
     * @param p_In     端口进流量
     * @param p_Out    端口出流量
     * @param p_Status 端口状态（0 禁用， 1启用）
     */
    public TS_ValuePortChildObj(String p_Name, float p_In, float p_Out, int p_Status) {
        this.p_Name = p_Name;
        this.p_In = p_In;
        this.p_Out = p_Out;
        this.p_Status = p_Status;
    }

    public String p_Name = "";  // 端口名
    public float p_In = 0F;     // 断流进流量
    public float p_Out = 0F;    // 端口出流量
    public int p_Status = 0;    // 0 禁用，1 启用
}
