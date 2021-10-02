package io.ep2p.somnia.service;

import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.decentralized.SomniaNodeConnectionApi;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.Builder;
import lombok.Data;

import javax.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultRedistributionTaskHandler implements RedistributionTaskHandler {
    private SomniaKademliaSyncRepositoryNode node;
    private ScheduledExecutorService scheduledExecutorService = null;
    private final Lock lock = new ReentrantLock();
    private List<Task> tasks = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void init(SomniaKademliaSyncRepositoryNode selfNode) {
        assert selfNode != null;
        if (this.node == null){
            this.node = selfNode;
            this.start();
        }
    }

    @Override
    public synchronized void addTask(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> publisher) {
        Task task = Task.builder().somniaKey(somniaKey).publisher(publisher).build();
        if (!this.tasks.contains(task)){
            this.tasks.add(task);
        }
    }

    private void start(){
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutorService.scheduleAtFixedRate(
                this,
                0,
                10,
                TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        if(this.lock.tryLock()){
            try {
                this.processTasks();
            } finally {
                this.lock.unlock();
            }
        }
    }

    /**
     *  Looping through tasks and process each one
     *  If an exception raise during the process we increase the failure flag on the task
     *  If a task has failure > 0 we reduce the failure but ignore it and move to next tasks
     *  This helps us prioritize cleaner tasks and not get stuck on them if they keep failing
     */
    private void processTasks() {
        for (Task task : this.tasks) {
            try {
                if (task.getFailures() > 0){
                    task.setFailures(task.getFailures() - 1);
                    continue;
                }
                this.processTask(task);
            }catch (Exception e){
                task.setFailures(task.getFailures() + 1);
            }
        }
    }

    /**
     *  To process a task, we send paginated requests to the publisher of the key and ask them for the data
     *  Publisher validates that our node ID is closer to the key than them and starts to send us the result
     *  We persist the result of each request till there are no results in the request
     *  If an exception gets thrown in the process task remains in the queue
     *  When all of the results are moved to our server we tell the publisher to remove their data
     *  If the call for removing the data is successful we would delete the task from queue
     */
    private void processTask(Task task) {
        int page = 1;
        int size = 20;

        if (this.node.getNodeConnectionApi() instanceof SomniaNodeConnectionApi){

            List<SomniaValue> somniaValues = null;
            while ((somniaValues = ((SomniaNodeConnectionApi) this.node.getNodeConnectionApi()).readForKey(
                    this.node,
                    task.getPublisher(),
                    task.getSomniaKey(),
                    page,
                    size
            )).size() > 0){
                somniaValues.forEach(somniaValue -> {
                    this.node.getKademliaRepository().store(task.getSomniaKey(), somniaValue);
                });
                page += 1;
            }

            if (((SomniaNodeConnectionApi) this.node.getNodeConnectionApi()).deleteForKey(this.node, task.getPublisher(), task.getSomniaKey())) {
                this.tasks.remove(task);
            }
        }
    }

    @Data
    @Builder
    private static class Task {
        private SomniaKey somniaKey;
        private Node<BigInteger, SomniaConnectionInfo> publisher;
        private int failures = 0;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return Objects.equals(somniaKey, task.somniaKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(somniaKey);
        }
    }

    @PreDestroy
    public void destroy(){
        this.scheduledExecutorService.shutdownNow();
    }
}
