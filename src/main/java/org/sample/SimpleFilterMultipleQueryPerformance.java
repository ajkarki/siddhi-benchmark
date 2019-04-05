package org.sample;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

@State(Scope.Benchmark)
public class SimpleFilterMultipleQueryPerformance {

    Event event;
    InputHandler inputHandler;
    Object[] o = new Object[]{"WSO2", 55.6F, 100, System.currentTimeMillis()};

    @Setup
    public void setup() {
        SiddhiManager manager = new SiddhiManager();
        String siddhiScript = "" +
                "define stream cseEventStream (symbol string, price float, volume int, timestamp long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into outputStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from cseEventStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";
        SiddhiAppRuntime siddhiAppRuntime = manager.createSiddhiAppRuntime(siddhiScript);
        inputHandler = siddhiAppRuntime.getInputHandler("cseEventStream");
        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for(Event e : events){
                    event = e;
                }
            }
        });
        siddhiAppRuntime.start();
    }

    @Benchmark
    public void benchSimpleFilterMultipleQuery() throws InterruptedException {
        inputHandler.send(o);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SimpleFilterMultipleQueryPerformance.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

