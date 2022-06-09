package org.graylog2.plugin.custom.input;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.plugin.LocalMetricRegistry;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;

public class UdpSnmpInput extends MessageInput {
    private static final String NAME = "UDP Input";

    @AssistedInject
    public UdpSnmpInput(@Assisted Configuration configuration,
                        MetricRegistry metricRegistry,
                        final UdpSnmpTransport.Factory udpTransportFactory,
                        final LocalMetricRegistry localRegistry,
                        SyslogSnmpCodec.Factory codec,
                        Config config,
                        Descriptor descriptor,
                        ServerStatus serverStatus) {
        super(
                metricRegistry,
                configuration,
                udpTransportFactory.create(configuration),
                localRegistry,
                codec.create(configuration),
                config,
                descriptor,
                serverStatus
        );
    }

    @FactoryClass
    public interface Factory extends MessageInput.Factory<UdpSnmpInput> {
        @Override
        UdpSnmpInput create(Configuration configuration);

        @Override
        UdpSnmpInput.Config getConfig();

        @Override
        Descriptor getDescriptor();
    }

    public static class Descriptor extends MessageInput.Descriptor {
        @Inject
        public Descriptor() {
            super(NAME, false, "");
        }
    }

    @ConfigClass
    public static class Config extends MessageInput.Config {
        @Inject
        public Config(UdpSnmpTransport.Factory transport, SyslogSnmpCodec.Factory codec) {
            super(transport.getConfig(), codec.getConfig());
        }
    }
}
