package org.graylog2.plugin.custom.input;

import com.codahale.metrics.MetricSet;
import com.github.joschi.jadconfig.util.Size;
import com.google.common.primitives.Ints;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.inputs.transports.NettyTransportConfiguration;
import org.graylog2.inputs.transports.UdpTransport;
import org.graylog2.inputs.transports.netty.EventLoopGroupFactory;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.MisfireException;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.inputs.transports.Transport;
import org.graylog2.plugin.inputs.util.ThroughputCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graylog2.plugin.inputs.transports.NettyTransport;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.*;


public class UdpSnmpTransport extends UdpTransport {
    private static final Logger LOG = LoggerFactory.getLogger(UdpSnmpTransport.class.getName());
    ExecutorService trapExecutor = Executors.newSingleThreadExecutor();
    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);
    private final Configuration configuration;
    private static final String CK_CONFIG_INTERVAL = "snmp_get_interval";
    private static final String CK_CONFIG_TRAPPORT = "trap_port";

    @AssistedInject
    public UdpSnmpTransport(@Assisted Configuration configuration,
                            EventLoopGroupFactory eventLoopGroupFactory,
                            NettyTransportConfiguration nettyTransportConfiguration,
                            ThroughputCounter throughputCounter,
                            LocalMetricRegistry localRegistry) {
        super(configuration, eventLoopGroupFactory, nettyTransportConfiguration, throughputCounter, localRegistry);
        this.configuration = configuration;
    }

    @Override
    public void setMessageAggregator(CodecAggregator codecAggregator) {

    }

    @Override
    public void launch(MessageInput messageInput) throws MisfireException {
        String collectorId = System.getenv("MAIN_COLLECTOR_ID");
        if(collectorId == null || collectorId==""){
            LOG.info("can not find main collector id.................");
        }

        Properties properties = new Properties();
        InputStream in = UdpSnmpTransport.class.getClassLoader().getResourceAsStream("custom/graylog-plugin.properties");
        try {
            properties.load(in);
            int offine_interval = Integer.parseInt(properties.getProperty("offine_interval"));
            long initalDelayMs = 6;//TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
            executorService.scheduleAtFixedRate(new MonitorOfflineTask(offine_interval), initalDelayMs,
                    offine_interval, TimeUnit.SECONDS);
            if(MyTools.isVersionBCLas()){
                executorService.scheduleAtFixedRate(new UpdateConmunicationTask(180), 30,
                        180, TimeUnit.SECONDS);
            }
            int interval = this.configuration.getInt(CK_CONFIG_INTERVAL);
            executorService.scheduleAtFixedRate(new WhitelistTask(), initalDelayMs,
                    interval, TimeUnit.SECONDS);
            executorService.scheduleAtFixedRate(new SnmpGetTask(collectorId), initalDelayMs,
                    interval, TimeUnit.SECONDS);
            int trapPort = this.configuration.getInt(CK_CONFIG_TRAPPORT);
            trapExecutor.execute(() -> {
                try{
                    SnmpCommandResponder.startTrapServer(trapPort);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.launch(messageInput);
    }

    @Override
    public void stop() {
        SnmpCommandResponder.stopTrapServer();
        trapExecutor.shutdown();
        executorService.shutdown();

        super.stop();
    }

    @Override
    public MetricSet getMetricSet() {
        return null;
    }

    @FactoryClass
    public interface Factory extends Transport.Factory<UdpSnmpTransport> {

        @Override
        UdpSnmpTransport create(Configuration configuration);

        @Override
        UdpSnmpTransport.Config getConfig();

    }

    @ConfigClass
    public static class Config extends NettyTransport.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            ConfigurationRequest cr = super.getRequestedConfiguration();
            int recvBufferSize = Ints.saturatedCast(Size.kilobytes(256L).toBytes());
            cr.addField(ConfigurationRequest.Templates.recvBufferSize("recv_buffer_size", recvBufferSize));
            cr.addField(new NumberField(CK_CONFIG_TRAPPORT,
                    "TrapServer port",
                    162,
                    "The snmp trap service port"));
            cr.addField(new NumberField(CK_CONFIG_INTERVAL,
                    "Interval",
                    60,
                    "Time between requests",
                    ConfigurationField.Optional.NOT_OPTIONAL));
            return cr;
        }
    }

}
