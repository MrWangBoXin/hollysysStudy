package org.graylog2.plugin.custom.pipeline;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.custom.graphql.BaselineCheckBean;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.util.*;

public class Basecheck extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(Basecheck.class);
    public static final String NAME = "Baselinecheck";
    private static final String PARAM = "option";
    private static ScriptEngineManager manager = new ScriptEngineManager();
    private static ScriptEngine engine = manager.getEngineByName("javascript");

    /** key ip，value 数据checksMap */
    private static Map<String,Map<Integer, List<BaselineCheckBean>>> checkListAllMap = new HashMap<>(10);
    /** key ip，value 时间戳*/
    private static Map<String,String> ipStampMap = new HashMap<>();
    /** key 组号，value 每组数据*/
    private static Map<Integer, List<BaselineCheckBean>> checksMap = new HashMap<>(6);


    public Basecheck(){
        LOG.info("Basecheck construct......");
        try {
            FileReader fr = new FileReader("./plugin/basecheck.js");
            engine.eval(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        //String optString = valueParam.required(functionArgs, evaluationContext);
        Message message = evaluationContext.currentMessage();
        String msg = (String) message.getField("message");
        String keypoint = "baselinecheck:";

        if(MyTools.isVersionBCLas()){
            if(msg.contains(keypoint)) {
                Integer begin = msg.indexOf(keypoint) + keypoint.length();
                Integer end = msg.indexOf(",source_ip:");
                if(begin > 0 && end > begin) {
                    String checkstr = msg.substring(begin, end);
                    LOG.info("---------------------baselinecheck:{}",checkstr);
                    try{
                        List<BaselineCheckBean> checklist = JSON.parseArray(checkstr,BaselineCheckBean.class);
                        if(checklist.size() > 0) {
                            String ip = (String) message.getField("source_ip");
                            CGraphql.BaselineCheck(ip, checklist,true);
                            //sendCheckResult(ip,msg,checklist);
                        }
                    }catch (Exception e){
                        LOG.warn("fastjson error is:{}",e.getMessage());
                    }
                }
            }
        }else {
            if(msg.contains(keypoint)) {
                Integer begin = msg.indexOf(keypoint) + keypoint.length();
                Integer end = msg.indexOf(";sourcelogdevicetype=");
                if(begin > 0 && end > begin) {
                    List<BaselineCheckBean> checklist = new ArrayList<BaselineCheckBean>();
                    String checkstr = msg.substring(begin, end);
                    String[] checkArray = checkstr.split(";");
                    for(String checkitem : checkArray) {
                        String[] items = checkitem.split(",");
                        if(items.length == 4) {
                            BaselineCheckBean checkbean = new BaselineCheckBean();
                            checkbean.setCheckType(Integer.parseInt(items[0]));
                            if (engine instanceof Invocable) {
                                Invocable in = (Invocable) engine;
                                String checkName = "unknow";
                                try {
                                    checkName = (String) in.invokeFunction("callback", Integer.parseInt(items[1]));
                                } catch (ScriptException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                                LOG.debug("checkName is:{}", checkName);
                                checkbean.setName(checkName);
                            }
                            checkbean.setScanResult(items[2]);
                            if(items[3].equals("0")) {
                                checkbean.setCheckResult("false");
                            }else{
                                checkbean.setCheckResult("true");
                            }
                            checkbean.setWeight(1);
                            checklist.add(checkbean);
                        }
                    }
                    if(checklist.size() > 0) {
                        String ip = (String) message.getField("source_ip");
                        sendCheckResult(ip,msg,checklist);
                    }
                }
            }
        }
        return 0L;
    }

    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
            .string(PARAM)
            .description("reserve.")
            .build();

    @Override
    public FunctionDescriptor<Long> descriptor() {
        return FunctionDescriptor.<Long>builder()
                .name(NAME)
                .description("baseline check.")
                .params(valueParam)
                .returnType(Long.class)
                .build();
    }

    synchronized static void sendCheckResult(String ip, String strlog,List<BaselineCheckBean> checklist){

        String flag = "batchStamp:";
        Integer begin = strlog.indexOf(flag);
        if(begin >= 0) {
            Integer end = strlog.indexOf(",", begin);
            String stamp = strlog.substring(begin+flag.length(),end);
            dealchecklist(ip,stamp,checklist);
        }
    }

    /**
     * 此版本中agent 上传的baselinecheck 数据分5次上传完整，共6组配置，将5次的数据合并为一个list，当6组数据完整收到时，一次性持久化
     * @param ip
     * @param stamp
     * @param checklist
     * @return: void
     * @author lishengcai
     * @date: 2022/5/21 13:20
     */
    private static void dealchecklist(String ip,String stamp,List<BaselineCheckBean> checklist){
        if (ipStampMap.get(ip) != null) {
            Date logDate = DateUtil.parse(stamp, "yyyy/MM/dd HH:mm:ss");
            Date currDate = DateUtil.parse(ipStampMap.get(ip), "yyyy/MM/dd HH:mm:ss");
            if (DateUtil.between(logDate, currDate, DateUnit.SECOND) < 5) {
                mergeChecklist(ip, checklist);
            } else {
                ipStampMap.put(ip, stamp);
                checkListAllMap.remove(ip);
                mergeChecklist(ip, checklist);
            }
        } else {
            ipStampMap.put(ip, stamp);
            checkListAllMap.remove(ip);
            mergeChecklist(ip, checklist);
        }

        if(checkListAllMap.get(ip).size()==6){
            List<BaselineCheckBean> checklistAll = new ArrayList<>();
            checkListAllMap.get(ip).forEach((key, value) -> {
                checklistAll.addAll(value);
            });
            LOG.debug("checklistAll ========= {}",JSON.toJSONString(checklistAll));
            CGraphql.BaselineCheck(ip, checklistAll,true);
            checkListAllMap.remove(ip);
            ipStampMap.remove(ip);
        }
    }

    /**
     *  合并6组数据
     * @param ip
     * @param checklist
     * @return: void
     * @author lishengcai
     * @date: 2022/5/21 13:22
     */
    private static void mergeChecklist(String ip, List<BaselineCheckBean> checklist) {
        //此ip所在的数据存在，且时间戳相等，是同一时刻的数据
        checklist.forEach(checkBean -> {
            checksMap = checkListAllMap.get(ip);
            if (checksMap == null) {
                checksMap = new HashMap<>();
            }
            if (checksMap.size() > 0
                    && checksMap.get(checkBean.getCheckType()) != null && checksMap.get(checkBean.getCheckType()).size() > 0) {
                //此ip对应分组的数据已存在
                checksMap.get(checkBean.getCheckType()).add(checkBean);
            } else if (checksMap.size() == 0) {
                //此ip 还没有数据
                List<BaselineCheckBean> checkBeans = new ArrayList<>();
                checkBeans.add(checkBean);
                checksMap.put(checkBean.getCheckType(), checkBeans);
                checkListAllMap.put(ip, checksMap);
            } else if (checksMap.get(checkBean.getCheckType()) == null || checksMap.get(checkBean.getCheckType()).size() == 0) {
                //此ip的某个分组没有数据
                List<BaselineCheckBean> checkBeans = new ArrayList<>();
                checkBeans.add(checkBean);
                checksMap.put(checkBean.getCheckType(), checkBeans);
                checkListAllMap.put(ip, checksMap);
            }
        });
    }

}
