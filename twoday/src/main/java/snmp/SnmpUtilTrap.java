package snmp;

import java.io.IOException;

import java.util.List;

import org.snmp4j.*;

import org.snmp4j.event.ResponseEvent;

import org.snmp4j.mp.SnmpConstants;

import org.snmp4j.smi.*;

import org.snmp4j.transport.DefaultUdpTransportMapping;



public class SnmpUtilTrap {

    private Snmp snmp = null;

    private Address targetAddress = null;

    private TransportMapping transport = null;

    public void initComm() throws IOException {

        // 设置Agent方的IP和端口
        targetAddress = GenericAddress.parse("udp:127.0.0.1/162");

        // 设置接收trap的IP和端口
        transport = new DefaultUdpTransportMapping();

        snmp = new Snmp(transport);



        CommandResponder trapRec = new CommandResponder() {

            @Override
            public synchronized void processPdu(CommandResponderEvent e) {

                // 接收trap

                PDU command = e.getPDU();

                if (command != null) {

                    System.out.println("trap接收："+command.toString());

                }

            }

        };

        snmp.addCommandResponder(trapRec);

        transport.listen();



    }


    public  void sentTrap() throws IOException {
        CommunityTarget target = new CommunityTarget();

        target.setCommunity(new OctetString("public"));

        target.setAddress(targetAddress);

        // 通信不成功时的重试次数

        target.setRetries(2);

        // 超时时间

        target.setTimeout(1500);

        target.setVersion(SnmpConstants.version2c);
        PDU pdu = new PDU();

//        pdu.add(new VariableBinding(new OID(".1.3.6.1.2.3377.10.1.1.1.1"),
//
//                new OctetString("SnmpTrap")));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1.0")));
        pdu.setType(PDU.TRAP);


        //发送给Agent,是Trap类型
       snmp.send(pdu, target);

        System.out.println("send finished!!! ");
    }


    public synchronized void listen() {

        System.out.println("Waiting for traps..");

        try {

            this.wait();//Wait for traps to come in

        } catch (InterruptedException ex) {

            System.out.println("Interrupted while waiting for traps: " + ex);

            System.exit(-1);

        }

    }



    public static void main(String[] args) {

        try {

            SnmpUtilTrap util = new SnmpUtilTrap();

            util.initComm();

            util.sentTrap();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            util.listen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
