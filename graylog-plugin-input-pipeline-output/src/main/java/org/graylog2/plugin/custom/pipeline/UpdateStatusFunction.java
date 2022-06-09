package org.graylog2.plugin.custom.pipeline;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;

import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.custom.graphql.DevStatusBean;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.custom.input.SnmpGetTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.*;

import static org.graylog2.plugin.custom.output.RelayOutput.GetAddressList;


public class UpdateStatusFunction extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateStatusFunction.class);
    public static final String NAME = "UpdateDevStatus";
    private static final String PARAM = "option";
    private static ScriptEngineManager manager = null;
    private static ScriptEngine engine;

    public UpdateStatusFunction() {
        LOG.info("GraphqlUpdateStatus construct...");
        if (!MyTools.isVersionBCLas()) {
            manager = new ScriptEngineManager();
            engine = manager.getEngineByName("javascript");
            //engine.eval("function add(a,b){return a+b;}");
            try {
                FileReader fr = new FileReader("./plugin/updatestatu.js");
                engine.eval(fr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Long evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        Message message = evaluationContext.currentMessage();
        String msg = (String) message.getField("message");

        if (MyTools.isVersionBCLas()) {
            try {
                String ip = (String) message.getField("source_ip");
                String snmpKey = "snmp-status:";
                if (msg.contains(snmpKey)) {
                    LOG.info("recive one snmp status log from:{}", ip);
                    Integer begin = msg.indexOf(snmpKey) + snmpKey.length();
                    Integer end = msg.indexOf(",source_ip");
                    if (begin > 0 && end > begin) {
                        String strStatus = msg.substring(begin, end);
                        LOG.info("recive one snmp status:{}", strStatus);
                        DevStatusBean devStatus = JSON.parseObject(strStatus, DevStatusBean.class);
                        MyTools.setValue(msg, ip, "0", devStatus);
                        devStatus.setIp(ip);
                        devStatus.setOnline("true");
                        SnmpGetTask.updateStat(devStatus);
                    }
                } else {
                    Integer cpu_usage = -1;
                    Object cpu = message.getField("cpu_usage");
                    if (cpu != null) {
                        if (cpu instanceof Integer) {
                            cpu_usage = (Integer) cpu;
                        }
                        if (cpu instanceof Float) {
                            cpu_usage = Math.round((Float) cpu);
                        }
                    }
                    Integer mem_usage = -1;
                    Object mem = message.getField("mem_usage");
                    if (mem != null) {
                        if (mem instanceof Integer) {
                            mem_usage = (Integer) mem;
                        }
                        if (mem instanceof Float) {
                            mem_usage = Math.round((Float) mem);
                        }
                    }
                    Integer disk_idle = -1;
                    Object disk = message.getField("disk_idle");
                    if (disk != null) {
                        if (disk instanceof Integer) {
                            disk_idle = (Integer) disk;
                        }
                        if (disk instanceof Float) {
                            disk_idle = Math.round((Float) disk);
                        }
                    }

                    LOG.info("update dev status:{},{},{},{}", ip, cpu_usage, mem_usage, disk_idle);
                    DevStatusBean devStatus = new DevStatusBean(ip, cpu_usage, mem_usage, disk_idle);
                    String hostFlag = "";
                    //是否为安管平台宿主机
                    if (msg.contains("platform-self[")) {
                        hostFlag = "127.0.0.1";
                        //去掉self关键字，日志转发后不会再判断为平台本身
                        msg = msg.replace("platform-self[", "platform[");
                    }
                    MyTools.setValue(msg, ip, hostFlag, devStatus);
                    CGraphql.GraphqlUpdateDevStatus(devStatus);
                }
                //转发性能日志
                List<InetSocketAddress> addrList = GetAddressList();
                if (!addrList.isEmpty()) {
                    MyTools.relayStatus(msg, addrList);
                }
                //过滤性能日志
                message.setFilterOut(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                String ip = (String)message.getField("source_ip");
                if(!msg.contains("snmp-status:")) {
                    Integer cpu_usage = (Integer)message.getField("cpu_usage");
                    Integer mem_usage = (Integer)message.getField("mem_usage");
                    Integer disk_idle = (Integer)message.getField("disk_idle");
                    DevStatusBean devStatus = new DevStatusBean(ip, cpu_usage, mem_usage, disk_idle);
                    LOG.info("update dev status:{},{},{},{}",ip,cpu_usage,mem_usage,disk_idle);
                    CGraphql.GraphqlUpdateDevStatus(devStatus);
                }else{
                    //engine.eval(optString);
                    if (engine instanceof Invocable) {
                        Invocable in = (Invocable) engine;
                        String newmsg = (String) in.invokeFunction("callback",msg);
                        LOG.info("newmsg is:{}",newmsg);
                        if(newmsg != null && newmsg != "" && newmsg.length() > 0) {
                            ObjectMapper mapper = new ObjectMapper();
                            DevStatusBean devStatus = mapper.readValue(newmsg, DevStatusBean.class);
                            devStatus.setIp(ip);
                            devStatus.setOnline("true");
                            CGraphql.GraphqlUpdateDevStatus(devStatus);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0L;
    }

    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
            .string(PARAM)
            .description("JSON String containing cpuUsageRate,ramUsageRate,diskIdle.")
            .build();

    @Override
    public FunctionDescriptor<Long> descriptor() {
        return FunctionDescriptor.<Long>builder()
                .name(NAME)
                .description("Update Dev Status online.")
                .params(valueParam)
                .returnType(Long.class)
                .build();
    }
}
