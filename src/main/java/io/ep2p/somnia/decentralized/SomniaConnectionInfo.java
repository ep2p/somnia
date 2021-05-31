package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.connection.ConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SomniaConnectionInfo implements ConnectionInfo {
    private String address;
}
