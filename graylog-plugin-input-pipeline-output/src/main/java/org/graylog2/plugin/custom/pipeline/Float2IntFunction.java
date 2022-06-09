package org.graylog2.plugin.custom.pipeline;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class Float2IntFunction extends AbstractFunction<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(Float2IntFunction.class);
    public static final String NAME = "Float2Int";
    private static final String PARAM = "Floatstring";

    @Override
    public Long evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
    	String floatString = valueParam.required(functionArgs, evaluationContext);
    	if(floatString == ""){
    	    return 0L;
        }
    	try {
            Float fvalue = Float.parseFloat(floatString);
            Long ivalue =  Long.valueOf(Math.round(fvalue));
            LOG.info("Float2IntFunction converts {} to {}",floatString,ivalue);
            return ivalue;
        }catch (NumberFormatException nfe) {}

        return 0L;
    }
    
    private final ParameterDescriptor<String, String> valueParam = ParameterDescriptor
    		.string(PARAM)
    		.description("String containing string float digits.")
    		.build();

   	@Override
   	public FunctionDescriptor<Long> descriptor() {
   	    return FunctionDescriptor.<Long>builder()
   	            .name(NAME)
   	            .description("It effectively converts float string notation to numeric representation.")
   	            .params(valueParam)
   	            .returnType(Long.class)
   	            .build();
   	}
}
