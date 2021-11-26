package io.ep2p.somnia.service;

import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.decentralized.protocol.message.ChunkRequestMessage;
import io.ep2p.somnia.decentralized.protocol.message.ChunkResponseMessage;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.model.query.Order;
import io.ep2p.somnia.model.query.Query;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@Slf4j
public class SomniaDistributionJobManager implements DistributionJobManager, Runnable {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final List<Job> jobs = new CopyOnWriteArrayList<>();
    private volatile boolean locked = false;

    @Override
    public void addJob(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> through, KademliaNodeAPI<BigInteger, SomniaConnectionInfo> kademliaNodeAPI) {
        var job = Job.builder().key(somniaKey).node(through).build();
        if (!this.jobs.contains(job)) {
            this.jobs.add(job);
        }
    }

    @Override
    public void removeJob(SomniaKey somniaKey) {
        this.jobs.removeIf(j -> j.getKey().equals(somniaKey));
    }

    @Override
    public List<SomniaKey> getJobs() {
        List<SomniaKey> keys = new ArrayList<>();
        this.jobs.forEach(job -> keys.add(job.getKey()));
        return keys;
    }

    @Override
    public void start() {
        scheduledExecutorService.submit(this);
    }

    @Override
    public void run() {
        if (locked || this.jobs.size() == 0)
            return;
        Job job = this.jobs.get(0);
        try {
            this.processJob(job);
        } catch (Exception e){
            log.error("Caught exception while processing job", e);
        } finally {
            locked = false;
            this.jobs.remove(job);
        }
    }

    protected void processJob(Job job){
        var dhtKademliaNodeAPI = job.dhtKademliaNodeAPI;
        SomniaKey key = job.getKey();
        key.setMeta(SomniaKey.Meta.builder().build());
        key.getMeta().setQuery(new Query().addOrder(Order.builder().field("id").direction(Order.Direction.ASC).build()));
        long offset = 0;
        int limit = 10;
        long outputSize = -1;
        var chunkRequest = new ChunkRequestMessage();
        chunkRequest.setData(ChunkRequestMessage.ChunkRequestData.builder().key(key).build());

        while (outputSize == -1 || outputSize >= limit){
            key.getMeta().setOffset(offset);
            key.getMeta().setLimit(limit);

            chunkRequest.setData(ChunkRequestMessage.ChunkRequestData.builder()
                    .key(job.key)
                    .build());
            KademliaMessage<BigInteger, SomniaConnectionInfo, Serializable> responseMessage = dhtKademliaNodeAPI.getMessageSender().sendMessage(dhtKademliaNodeAPI, job.getNode(), chunkRequest);
            ChunkResponseMessage.ChunkResponseData chunkResponseData = (ChunkResponseMessage.ChunkResponseData) responseMessage.getData();
            outputSize = chunkResponseData.getValue().getCount();
            dhtKademliaNodeAPI.getKademliaRepository().store(key, chunkResponseData.getValue());
        }
    }

    @Builder
    @Data
    private static class Job {
        private SomniaKey key;
        private DHTKademliaNodeAPI<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue> dhtKademliaNodeAPI;
        private Node<BigInteger, SomniaConnectionInfo> node;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Job job = (Job) o;
            return Objects.equals(key, job.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

}
