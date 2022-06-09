package siddhiTest;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.stream.output.sink.Sink;

import java.util.Collection;
import java.util.List;


public class SiddhiHttp {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("开始了。。。。。。");
        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();

        //Siddhi Application
        //小于150
        String siddhiApp = "\n" +
                "@sink(type='http-call', sink.id='foo',\n" +
                "      publisher.url='http://localhost:8848/show.do',\n" +
                "      @map(type='xml', @payload('{{payloadBody}}')))";

        //Generate runtime
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);



        //Adding callback to retrieve output events from stream
        siddhiAppRuntime.addCallback("LoanResponseStream", new StreamCallback() {
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
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockStream");

        //Start processing
        siddhiAppRuntime.start();
        System.out.println("开始了。。。。。。");
        //Sending events to Siddhi
        inputHandler.send(new Object[]{"我来了", 12345f, 100L});

        Thread.sleep(50000);
        System.out.println("结束了。。。。。。");

        //Shutdown runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi Manager
        siddhiManager.shutdown();

    }
}
