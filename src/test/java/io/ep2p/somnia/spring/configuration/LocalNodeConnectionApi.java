package io.ep2p.somnia.spring.configuration;

import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.exception.NodeIsOfflineException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaRepositoryNode;
import com.github.ep2p.kademlia.node.Node;
import io.ep2p.somnia.model.SomniaConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class LocalNodeConnectionApi<ID extends Number> implements NodeConnectionApi<ID, SomniaConnectionInfo> {
    protected final Map<ID, KademliaNode<ID, SomniaConnectionInfo>> nodeMap = new ConcurrentHashMap<>();

    public void registerNode(KademliaNode<ID, SomniaConnectionInfo> node){
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
    public PingAnswer<ID> ping(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> node) {
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
    public void shutdownSignal(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> node) {
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode != null){
            kademliaNode.onShutdownSignal(caller);
        }
    }

    @Override
    public FindNodeAnswer<ID, SomniaConnectionInfo> findNode(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> node, ID destination) {
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            FindNodeAnswer<ID, SomniaConnectionInfo> findNodeAnswer = new FindNodeAnswer(0);
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
    public <K, V> void storeAsync(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> requester, Node<ID, SomniaConnectionInfo> node, K key, V value) {
        log.info("storeAsync("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+", "+value+")");
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
    public <K> void getRequest(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> requester, Node<ID, SomniaConnectionInfo> node, K key) {
        log.info("getRequest("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+")");
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
    public <K, V> void sendGetResults(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> requester, K key, V value) {
        log.info("sendGetResults("+caller.getId()+", "+requester.getId()+", "+key+", "+value+")");
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onGetResult(caller, key, value);
        }
    }

    @Override
    public <K> void sendStoreResults(Node<ID, SomniaConnectionInfo> caller, Node<ID, SomniaConnectionInfo> requester, K key, boolean success) {
        log.info("sendStoreResults("+caller.getId()+", "+requester.getId()+", "+key+", "+success+")");
        KademliaNode<ID, SomniaConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onStoreResult(caller, key, success);
        }
    }


}