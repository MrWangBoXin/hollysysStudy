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

public class UpdateOsinfoFunction extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateOsinfoFunction.class);
    public static final String NAME = "UpdateOsinfo";
    private static final String PARAM = "flag";

    public UpdateOsinfoFunction(){
        LOG.info("UpdateOsinfoFunction construct...");
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
        LOG.info("------pipeline update dev strParam:{}, ip:{}",strParam,ip);
        CGraphql.UpdateOsinfo( ip, msg);
        return 0L;
    }

    @Override
    public FunctionDescriptor<Long> descriptor() {
        return FunctionDescriptor.<Long>builder()
                .name(NAME)
                .description("Update Device osinfo.")
                .params(valueParam)
                .returnType(Long.class)
                .build();
    }
}
