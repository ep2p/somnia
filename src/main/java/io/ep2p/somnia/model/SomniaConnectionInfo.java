package io.ep2p.somnia.model;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomniaConnectionInfo implements ConnectionInfo {
    private String address;
    private int port;
    private String protocol;
    private String meta;
}
