package demo.examples.denormalization.task;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.*;

import java.util.Map;

/**
 * Created by shashankteotia on 6/4/16.
 */
public class ChangelogDenormalizationStreamTask implements InitableTask,StreamTask,WindowableTask {

    private KeyValueStore<Integer, String> a;
    private KeyValueStore<Integer, String> b;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        a = (KeyValueStore<Integer, String>) taskContext.getStore("a");
        b = (KeyValueStore<Integer, String>) taskContext.getStore("b");
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        System.out.println(incomingMessageEnvelope.getSystemStreamPartition().getStream());
        System.out.println(incomingMessageEnvelope.getMessage());
    }

    @Override
    public void window(MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {

    }
}
