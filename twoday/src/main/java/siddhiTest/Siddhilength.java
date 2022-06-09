package siddhiTest;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;

public class Siddhilength {


    public static void main(String[] args) throws InterruptedException {

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();

        //Siddhi Application
        //小于150
        //8秒之内所到达的事件
        //#window.length(8)
        String siddhiApp =  "define stream cseEventStream (symbol string, price float, volume long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(3)" +
                "select symbol, price, avg(price) as ap, sum(price) as sp, count(price) as cp " +
                "group by symbol " +
                "insert expired events into outputStream;";

        //Generate runtime
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        //Adding callback to retrieve output events from stream
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                // EventPrinter.print(events);
                for(int i=0;i<events.length;i++){
                    System.out.println(events[i].toString());
                }


                //To convert and print event as a map
                // EventPrinter.print(toMap(events));
            }
        });

        //Get InputHandler to push events into Siddhi
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");



        //Start processing
        siddhiAppRuntime.start();


        //select symbol, price, avg(price) as ap, sum(price) as sp, count(price) as cp
        //Sending events to Siddhi

        //当设置窗口大小是4，当第五个事件到达会将第一个事件弹出，计算的将会是2-4事件，地5个时间不会被加入计算





        inputHandler.send(new Object[]{"one", 1f, 103L});
        inputHandler.send(new Object[]{"one", 2f, 104L});
        inputHandler.send(new Object[]{"one", 3f, 104L});
        inputHandler.send(new Object[]{"one", 4f, 104L});
        inputHandler.send(new Object[]{"one", 5f, 104L});
        Thread.sleep(2000);







        //Shutdown runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi Manager
        siddhiManager.shutdown();

    }

}
