package org.graylog2.plugin.custom.output;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RelayOutput implements MessageOutput {
    private static final Logger LOG = LoggerFactory.getLogger(RelayOutput.class);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Configuration config;
    public static final String CK_ADDRESS_LIST = "ip_port_list";
    public static final String CK_RELAY_CONDITON = "relay_condition";
    public static final String CK_CONDITON_CONTENT = "relay_condition_content";
    private static List<InetSocketAddress> addrList = new ArrayList<InetSocketAddress>(){ };
    private static String relayCondition="";
    private static String conditionContent="";
    @Inject
    public RelayOutput(@Assisted Stream stream, @Assisted Configuration config) throws MessageOutputConfigurationException {
        LOG.info("relay output started!");
        isRunning.set(true);
        this.config = config;
        try {
            String strAddr = config.getString(CK_ADDRESS_LIST);
            relayCondition = config.getString(CK_RELAY_CONDITON);
            conditionContent = config.getString(CK_CONDITON_CONTENT);
            LOG.info("relay address list:{},{},{}",strAddr,relayCondition,conditionContent);
            if(MyTools.isVersionBCLas()) {
                addrList.clear();
            }
            String[] strAddrList = strAddr.split(",");
            if(strAddrList != null){
                for(String addrPair : strAddrList){
                    String[] pair = addrPair.split(":");
                    if(pair.length == 2 && pair[0]!= null) {
                        String regex = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
                        boolean ipv4 = pair[0].matches(regex);
                        if(ipv4) {
                            int port = Integer.valueOf(pair[1]);
                            if(port > 0 && port < 65535) {
                                InetSocketAddress addr = new InetSocketAddress(pair[0], port);
                                addrList.add(addr);
                            }
                        }
                    }
                }
            }
            if(MyTools.isVersionBCLas()) {
                LOG.info("relay addr list size:{}", addrList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void write(Message message) throws Exception {
        String onelog = (String)message.getField("message");
        if(MyTools.isVersionBCLas()) {
            String ip = (String)message.getField("source_ip");
            LOG.debug("------relay from:{} to ip list size:{}",ip,addrList.size());
        }
        if(relayCondition.equals("contain")){
            if(!conditionContent.isEmpty() && !onelog.contains(conditionContent)){
                return;
            }
        }else if(relayCondition.equals("regex")){
            if(!conditionContent.isEmpty() && !onelog.matches(conditionContent)){
                return;
            }
        }

        for(InetSocketAddress addr : addrList){
            byte[] bs = onelog.getBytes();
            java.net.DatagramPacket udppacket = new java.net.DatagramPacket(bs, bs.length, addr);
            java.net.DatagramSocket socket = new java.net.DatagramSocket();
            socket.send(udppacket);
            socket.close();
        }
    }

    @Override
    public void write(List<Message> messages) throws Exception {
        for (Message message : messages) {
            write(message);
        }
    }

    @Override
    public void stop() {
        LOG.info("Stopping relay output");
        isRunning.set(false);
    }

    public static class Config extends MessageOutput.Config{
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            final ConfigurationRequest cr = new ConfigurationRequest();

            cr.addField(new TextField(CK_ADDRESS_LIST, "address list", "",
                    "contains relay address list. for example: 172.21.0.1:514,172.21.0.2:515",
                    ConfigurationField.Optional.NOT_OPTIONAL)
            );

            Map<String, String> values = new HashMap<>();
            values.put("any","全部转发");
            values.put("contain","包含字符串");
            values.put("regex","正则表达式");
            cr.addField(new DropdownField(CK_RELAY_CONDITON, "relay condition", "any",
                    values,"select the relay condition.",
                    ConfigurationField.Optional.NOT_OPTIONAL)
            );

            cr.addField(new TextField(CK_CONDITON_CONTENT, "condition content", "",
                    "set the relay condition.",
                    ConfigurationField.Optional.OPTIONAL)
            );

            return cr;
        }
    }

    public interface Factory extends MessageOutput.Factory<RelayOutput> {
        @Override
        RelayOutput create(Stream stream, Configuration configuration);

        @Override
        RelayOutput.Config getConfig();

        @Override
        RelayOutput.Descriptor getDescriptor();
    }

    public static class Descriptor extends MessageOutput.Descriptor {
        public Descriptor() {
            super("Relay Output", false, "", "An output plugin that relay log data to other host");
        }
    }

    public static List<InetSocketAddress> GetAddressList(){
        return addrList;
    }
}
