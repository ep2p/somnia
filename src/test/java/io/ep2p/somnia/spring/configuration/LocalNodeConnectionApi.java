package io.ep2p.somnia.spring.configuration;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.exception.NodeIsOfflineException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaRepositoryNode;
import com.github.ep2p.kademlia.node.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class LocalNodeConnectionApi<ID extends Number> implements NodeConnectionApi<ID, ConnectionInfo> {
    protected final Map<ID, KademliaNode<ID, ConnectionInfo>> nodeMap = new ConcurrentHashMap<>();

    public void registerNode(KademliaNode<ID, ConnectionInfo> node){
        log.info("Registering node with id " + node.getId());
        nodeMap.putIfAbsent(node.getId(), node);
    }

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LocalNodeConnectionApi() {
        synchronized (nodeMap){
            nodeMap.clear();
        }
    }

    @Override
    public PingAnswer<ID> ping(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> node) {
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            PingAnswer pingAnswer = new PingAnswer(node.getId());
            pingAnswer.setAlive(false);
            return pingAnswer;
        }
        try {
            return kademliaNode.onPing(caller);
        } catch (NodeIsOfflineException e) {
            return new PingAnswer(node.getId(), false);
        }
    }

    @Override
    public void shutdownSignal(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> node) {
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode != null){
            kademliaNode.onShutdownSignal(caller);
        }
    }

    @Override
    public FindNodeAnswer<ID, ConnectionInfo> findNode(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> node, ID destination) {
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            FindNodeAnswer<ID, ConnectionInfo> findNodeAnswer = new FindNodeAnswer(0);
            findNodeAnswer.setAlive(false);
            return findNodeAnswer;
        }
        try {
            return kademliaNode.onFindNode(caller, destination == null ? caller.getId() : destination);
        } catch (NodeIsOfflineException e) {
            return new FindNodeAnswer(0);
        }
    }

    @Override
    public <K, V> void storeAsync(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> requester, Node<ID, ConnectionInfo> node, K key, V value) {
        log.info("storeAsync("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+", "+value+")");
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Fake network latency
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((KademliaRepositoryNode) kademliaNode).onStoreRequest(caller, requester, key, value);
                }
            });
        }
        if(kademliaNode == null){
            throw new RuntimeException("Node "+ node.getId() +" not available");
        }
    }

    @Override
    public <K> void getRequest(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> requester, Node<ID, ConnectionInfo> node, K key) {
        log.info("getRequest("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+")");
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Fake network latency
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((KademliaRepositoryNode) kademliaNode).onGetRequest(caller, requester, key);
                }
            });
        }
    }


    @Override
    public <K, V> void sendGetResults(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> requester, K key, V value) {
        log.info("sendGetResults("+caller.getId()+", "+requester.getId()+", "+key+", "+value+")");
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onGetResult(caller, key, value);
        }
    }

    @Override
    public <K> void sendStoreResults(Node<ID, ConnectionInfo> caller, Node<ID, ConnectionInfo> requester, K key, boolean success) {
        log.info("sendStoreResults("+caller.getId()+", "+requester.getId()+", "+key+", "+success+")");
        KademliaNode<ID, ConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onStoreResult(caller, key, success);
        }
    }


}
