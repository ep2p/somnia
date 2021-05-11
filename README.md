[![](https://jitpack.io/v/ep2p/somnia.svg)](https://jitpack.io/#ep2p/somnia)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.ep2p/somnia/badge.png?gav=true)](https://maven-badges.herokuapp.com/maven-central/io.ep2p/somnia)

```
-------------------------------------------------------------
|   ______                                      _           |
| .' ____ \                                    (_)          |
| | (___ \_|   .--.    _ .--..--.    _ .--.    __    ,--.   |
|  _.____`.  / .'`\ \ [ `.-. .-. |  [ `.-. |  [  |  `'_\ :  |
| | \____) | | \__. |  | | | | | |   | | | |   | |  // | |, |
|  \______.'  '.__.'  [___||__||__] [___||__] [___] \'-;__/ |
-------------------------------------------------------------
|   Decentralized storage based on MongoDB and SpringBoot   |
-------------------------------------------------------------
```

Somnia is a small library to handle data distribution in a trusted (private) decentralized network. Using kademlia algorithm, Somnia is able to transfer data to a certain node on network or distribute it between many nodes.

Somnia is strongly depended on **Spring Boot** and by default it uses **Mongo DB** as storage. Also, it's written on top of [ep2p/kademlia-api](https://github.com/ep2p/kademlia-api) and you'd need to understand the kademlia-api before using Somnia.

On the other hand, due to the nature of kademlia-api library, Somnia is completely independed from how you want to implement connections between the nodes and the discovery system. You can use any protocols such as HTTP, WebSocket, Raw TCP, etc.

