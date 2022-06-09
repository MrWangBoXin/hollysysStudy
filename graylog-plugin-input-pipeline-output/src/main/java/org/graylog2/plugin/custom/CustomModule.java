package org.graylog2.plugin.custom;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import org.graylog.plugins.pipelineprocessor.ast.functions.Function;
import org.graylog2.plugin.PluginModule;
import org.graylog2.plugin.custom.alert.SampleAlertCondition;
import org.graylog2.plugin.custom.alert.SampleAlertNotification;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.custom.input.SyslogSnmpCodec;
import org.graylog2.plugin.custom.input.UdpSnmpInput;
import org.graylog2.plugin.custom.input.UdpSnmpTransport;
import org.graylog2.plugin.custom.output.RelayOutput;
import org.graylog2.plugin.custom.pipeline.*;
import org.graylog2.plugin.custom.timeseriesdb.TS_DbInit;
import org.graylog2.plugin.custom.timeseriesdb.TS_JobHandler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class CustomModule extends PluginModule {
    private static final Logger LOG = LoggerFactory.getLogger(CustomModule.class.getName());

    /**
     * Returns all configuration beans required by this plugin.
     * <p>
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */
//    @Override
//    public Set<? extends PluginConfigBean> getConfigBeans() {
//        return Collections.emptySet();
//    }


    @Override
    protected void configure() {


        //初始化全局参数
        CGraphql.init(System.getenv("BERYLLIUM_URL"), System.getenv("BERYLLIUM_TOKEN"), System.getenv("SIDDHI_URL"));
        //注册input插件
        addTransport("udp-port-transport", UdpSnmpTransport.class);
        addMessageInput(UdpSnmpInput.class);
        //installTransport(transportMapBinder(),"udp-port-transport", UdpSnmpTransport.class);
        //installInput(inputsMapBinder(), UdpSnmpInput.class, UdpSnmpInput.Factory.class);

        addCodec(SyslogSnmpCodec.NAME, SyslogSnmpCodec.class);
        //注册pipline插件
        addMessageProcessorFunction(Hex2DecFunction.NAME, Hex2DecFunction.class);
        addMessageProcessorFunction(Dec2HexFunction.NAME, Dec2HexFunction.class);
        addMessageProcessorFunction(Float2IntFunction.NAME, Float2IntFunction.class);
        addMessageProcessorFunction(UpdateOsinfoFunction.NAME, UpdateOsinfoFunction.class);
        addMessageProcessorFunction(UpdateStatusFunction.NAME, UpdateStatusFunction.class);
        addMessageProcessorFunction(SiddhiFunction.NAME, SiddhiFunction.class);
        addMessageProcessorFunction(Basecheck.NAME, Basecheck.class);
        addMessageProcessorFunction(UpdateRiskPort.NAME, UpdateRiskPort.class);
        //注册output插件
        addMessageOutput(RelayOutput.class, RelayOutput.Factory.class);
        //注册alert插件
        addAlarmCallback(SampleAlertNotification.class);
        addAlertCondition(SampleAlertCondition.class.getCanonicalName(),
                SampleAlertCondition.class,
                SampleAlertCondition.Factory.class);

        /**如果是b/c/las版本**/
        if (MyTools.isVersionBCLas()) {
            addMessageProcessorFunction(Communication.NAME, Communication.class);
        }
        /**如果是b/c/las版本**/
        if (MyTools.isVersionBCLas()) {
            /*
            时序库初始化（建库）
             */
            try {
                TS_DbInit ts_dbInit = new TS_DbInit("log");
                ts_dbInit.CreateDB();
                new TS_DbInit("sec");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*
            Quartz任务管理启动
            定时处理数据（1秒）
             */
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            TS_JobHandler mainScheduler = new TS_JobHandler();
            try {
                mainScheduler.schedulerJob();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        LOG.info("plugin config finished.");
    }

    private void addMessageProcessorFunction(String name, Class<? extends Function<?>> functionClass) {
        addMessageProcessorFunction(binder(), name, functionClass);
    }

    private MapBinder<String, Function<?>> processorFunctionBinder(Binder binder) {
        return MapBinder.newMapBinder(binder, TypeLiteral.get(String.class), new TypeLiteral<Function<?>>() {
        });
    }

    private void addMessageProcessorFunction(Binder binder, String name, Class<? extends Function<?>> functionClass) {
        processorFunctionBinder(binder).addBinding(name).to(functionClass);
    }
}
