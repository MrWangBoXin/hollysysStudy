package test;


import java.io.IOException;
import java.net.*;

public   class  TestUdpSend {
    public   static   void  main( String [] args) throws SocketException {


        String  data= "date1=2022-05-25 time=09:32:18 devname=XFW1697867576139 device_id=XFW1697867576139 log_id=2 type=traffic subtype=allowed pri=notice status=accept vd=\"sis\" dir_disp=org tran_disp=noop src=192.168.10.12 srcname=192.168.10.12 src_port=2043 dst=192.168.10.2 dstname=192.168.10.2 dst_port=135 tran_ip=N/A tran_port=0 service=135/tcp proto=6 app_type=N/A duration=140 rule=2 policyid=2 identidx=0 sent=990 rcvd=538 shaper_drop_sent=0 shaper_drop_rcvd=0 perip_drop=0 shaper_sent_name=\"N/A\" shaper_rcvd_name=\"N/A\" perip_name=\"N/A\" sent_pkt=8 rcvd_pkt=6 vpn=\"N/A\" src_int=\"port4\" dst_int=\"port3\" SN=194772 app=\"N/A\" app_cat=\"N/A\" user=\"N/A\" group=\"N/A\" carrier_ep=\"N/A\",source_ip:172.21.88.25,,soucelogdevicetype=Firewall";
        DatagramSocket datagramSocket=null;

            //实例化套接字，并指定发送端口
        try {
            datagramSocket= new  DatagramSocket();
            //指定数据目的地的地址，以及目标端口
            InetAddress destination=InetAddress.getByName( "172.21.34.18" );
            DatagramPacket datagramPacket=
                    new DatagramPacket(data.getBytes(), data.getBytes().length,destination, 514 );
            //发送数据
            for (int i=0;i<10;i++){
                datagramSocket.send(datagramPacket);
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            datagramSocket.close();
        }

    }
}

