package org.graylog2.plugin.custom.input;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.InetAddresses;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.inputs.codecs.SyslogCodec;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.ResolvableInetSocketAddress;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.inputs.annotations.Codec;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.AbstractCodec;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.journal.RawMessage;
import org.graylog2.syslog4j.server.SyslogServerEventIF;
import org.graylog2.syslog4j.server.impl.event.CiscoSyslogServerEvent;
import org.graylog2.syslog4j.server.impl.event.FortiGateSyslogEvent;
import org.graylog2.syslog4j.server.impl.event.SyslogServerEvent;
import org.graylog2.syslog4j.server.impl.event.structured.StructuredSyslogServerEvent;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.base.Strings.isNullOrEmpty;

@Codec(name = "syslog-snmp", displayName = "Syslog-snmp")
public class SyslogSnmpCodec extends AbstractCodec {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogSnmpCodec.class.getName());
    private static final Pattern STRUCTURED_SYSLOG_PATTERN = Pattern.compile("<\\d{1,3}>[0-9]\\d{0,2}\\s.*", Pattern.DOTALL);
    private static final Pattern CISCO_WITH_SEQUENCE_NUMBERS_PATTERN = Pattern.compile("<\\d{1,3}>\\d*:\\s.*", Pattern.DOTALL);
    private static final Pattern FORTIGATE_PATTERN = Pattern.compile("<\\d{1,3}>date=.*", Pattern.DOTALL);

    static final String CK_FORCE_RDNS = "force_rdns";
    static final String CK_ALLOW_OVERRIDE_DATE = "allow_override_date";
    static final String CK_EXPAND_STRUCTURED_DATA = "expand_structured_data";
    static final String CK_STORE_FULL_MESSAGE = "store_full_message";
    static String localIp = null;

    private final Timer resolveTime;
    private final Timer decodeTime;
    public static String NAME="syslog-snmp";

    @AssistedInject
    public SyslogSnmpCodec(@Assisted Configuration configuration, MetricRegistry metricRegistry) {
        super(configuration);
        this.resolveTime = metricRegistry.timer(name(SyslogCodec.class, "resolveTime"));
        this.decodeTime = metricRegistry.timer(name(SyslogCodec.class, "decodeTime"));
        try {
            localIp = Inet4Address.getLocalHost().getHostAddress();
            LOG.debug("localIp is:{}",localIp);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public Message decode(@Nonnull RawMessage rawMessage) {
        String msg = new String(rawMessage.getPayload(), StandardCharsets.UTF_8);
        LOG.info("decode msg is:{}",msg);

        try (Timer.Context ignored = this.decodeTime.time()) {
            String source_ip = "source_ip:";
            String remoteIp = null;
            final ResolvableInetSocketAddress address = rawMessage.getRemoteAddress();
            final InetSocketAddress remoteAddress;
            /**不在白名单中的message将会被抛弃-->新版的Filter**/
            boolean bFilter = false;
            if (address == null) {
            remoteAddress = null;
            } else {
                remoteAddress = address.getInetSocketAddress();
                remoteIp = remoteAddress.getAddress().getHostAddress();
                //子采集器已经在原始日志中添加了关键字:source_ip
                if(MyTools.isVersionBCLas()) {
                    int beginPos = msg.indexOf(source_ip);
                    //至少长度大于0.0.0.0
                    if (beginPos > 0) {
                        beginPos += source_ip.length();
                        int endPos = msg.indexOf(",", beginPos);
                        if (endPos > beginPos) {
                            String srcIp = msg.substring(beginPos, endPos);
                            Map<String, String> ipType = CGraphql.getIpType(srcIp);
                            if (!ipType.get("exist").equals("yes")) {
                                //转发的日志不在白名单中要过滤
                                bFilter = true;
                                LOG.info("------filter srcIp is:{}", srcIp);
                            }
                        }
                    } else {
                        msg = String.format("%s,%s%s,", msg, source_ip, remoteIp);
                    }
                }else {
                    if (!msg.contains(source_ip)) {
                        msg = String.format("%s,%s%s", msg, source_ip, remoteIp);
                    }
                }

            }

            Map<String,String> ipType = CGraphql.getIpType(remoteIp);
            LOG.info("ipType is : {}",ipType);
            if (ipType.get("exist").equals("yes")){
                //添加设备类型
                final String constType = "sourcelogdevicetype=";
                if(!msg.contains(constType)){
                    msg = String.format("%s,%s%s",msg,constType,ipType.get("type"));
                }
            }else{
                String dockerIp ="";
                if(MyTools.isVersionBCLas()) {
                     dockerIp = localIp.substring(0, localIp.lastIndexOf("."));
                }
                if(!remoteIp.equals(localIp) && !remoteIp.equals("127.0.0.1")){
                    if(MyTools.isVersionBCLas() && !msg.contains("platform-self[")) {
                        bFilter = true;
                        LOG.info("filter ip is:{},dockerip is:{}", remoteIp, dockerIp);
                    }else {
                        bFilter = true;
                        LOG.info("filter ip is:{}", remoteIp);
                    }
                }

            }
            LOG.debug("finally msg is:{}",msg);
            Message message = parse(msg, remoteAddress == null ? null : remoteAddress.getAddress(), rawMessage.getTimestamp());
            message.setFilterOut(bFilter);
            return message;
        }
    }

    private Message parse(String msg, InetAddress remoteAddress, DateTime receivedTimestamp) {
        final SyslogServerEventIF e;
        if (STRUCTURED_SYSLOG_PATTERN.matcher(msg).matches()) {
            e = new StructuredSyslogServerEvent(msg, remoteAddress);
        } else if (CISCO_WITH_SEQUENCE_NUMBERS_PATTERN.matcher(msg).matches()) {
            e = new CiscoSyslogServerEvent(msg, remoteAddress);
        } else if (FORTIGATE_PATTERN.matcher(msg).matches()) {
            e = new FortiGateSyslogEvent(msg);
        } else {
            e = new SyslogServerEvent(msg, remoteAddress);
        }

        // If the message is a structured one, we do not want the message ID and the structured data in the
        // message string. See: https://github.com/Graylog2/graylog2-server/issues/845#issuecomment-69499719
        final String syslogMessage;
        if (e instanceof StructuredSyslogServerEvent) {
            final String structMessage = ((StructuredSyslogServerEvent) e).getStructuredMessage().getMessage();
            syslogMessage = isNullOrEmpty(structMessage) ? e.getMessage() : structMessage;
        } else {
            syslogMessage = e.getMessage();
        }

        final Message m = new Message(syslogMessage, parseHost(e, remoteAddress), parseDate(e, receivedTimestamp));
        m.addField("facility", Tools.syslogFacilityToReadable(e.getFacility()));
        m.addField("level", e.getLevel());

        // I can haz pattern matching?
        if (e instanceof CiscoSyslogServerEvent) {
            m.addField("sequence_number", ((CiscoSyslogServerEvent) e).getSequenceNumber());
        }
        if (e instanceof FortiGateSyslogEvent) {
            final HashMap<String, Object> fields = new HashMap<>(((FortiGateSyslogEvent) e).getFields());
            // The FortiGate "level" field is a string, Graylog requires a numeric value.
            fields.remove("level");
            m.addFields(fields);
        }

        // Store full message if configured.
        if (configuration.getBoolean(CK_STORE_FULL_MESSAGE)) {
            m.addField("full_message", new String(e.getRaw(), StandardCharsets.UTF_8));
        }


        final boolean expandStructuredData = configuration.getBoolean(CK_EXPAND_STRUCTURED_DATA);
        m.addFields(parseAdditionalData(e, expandStructuredData));

        return m;
    }

    private Map<String, Object> parseAdditionalData(SyslogServerEventIF msg, boolean expand) {

        // Structured syslog has more data we can parse.
        if (msg instanceof StructuredSyslogServerEvent) {
            final StructuredSyslogServerEvent sMsg = (StructuredSyslogServerEvent) msg;
            final Map<String, Object> structuredData = new HashMap<>(extractFields(sMsg, expand));

            if (!isNullOrEmpty(sMsg.getApplicationName())) {
                structuredData.put("application_name", sMsg.getApplicationName());
            }

            if (!isNullOrEmpty(sMsg.getProcessId())) {
                structuredData.put("process_id", sMsg.getProcessId());
            }

            return structuredData;
        } else {
            return Collections.emptyMap();
        }
    }

    private String parseHost(SyslogServerEventIF msg, InetAddress remoteAddress) {
        if (remoteAddress != null && configuration.getBoolean(CK_FORCE_RDNS)) {
            try (Timer.Context ignored = this.resolveTime.time()) {
                return Tools.rdnsLookup(remoteAddress);
            } catch (UnknownHostException e) {
                LOG.warn("Reverse DNS lookup failed. Falling back to parsed hostname.", e);
            }
        }

        final String host = msg.getHost();
        return isNullOrEmpty(host) && remoteAddress != null ? InetAddresses.toAddrString(remoteAddress) : host;
    }

    private DateTime parseDate(SyslogServerEventIF msg, DateTime receivedTimestamp) throws IllegalStateException {
        // Check if date could be parsed.
        if (msg.getDate() == null) {
            if (configuration.getBoolean(CK_ALLOW_OVERRIDE_DATE)) {
                LOG.debug("Date could not be parsed. Was set to NOW because {} is true.", CK_ALLOW_OVERRIDE_DATE);
                return receivedTimestamp;
            } else {
                LOG.warn("Syslog message is missing date or date could not be parsed. (Possibly set {} to true) "
                                + "Not further handling. Message was: {}",
                        CK_ALLOW_OVERRIDE_DATE, new String(msg.getRaw(), StandardCharsets.UTF_8));
                throw new IllegalStateException("Syslog message is missing date or date could not be parsed.");
            }
        }else if(MyTools.isVersionBCLas()){
            long msgtime = msg.getDate().getTime()/1000;
            long recivetime = receivedTimestamp.toDate().getTime()/1000;
             if( msgtime > recivetime){
                 long timespace = msgtime - recivetime;
                 timespace = timespace/3600;
                 if(timespace > 0){
                     LOG.info("---------time is:{}, {}, {}",msg.getDate(),receivedTimestamp,timespace);
                     return receivedTimestamp;
                 }
             }
        }

        return new DateTime(msg.getDate());
    }


    @Override
    @Nullable
    public CodecAggregator getAggregator() {
        return null;
    }

    @FactoryClass
    public interface Factory extends AbstractCodec.Factory<SyslogSnmpCodec> {
        @Override
        SyslogSnmpCodec create(Configuration configuration);

        @Override
        SyslogSnmpCodec.Config getConfig();

        @Override
        SyslogSnmpCodec.Descriptor getDescriptor();
    }

    @ConfigClass
    public static class Config extends AbstractCodec.Config {
        public Config() {
        }

        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            ConfigurationRequest r = super.getRequestedConfiguration();
            //r.addField(new BooleanField("force_rdns", "Force rDNS?", false, "Force rDNS resolution of hostname? Use if hostname cannot be parsed. (Be careful if you are sending DNS logs into this input because it can cause a feedback loop.)"));
            r.addField(new BooleanField("allow_override_date", "Allow overriding date?", true, "Allow to override with current date if date could not be parsed?"));
            r.addField(new BooleanField("store_full_message", "Store full message?", false, "Store the full original syslog message as full_message?"));
            r.addField(new BooleanField("expand_structured_data", "Expand structured data?", false, "Expand structured data elements by prefixing attributes with their SD-ID?"));
            return r;
        }

        @Override
        public void overrideDefaultValues(@Nonnull ConfigurationRequest cr) {
            if (cr.containsField("port")) {
                cr.getField("port").setDefaultValue(514);
            }

        }
    }

    public static class Descriptor extends org.graylog2.plugin.inputs.codecs.AbstractCodec.Descriptor {
        @Inject
        public Descriptor() {
            super(((Codec) SyslogSnmpCodec.class.getAnnotation(Codec.class)).displayName());
        }
    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    Map<String, Object> extractFields(final StructuredSyslogServerEvent msg, final boolean expand) {
        try {
            final Map<String, Map<String, String>> raw = msg.getStructuredMessage().getStructuredData();

            if (raw != null && !raw.isEmpty()) {
                final Map<String, Object> fields = new HashMap<>();
                for (Map.Entry<String, Map<String, String>> entry : raw.entrySet()) {
                    if (expand) {
                        fields.putAll(prefixElements(entry.getKey(), entry.getValue()));
                    } else {
                        fields.putAll(entry.getValue());
                    }
                }
                return fields;
            }
        } catch (Exception e) {
            LOG.debug("Could not extract structured syslog", e);
        }
        return Collections.emptyMap();
    }

    private Map<String, String> prefixElements(final String prefix, final Map<String, String> elements) {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, String> prefixedMap = new HashMap<>(elements.size());
        for (Map.Entry<String, String> entry : elements.entrySet()) {
            prefixedMap.put(prefix.trim() + "_" + entry.getKey(), entry.getValue());
        }

        return prefixedMap;
    }
}
