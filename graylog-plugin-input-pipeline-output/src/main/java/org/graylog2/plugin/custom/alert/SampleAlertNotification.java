package org.graylog2.plugin.custom.alert;

import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.custom.input.UdpSnmpTransport;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.events.inputs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SampleAlertNotification implements AlarmCallback {
    private static final Logger LOG = LoggerFactory.getLogger(SampleAlertNotification.class.getName());
    private Configuration config;

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        this.config = config;
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
        LOG.info("alert notification callback!!!");
        List<MessageSummary> msgSummaryList = result.getMatchingMessages();
        for (MessageSummary msgSummary: msgSummaryList) {
            LOG.info("alert callback msg is:",msgSummary.getMessage());
        }
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        return new ConfigurationRequest();
    }

    @Override
    public String getName() {
        return "Sample Alert Notification";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return config.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {

    }
}
