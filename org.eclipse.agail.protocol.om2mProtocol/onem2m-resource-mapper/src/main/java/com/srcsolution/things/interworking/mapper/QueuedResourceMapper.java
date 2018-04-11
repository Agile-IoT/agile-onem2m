package com.srcsolution.things.interworking.mapper;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;

import com.srcsolution.things.interworking.data.Thing;
import com.srcsolution.things.interworking.mapper.content.ContentConverter;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;

import static org.slf4j.LoggerFactory.getLogger;

public class QueuedResourceMapper extends ResourceMapper {

    public static final Logger LOGGER = getLogger(QueuedResourceMapper.class);

    private final int[] retryWaitPeriods = {5, 10, 30, 60, 120, 360};

    private final LinkedBlockingQueue<Thing> queue;

    //    private final ExecutorService executor;

    private Thread thread;

    public QueuedResourceMapper(ApplicationEntity root, ContentConverter converter, UUID token, int maxQueueSize) {
        super(root, converter, token);
        queue = new LinkedBlockingQueue<>(maxQueueSize);
        //        executor = Executors.newFixedThreadPool(8);
        start();
    }

    public void start() {
        thread = new Thread(this::consumeQueue, "queueResource");
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            queue.clear();
        }
    }

    @Override
    public void write(Thing thing) {
        queue.add(thing);
    }

    private void consumeQueue() {
        while (true) {
            try {
                LOGGER.trace("Waiting for thing from queue...");
                Thing thingToProcess = queue.take();

                Boolean connectionLost = false;
                int retryWaitPeriodsIndex = 0;

                do {
                    try {
                        LOGGER.debug("Writing thing {}...", thingToProcess.getId());
                        super.write(thingToProcess);
                        connectionLost = false;

                    } catch (Exception e) {
                        LOGGER.warn("Cannot write thing", e);
                        if (e.getCause() instanceof IOException) {
                            int waitRetry = (retryWaitPeriodsIndex >= retryWaitPeriods.length ?
                                    retryWaitPeriods[retryWaitPeriods.length - 1] :
                                    retryWaitPeriods[retryWaitPeriodsIndex++]) * 1000;

                            connectionLost = true;
                            clear(thingToProcess);
                            LOGGER.debug("Waiting {} to retry...", waitRetry);
                            Thread.sleep(waitRetry);
                        }
                    }
                } while (connectionLost);

            } catch (Exception e) {
                LOGGER.warn("Cannot consume queue", e);
            }
        }
    }

    private void clear(Thing thing) {
        thing.getTopics().forEach(topic -> topic.getMessages().clear());
    }

    @Override
    public void clear() {
        super.clear();
        stop();
    }

    public LinkedBlockingQueue<Thing> getQueue() {
        return queue;
    }

}
