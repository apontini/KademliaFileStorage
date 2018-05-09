# Kademlia File Storage

Kademlia File Storage is a project created for the Distributed Systems course in Computer Science Engineering at University of Padua.
It uses *Kademlia* DHT to distribute file to other peers for storage purpose.
Our goal is to achieve the most permanent file storage solution on a p2p network.
*We will not maintain this project once it's completed (with regards to our course objectives)*.

#### How does it work?
When instantiating a node, a FIND_NODE RPC is performed on a list of a handful nodes (which is serialized in the `nodes` file) to know at most K nodes which are the closest to our node.
This file is serialized by the `writeFixedNodes` method in `Kademlia.java`, if you need to change those nodes for some implementation/change/whatever purposes, please check that.
From then, all other RPCs can be called to interact with the network.
While a node is instantiated, it will receive files from other peers which are performing a *STORE* RPC on the network. These files are frequently refreshed to quickly react to changes occuring in the network, ensuring file persistance for the longest time possible.

#### How to use it
As you can see in `Main.java` you can simply join a network by creating an instance of the `Kademlia` as follows:
```
Kademlia myNode = new Kademlia();
```

You can then start using Kademlia 4 basic RPCs (*FIND_NODE*, *PING*, *FIND_VALUE* and *STORE*) and an extra RPC (*DELETE*), added by us, to interact with the p2p network and store files.
That's it!

If you wish to test it with our provided implementation, just run the `start.sh` file found in the main directory.
This will build all Java classes needed and will run `Main.java`.

#### Settings
For testing purposes, we added some settings inside the `settings.properties` file. These are:
- **port**: the port address which will be used (MUST be forwarded if behind a NAT).
- **fileRefreshThreadSleep**: amount of time (in milliseconds) between one file refresh sequence and another.
- **fileRefreshWait**: amount of time (in milliseconds) to wait before a file can be refreshed.
- **bucketRefreshWait**: amount of tume (in milliseconds) between one bucket refresh and another.
- **socketTimeout**: self-explainatory (in milliseconds).

Based on [Petar Maymounkov and David Mazières paper](https://pdos.csail.mit.edu/~petar/papers/maymounkov-kademlia-lncs.pdf).
Clarifications were found in a [presentation](https://docs.google.com/presentation/d/11qGZlPWu6vEAhA7p3qsQaQtWH7KofEC9dMeBFZ1gYeA/edit#slide=id.g1718cc2bc_08645) made by Tristan Slominski (@tristanls)
