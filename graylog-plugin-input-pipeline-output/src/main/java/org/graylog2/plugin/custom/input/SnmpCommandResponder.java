package org.graylog2.plugin.custom.input;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.journal.RawMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.OIDTextFormat;


public class SnmpCommandResponder implements CommandResponder {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCommandResponder.class);
    private final RawMessage rawMessage;
    private Message message = null;
    private final OIDTextFormat oidTextFormat = null;
    private static Address listenAddress;
    private static Snmp snmp = null;

    public SnmpCommandResponder(RawMessage rawMessage){
        this.rawMessage = rawMessage;
    }

    public Message getMessage() {
        return message;
    }

    public static void startTrapServer(int trapPort) throws IOException {
        LOG.info("SnmpCommandResponder init........................");
        //监听端的 ip地址 和 监听端口号
        String strAddr = String.format("udp:0.0.0.0/%d",trapPort);
        listenAddress = GenericAddress.parse(strAddr);
        final MessageDispatcher dispatcher = new MessageDispatcherImpl();
        final SnmpCommandResponder responder = new SnmpCommandResponder(null);
        dispatcher.addCommandResponder(responder);


        TransportMapping<?> transport;
        if (listenAddress instanceof UdpAddress) {
            transport = new DefaultUdpTransportMapping((UdpAddress)listenAddress);
        }else{
            transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
        }
        snmp = new Snmp(dispatcher, transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
        snmp.listen();
    }

    public static void stopTrapServer(){
        if(snmp != null){
            try {
                snmp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实现CommandResponder的processPdu方法, 用于处理传入的请求、PDU等信息
     * 当接收到trap时，会自动进入这个方法
     *
     * @param respEvnt
     */
    @Override
    public void processPdu(CommandResponderEvent respEvnt){
        LOG.info("Processing SNMP event: {}", respEvnt.getPeerAddress().toString());
        String addr = respEvnt.getPeerAddress().toString();
        String[] ipPort = addr.split("/");
        final PDU pdu = respEvnt.getPDU();
        String trapmsg = "snmptrap:";
        for (final VariableBinding binding : pdu.getVariableBindings()){
            OID oid = binding.getOid();
            String strOid = null;
            if(oidTextFormat != null){
                strOid = oidTextFormat.formatForRoundTrip(oid.getValue());
            }
            if (strOid == null) {
                strOid = oid.toDottedString();
            }
            final Variable variable = binding.getVariable();
            LOG.info("oid-value is:{},{}",strOid,variable.toString());
            trapmsg += strOid +" = "+ variable.toString()+";";
        }

        MyTools.sendSyslog(trapmsg,ipPort[0],"127.0.0.1");
    }

}
