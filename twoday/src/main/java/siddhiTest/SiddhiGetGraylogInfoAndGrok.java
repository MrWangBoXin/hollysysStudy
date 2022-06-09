package siddhiTest;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.thekraken.grok.api.Grok;
import io.thekraken.grok.api.Match;
import io.thekraken.grok.api.exception.GrokException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;

public class SiddhiGetGraylogInfoAndGrok {
    private static final String GROK_PATTERN_PATH = "E:\\IdeaProjects\\twoday\\src\\main\\resources\\patterns\\patterns";

    public static void main(String[] args) throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String info =  "define stream inputStream(log String);  \n" +
                "\n" +
                "@info(name='print') \n" +
                "from inputStream\n" +
                "select log insert into outputStream;";


        //Generate runtime
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(info);

        //Adding callback to retrieve output events from stream
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for(int i=0;i<events.length;i++){
                    System.out.println(events[i].toString());
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");

        //Start processing
        siddhiAppRuntime.start();

        DatagramSocket ds=null;
        try {
            //创建数据包传输对象DatagramSocket 绑定端口号
            ds = new DatagramSocket(888);
            //创建字节数组
            byte[] data = new byte[1024];
            //创建数据包对象，传递字节数组
            DatagramPacket dp = new DatagramPacket(data, data.length);
            //调用ds对象的方法receive传递数据包

            while (true) {
                ds.receive(dp);
                int length = dp.getLength();
                String log=new String(data, 0, length);

                System.out.println("=========="+log);

                String pattern = "%{TIMESTAMP_ISO8601:timestamp1} %{IPV4} %{USERNAME:systemName} %{USERNAME:version} %{MAP},%{MAP},%{MAP},%{MAP},%{MAP},%{MAP1},%{MAP1},%{MAP1},%{MAP1}";
                Match match = null;

                try {
                    Grok grok = new Grok();
                    //添加patter配置文件,默认的grok的pattern是null
                    grok.addPatternFromFile(GROK_PATTERN_PATH);
                    //添加自定义pattern，当然%{IPV4}可以不用已有pattern，也可以自定义正则表达式

                    grok.compile(pattern);
                    match = grok.match(log);
                    match.captures();
                    if (!match.isNull()) {


                        //System.out.println(match.toJson().toString());

                        inputHandler.send(new Object[]{match.toJson().toString()});
                    } else {
                        System.out.println("println not match");
                        inputHandler.send(new Object[]{"not match"});
                    }
                } catch (GrokException e) {
                    e.printStackTrace();
                }

             //   inputHandler.send(new Object[]{log});
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            ds.close();
        }


        //Shutdown runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi Manager
        siddhiManager.shutdown();

    }


}
