package io.ep2p.somnia.service;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.KademliaRepositoryNode;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.TimestampAwareKademliaRepository;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.service.RepublishStrategy;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class SomniaRepublishStrategy implements RepublishStrategy<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue>, Runnable {
    private KademliaRepositoryNode<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue>  kademliaRepositoryNode;
    private NodeSettings.RepublishSettings republishSettings;
    private TimestampAwareKademliaRepository<SomniaKey, SomniaValue> timestampAwareKademliaRepository;
    private ScheduledExecutorService scheduledExecutorService = null;
    private volatile boolean isRunning = false;
    private final Lock lock = new ReentrantLock();

    @Override
    public void configure(KademliaRepositoryNode<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue> kademliaRepositoryNode, NodeSettings.RepublishSettings republishSettings, TimestampAwareKademliaRepository<SomniaKey, SomniaValue> timestampAwareKademliaRepository) {
        this.republishSettings = republishSettings;
        this.kademliaRepositoryNode = kademliaRepositoryNode;
        this.timestampAwareKademliaRepository = timestampAwareKademliaRepository;
        try {
            this.init();
        } catch (InterruptedException e) {
            log.error("Failed to init DefaultRepublishStrategy", e);
        }
    }

    public synchronized void init() throws InterruptedException {
        if (this.scheduledExecutorService != null){
            this.scheduledExecutorService.shutdown();
            if(this.scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES)){
                this.scheduledExecutorService.shutdownNow();
            }
            this.scheduledExecutorService = null;
        }
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.isRunning = false;
    }

    @Override
    public synchronized void start() {
        if (this.isRunning){
            return;
        }

        this.scheduledExecutorService.scheduleAtFixedRate(
                this,
                0,
                this.republishSettings.getRepublishIntervalValue(),
                this.republishSettings.getRepublishIntervalUnit());
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.scheduledExecutorService.shutdownNow();
    }

    @Override
    public void run() {
        boolean l = this.lock.tryLock();
        if (l){
            try {
                Map<SomniaKey, SomniaValue> result = null;
                int page = 1;
                while ((
                        result = this.timestampAwareKademliaRepository.getDataOlderThan(
                                this.republishSettings.getRepublishQueryTimeValue(),
                                this.republishSettings.getRepublishQueryUnit(),
                                page,
                                this.republishSettings.getRepublishQuerySizePerPage()
                        )
                ).size() > 0){
                    page++;
                    result.forEach(this::handleKeyRepublish);
                    if (result.size() < this.republishSettings.getRepublishQuerySizePerPage()){
                        break;
                    }
                }
            }catch (Exception e){
                log.error("Caught error while running republish strategy", e);
            }finally {
                this.lock.unlock();
            }
        }
    }

    public void handleKeyRepublish(SomniaKey key, SomniaValue value){
        // ignoring passed value since strategy is just to inform the closer node to come read data from this node

        // setting republish = true on the key for same reason
        key.setRepublish(true);

        for(Node<BigInteger, SomniaConnectionInfo> node : getNodesToPublishTo(key)){
            this.kademliaRepositoryNode.getNodeConnectionApi().storeAsync(
                    this.kademliaRepositoryNode, this.kademliaRepositoryNode, node, key, new SomniaValue()
            );
        }
    }

    public List<Node<BigInteger, SomniaConnectionInfo>> getNodesToPublishTo(SomniaKey key){
        List<Node<BigInteger, SomniaConnectionInfo>> results = new ArrayList<>();

        // Add closest node first
        FindNodeAnswer<BigInteger, SomniaConnectionInfo> findNodeAnswer = this.kademliaRepositoryNode.getRoutingTable().findClosest(
                this.kademliaRepositoryNode.getKeyHashGenerator().generate(key)
        );

        for (ExternalNode<BigInteger, SomniaConnectionInfo> node : findNodeAnswer.getNodes()) {
            if (node.getId().equals(this.kademliaRepositoryNode.getId())){
                continue;
            }
            results.add(node);
            break;
        }

        return results;
    }
}
