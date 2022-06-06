package tp2.impl.servers.rest;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp2.impl.kafka.KafkaPublisher;
import tp2.impl.kafka.KafkaSubscriber;
import tp2.impl.kafka.RecordProcessor;
import tp2.impl.kafka.sync.SyncPoint;

public class TestProcessor extends Thread implements RecordProcessor {
    static final String FROM_BEGINNING = "earliest";
    static final String TOPIC = "single_partition_topic";
    static final String KAFKA_BROKERS = "kafka";

    static int MAX_NUM_THREADS = 4;

    final String replicaId;
    final KafkaPublisher sender;
    final KafkaSubscriber receiver;
    final SyncPoint<String> sync;

    TestProcessor( String replicaId ) {
        this.replicaId = replicaId;
        this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
        this.receiver.start(false, this);
        this.sync = new SyncPoint<>();
    }


    public void run() {
        for(;;) {
            var operation = "op" + System.nanoTime();
            var version = sender.publish(TOPIC, replicaId, operation);
            var result = sync.waitForResult(version);
            System.out.printf("Op: %s, version: %s, result: %s\n", operation, version, result);
            sleep(500);
            //mandar a opreção
            //System.err.printf("replicaId: %s, sync state: %s", replicaId, sync);
        }
    }

    @Override
    public void onReceive(ConsumerRecord<String, String> r) {
        //on receive mandar os servidores fazer coisas
        var version = r.offset();
        System.out.printf("%s : processing: (%d, %s)\n", replicaId, version, r.value());

        var result = "result of " + r.value();
        sync.setResult( version, result);
    }

    private void sleep( int ms ) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
