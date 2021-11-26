package io.ep2p.somnia.util;

import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Callable;

import static io.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;


@Slf4j
public class ExternalNodeHelper {

    public static <C> Optional<C> performIfAlive(
            KademliaNodeAPI<BigInteger, SomniaConnectionInfo> somniaDHTKademliaNode,
            ExternalNode<BigInteger, SomniaConnectionInfo> externalNode,
            Callable<C> callable
    ) throws Exception {
        var date = getDateOfSecondsAgo(somniaDHTKademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());

        KademliaMessage<BigInteger, SomniaConnectionInfo, ?> pingAnswer;

        C c = null;

        if(externalNode.getLastSeen().after(date) || (pingAnswer = somniaDHTKademliaNode.getMessageSender().sendMessage(somniaDHTKademliaNode, externalNode, new PingKademliaMessage<>())).isAlive()){
            c = callable.call();
        }else {
            // We have definitely pinged the node, lets handle the pong specially now that node is offline
            try {
                somniaDHTKademliaNode.onMessage(pingAnswer);
            } catch (HandlerNotFoundException e) {
                // Should not get stuck here. Main objective is to store the message
                log.error(e.getMessage(), e);
            }
        }

        return Optional.ofNullable(c);
    }

}
