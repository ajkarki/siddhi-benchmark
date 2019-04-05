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
public class SimplePartitionedDoubleFilterQueryPerformance {

    Event event;
    InputHandler inputHandler;
    Object[] o = new Object[]{"1", 55.6f, 100, System.currentTimeMillis()};

    @Setup
    public void setup() {
        SiddhiManager manager = new SiddhiManager();
        String siddhiScript = "" +
                "define stream cseEventStream (symbol string, price float, volume long, timestamp long);" +
                "" +
                "partition with (symbol of cseEventStream) " +
                "begin " +
                "   @info(name = 'query1') " +
                "   from cseEventStream[700 > price] " +
                "   select * " +
                "   insert into #innerOutputStream ; " +
                "" +
                "   @info(name = 'query2') " +
                "   from #innerOutputStream[700 > price] " +
                "   select * " +
                "   insert into outputStream ;" +
                "end;";
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
    public void benchSimplePartitionedDoubleFilterQuery() throws InterruptedException {
        inputHandler.send(o);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SimplePartitionedDoubleFilterQueryPerformance.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

