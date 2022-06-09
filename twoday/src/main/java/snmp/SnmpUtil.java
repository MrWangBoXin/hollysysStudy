package snmp;

import java.io.IOException;

import java.util.List;

import org.snmp4j.CommunityTarget;

import org.snmp4j.PDU;

import org.snmp4j.Snmp;

import org.snmp4j.TransportMapping;

import org.snmp4j.event.ResponseEvent;

import org.snmp4j.mp.SnmpConstants;

import org.snmp4j.smi.Address;

import org.snmp4j.smi.GenericAddress;

import org.snmp4j.smi.OID;

import org.snmp4j.smi.OctetString;

import org.snmp4j.smi.VariableBinding;

import org.snmp4j.transport.DefaultUdpTransportMapping;



public class
SnmpUtil {

    private Snmp snmp = null;

    private Address targetAddress = null;

    public void initComm() throws IOException {

        // 设置Agent的IP和端口
        targetAddress = GenericAddress.parse("udp:127.0.0.1/161");

        //采用UDP传输
        TransportMapping transport = new DefaultUdpTransportMapping();

        //设置snmp关联transport
        snmp = new Snmp(transport);

        //当snmp.send的时候就可以监听到
        transport.listen();

    }

    public void sendPDU() throws IOException {

        // 设置 target
        CommunityTarget target = new CommunityTarget();

        // 设置社区名称给public
        target.setCommunity(new OctetString("public"));

        //设置要发送的地址
        target.setAddress(targetAddress);

        // 通信不成功时的重试次数
        target.setRetries(2);

        // 超时时间
        target.setTimeout(1500);

        // 设置版本1,2c,3
        target.setVersion(SnmpConstants.version2c);

        // 创建 PDU
        PDU pdu = new PDU();


        //设置对象标识(Object identifier-OID)
        //常用标识符  https://www.cnblogs.com/aspx-net/p/3554044.html
        pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.7.0")));


        // MIB的访问方式
        pdu.setType(PDU.GETNEXT);

        // 向Agent发送PDU，并接收Response
        ResponseEvent respEvnt = snmp.send(pdu, target);

        // 解析Response,打印结果
        if (respEvnt != null && respEvnt.getResponse() != null) {

            List<? extends VariableBinding> recVBs;
            recVBs = respEvnt.getResponse()

                    .getVariableBindings();

            for (int i = 0; i < recVBs.size(); i++) {

                VariableBinding recVB = recVBs.get(i);

                System.out.println(recVB.getOid() + " : " + recVB.getVariable());

            }

        }

    }
    public static void main(String[] args) {

        try {

            SnmpUtil util = new SnmpUtil();
            util.initComm();
            util.sendPDU();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}
