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
public class SimpleWindowSingleQueryPerformance {

    Event event;
    InputHandler inputHandler;
    Object[] o = new Object[]{"1", 55.6f, 100, System.currentTimeMillis()};

    @Setup
    public void setup() {
        SiddhiManager manager = new SiddhiManager();
        String siddhiScript = "" +
                "define stream cseEventStream (symbol string, price float, volume int, timestamp long);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.length(10) " +
                "select symbol, sum(price) as total, avg(volume) as avgVolume, timestamp " +
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
    public void benchSimpleWindowSingleQuery() throws InterruptedException {
        inputHandler.send(o);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(org.sample.SimpleWindowSingleQueryPerformance.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}



