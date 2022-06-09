package siddhiTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.input.DataFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: twoday
 * @BelongsPackage: siddhiTest
 * @Author: wangboxin
 * @CreateTime: 2022-05-25  13:54
 * @Description: TODO
 * @Version: 1.0
 */
public class Test2 {
    public static void main(String[] args) {
        String onelog="date=2022-05-25 time=09:32:18 devname=XFW1697867576139 device_id=XFW1697867576139 log_id=2 type=traffic subtype=allowed pri=notice status=accept vd=\"sis\" dir_disp=org tran_disp=noop src=192.168.10.12 srcname=192.168.10.12 src_port=2043 dst=192.168.10.2 dstname=192.168.10.2 dst_port=135 tran_ip=N/A tran_port=0 service=135/tcp proto=6 app_type=N/A duration=140 rule=2 policyid=2 identidx=0 sent=990 rcvd=538 shaper_drop_sent=0 shaper_drop_rcvd=0 perip_drop=0 shaper_sent_name=\"N/A\" shaper_rcvd_name=\"N/A\" perip_name=\"N/A\" sent_pkt=8 rcvd_pkt=6 vpn=\"N/A\" src_int=\"port4\" dst_int=\"port3\" SN=194772 app=\"N/A\" app_cat=\"N/A\" user=\"N/A\" group=\"N/A\" carrier_ep=\"N/A\",source_ip:172.18.1.42,,sourcelogdevicetype=Firewall";

        onelog="2022-05-25 09:22:14 172.18.1.70 HOLLiSec-SAS 3 128.0.1.80:1163 128.0.8.33:1200 HOLLYSYS_MACS6 初始化下装硬件配置信息,source_ip:172.18.1.70,,sourcelogdevicetype=SafeAudit";

        onelog="HOLLiSec-AGS {\"Description\":\"\",\"ObjectType\":\"45057.0\",\"Time\":\"1653442182\",\"Subject\":\"C:\\\\Windows\\\\System32\\\\svchost.exe\",\"TerminalID\":\"8E91E3F1-3138-460E-BC82-5CB3E06121FA\",\"SubjectType\":\"40962.0\",\"AuditType\":\"9.0\",\"AuditResult\":\"170.0\",\"AuditModule\":\"terminal\",\"OperationType\":\"53252.0\",\"Object\":\"C:\\\\Program Files\\\\Tip\\\\TipSrc\\\\TipGui.exe\",\"ip\":\"129.0.4.102\"},source_ip:172.18.1.80,,sourcelogdevicetype=HostProtection";
        String logtime="null";
        String devFlag="sourcelogdevicetype=";
        if(onelog.contains(devFlag)){
            String devType=onelog.substring(onelog.lastIndexOf(devFlag)+devFlag.length());
            String datetime="";
            String time="";
            String date="";
            switch (devType){
                case "Firewall":
                    datetime=onelog.substring(onelog.indexOf("=")+1,onelog.indexOf("devname")-1);
                    date = datetime.substring(0, datetime.indexOf("time"));
                    time=datetime.substring(datetime.indexOf("=")+1);
                    logtime=date+time;
                    break;
                case "SafeAudit":
                    logtime=onelog.substring(0,19);
                    break;
                case "HostProtection":
                    String jsonStrong=onelog.substring(onelog.indexOf("{"), onelog.indexOf("}")+1);
                    JSONObject jsonObject = JSON.parseObject(jsonStrong);
                    String time1 = String.valueOf(jsonObject.get("Time"))+"000";
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    logtime = sdf.format(new Date(Long.valueOf(time1)));
                    break;
                case "windowsEventlog":
                    logtime=onelog.substring(0,19);
                    break;

            }
            System.out.println(logtime);

        }


    }
}
