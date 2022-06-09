package test;
import io.thekraken.grok.api.Grok;
import io.thekraken.grok.api.Match;
import io.thekraken.grok.api.exception.GrokException;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

public class GrokTest {

    private static final String GROK_PATTERN_PATH = "E:\\IdeaProjects\\twoday\\src\\main\\resources\\patterns\\patterns";

    public static void main(String[] args) {
      String message = "<6>  2022-02-23 15:33:31 172.21.33.34 HOLLiSec-SAS 11 isuse=0,chCpuName=dcs1_cpu1,chMemName=dcs1_mem1,chnetAName=interface1,chnetBName=interface2,fCpuLoad=10%,fMemLoad=20%,fnetloadA=15%,fnetloadB=30%";
//
       String pattern = "%{TIMESTAMP_ISO8601:timestamp1} %{IPV4} %{USERNAME:systemName} %{USERNAME:version} %{MAP},%{MAP},%{MAP},%{MAP},%{MAP},%{MAP1},%{MAP1},%{MAP1},%{MAP1}";



        //String pattern = "%{TIMESTAMP_ISO8601:timestamp1}%{SPACE}%{WORD:location}.%{WORD:level}%{SPACE}%{IP:ip}%{SPACE}%{MONTH:month}";
//        String message = "2018-08-23 02:56:53 Local7.Info 171.8.79.214 Aug Aug  23 02:56:53 2018 "
//                + "S7506E-A %%10OSPF/6/ORIGINATE_LSA(t): OSPF TrapIDpID1.3.6.1.2.1..1.2.1.14.16.2.1.2.12ospfOriginateLsa: "
//                + "Originate new LSA AreaId 0.0.0.0 LsdbType 5 LsdbLsid id 192.168.17.16Lsd LsdbRouterId Id 192.168.250.254"
//                + " Rou Router er 192.168.250.254.";




        Match match = null;

        try {
            Grok grok = new Grok();
            //添加patter配置文件,默认的grok的pattern是null
            grok.addPatternFromFile(GROK_PATTERN_PATH);
            //添加自定义pattern，当然%{IPV4}可以不用已有pattern，也可以自定义正则表达式

            grok.compile(pattern);
            match = grok.match(message);
            match.captures();
            if (!match.isNull()) {

                Map<String, Object> stringObjectMap = match.toMap();
                Object timestamp1 = stringObjectMap.get("timestamp1");
                System.out.println(timestamp1.toString());
                System.out.println(match.toJson().toString());
            } else {
                System.out.println("not match");
            }
        } catch (GrokException e) {
            e.printStackTrace();
        }


    }
}
