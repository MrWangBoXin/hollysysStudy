package org.graylog2.plugin.custom.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SiddhiFunction  extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(SiddhiFunction.class);
    public static final String NAME = "SiddhiApi";
    private static final String PARAM = "option";
    private static ScriptEngineManager manager = null;
    private static ScriptEngine engine;
    private Map<String,Integer> ipRole = new HashMap<String, Integer>();

    public SiddhiFunction(){
        LOG.info("SiddhiFunction construct...");
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("javascript");
        try {
            FileReader fr = new FileReader("./plugin/siddhi.js");
            engine.eval(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        Message message = evaluationContext.currentMessage();
        String siddhiParams = getSiddhiParams(message);
        String event_source = (String)message.getField("sd_event_source");
        LOG.info("siddhi send params is:{},{}",event_source,siddhiParams);
        if(event_source == null){
            return 0L;
        }

        try {
            if (engine instanceof Invocable) {
                Invocable in = (Invocable) engine;
                String newmsg = (String) in.invokeFunction("callback", siddhiParams);
                LOG.info("newmsg is:{}", newmsg);
                if (newmsg != null && newmsg != "" && newmsg.length() > 0) {
                    int pos = newmsg.indexOf(",{");
                    if (pos > 0) {
                        String siddhiStream = newmsg.substring(0, pos);
                        siddhiParams = newmsg.substring(pos + 1);
                        String respJson = CGraphql.SendSiddhiParams(siddhiStream, siddhiParams);
                        if (respJson != null) {
                            if (respJson.contains("errors")) {
                                LOG.info("siddhi respJson is:{}", respJson);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static String addDate(String utc,int hour){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(utc);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR , hour);
            date = calendar.getTime();
            String strTime = sdf.format(date);
            return strTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return utc;
    }

    private String getSiddhiParams(Message message){
        Map<String, Object> paramMap = new HashMap<String, Object>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stamp = df.format(new Date());
        String timestamp = addDate(stamp,8);
        paramMap.put("timestamp",timestamp);
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        paramMap.put("sd_uuid",uuid);
        String srcip = (String)message.getField("source_ip");
        String strRole = "Unknown";
        Map<String,String[]> ipRole = CGraphql.getIpList();
        if(ipRole.containsKey(srcip)){
            strRole = ipRole.get(srcip)[1];
        }
        paramMap.put("sd_role",strRole);
        Iterator messageFields = message.getFields().entrySet().iterator();
        while (messageFields.hasNext()) {
            Map.Entry pair = (Map.Entry) messageFields.next();
            String key = String.valueOf(pair.getKey());
            LOG.debug("message key is:{}, value is:{}",key, String.valueOf(pair.getValue()));
            if(key.startsWith("sd_")) {
                paramMap.put(key,pair.getValue());
            }
        }

        ObjectMapper mapper=new ObjectMapper();
        try {
            String siddhiParams = mapper.writeValueAsString(paramMap);
            return siddhiParams;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
