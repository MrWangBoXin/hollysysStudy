package org.graylog2.plugin.custom.pipeline;

import com.alibaba.fastjson.JSON;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.custom.timeseriesdb.TS_PgOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.graylog2.plugin.custom.output.RelayOutput.GetAddressList;

public class Communication extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(Communication.class);
    public static final String NAME = "UpdateQuintuple";
    private static final String PARAM = "option";

    public Communication(){
        LOG.info("Communication construct...");
    }

    @Override
    public Long evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        Message message = evaluationContext.currentMessage();
        String msg = (String)message.getField("message");
        try {
            String ip = (String)message.getField("source_ip");
            String srcIp = (String)message.getField("srcIp");
            Integer srcPort = (Integer)message.getField("srcPort");
            String destIp = (String)message.getField("destIp");
            Integer destPort = (Integer)message.getField("destPort");
            String proto = (String)message.getField("proto");
            LOG.info("quintuple:{}:{}-{}:{},{}",srcIp,srcPort,destIp,destPort,proto);
            InsertQuintuple(srcIp,srcPort,destIp,destPort,proto);
            //转发日志
            List<InetSocketAddress> addrList = GetAddressList();
            if(!addrList.isEmpty()){
                MyTools.relayStatus(msg,addrList);
            }
            //过滤日志
            message.setFilterOut(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }

    public int InsertQuintuple(String srcIp,Integer srcPort,String destIp,Integer destPort,String proto) throws FileNotFoundException, SQLException {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strNowDate = formatter.format(new Date());

            String strSQL = String.format("INSERT INTO \"CommunicationRelationship\" (\"id\", \"sourceIp\", \"sourcePort\", \"destIp\", \"destPort\", \"protocol\",\"updatedAt\", \"createdAt\")  VALUES " +
                    " ( \'%s\',\'%s\',%d,\'%s\',%d,\'%s\',\'%s\',\'%s\') on conflict(\"sourceIp\",\"destIp\",\"destPort\",\"protocol\") do update set \"updatedAt\" =\'%s\'" , UUID.randomUUID(),srcIp,srcPort,destIp,destPort,proto,strNowDate,strNowDate,strNowDate);

            LOG.info("InsertQuintuple sql is:{}",strSQL);
            int resultSet = TS_PgOpt.Insert(strSQL);
            if (resultSet == -1) {
                LOG.info("执行语句错误：{}" , strSQL);
                return -1;
            }
            LOG.info("PG Insert quintuple OK");
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
            .string(PARAM)
            .description("JSON String containing Quintuple.")
            .build();
    @Override
    public FunctionDescriptor<Long> descriptor() {
        return FunctionDescriptor.<Long>builder()
                .name(NAME)
                .description("Update Device Communication.")
                .params(valueParam)
                .returnType(Long.class)
                .build();
    }
}
