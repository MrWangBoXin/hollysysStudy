package org.graylog2.plugin.custom.pipeline;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateRiskPort extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateRiskPort.class);
    public static final String NAME = "UpdateRiskPort";
    private static final String PARAM = "flag";

    public UpdateRiskPort(){
        LOG.info("UpdateRiskPort construct...");
    }

    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
            .string(PARAM)
            .description("String device .")
            .build();

    @Override
    public Long evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        String strParam = valueParam.required(functionArgs, evaluationContext);
        Message message = evaluationContext.currentMessage();
        String msg = (String)message.getField("message");
        String ip = (String)message.getField("source_ip");
        LOG.info("------pipeline update riskport strParam:{}, ip:{}",strParam,ip);
        CGraphql.GraphqlUpdateDevPorts( ip, msg);
        return 0L;
    }

    @Override
    public FunctionDescriptor<Long> descriptor() {
        return FunctionDescriptor.<Long>builder()
                .name(NAME)
                .description("Update os risk ports.")
                .params(valueParam)
                .returnType(Long.class)
                .build();
    }
}
