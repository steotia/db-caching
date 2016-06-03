package demo.examples.denormalization.task;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;

import java.util.Map;

/**
 * Created by shashankteotia on 6/4/16.
 */
public class DBToChangelogStreamTask implements StreamTask, InitableTask, WindowableTask {

    private KeyValueStore<Integer, String> a;
    private KeyValueStore<Integer, String> b;
    private String oTopic;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        a = (KeyValueStore<Integer, String>) taskContext.getStore("a");
        b = (KeyValueStore<Integer, String>) taskContext.getStore("b");
        oTopic = config.get("output.success.topic.name", "stream-de-normalized-events");
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        Map<String, Object> message = (Map<String, Object>) incomingMessageEnvelope.getMessage();
        System.out.println(message);
        String stream = incomingMessageEnvelope.getSystemStreamPartition().getStream();
        System.out.println("STREAM: "+stream);
        if(stream.equals("maxwell")){
            String table = String.valueOf(message.get("table"));
            Map data = (Map)message.get("data");
            if(table.equals("A")){
                a.put(Integer.parseInt(String.valueOf(data.get("id"))), String.valueOf(data.get("value")));
            } else if(table.equals("B")){
                b.put(Integer.parseInt(String.valueOf(data.get("id"))), String.valueOf(data.get("value")));
            }
        }
        if(stream.equals("events")){
            Integer aid = (Integer)message.get("aid");
            String aval = null;
            if(aid!=null){
                aval = a.get(aid);
            }
            Integer bid = (Integer)message.get("bid");
            String bval = null;
            if(bid!=null){
                bval = b.get(bid);
            }
            message.put("aval",aval);
            message.put("bval",bval);
            messageCollector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", oTopic), message));return;
        }
    }

    @Override
    public void window(MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {

    }
}
