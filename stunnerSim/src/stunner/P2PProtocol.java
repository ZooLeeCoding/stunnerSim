package stunner;

import peersim.config.*;
import peersim.core.*;
import peersim.transport.Transport;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import java.util.*;

public class P2PProtocol implements CDProtocol, EDProtocol {

    private int neighborLimit;

    private Node[] connections;
    private boolean isConnected;
    private int batteryLevel;
    private int natType;

    private int networkStability;

    private RandomWalkMessage rw;

    private Random random;

    public P2PProtocol(String prefix) {
        super();
        this.natType = -2;
        this.isConnected = false;
        this.batteryLevel = 0;
        this.neighborLimit = 20;
        this.connections = new Node[neighborLimit];
        this.random = new Random();
    }

    public Object clone() {

        P2PProtocol pp = null;
        try {
            pp = (P2PProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return pp;
    }

    public void nextCycle(Node node, int pid) {
        if (this.batteryLevel > 0) {
            if(this.getNumberOfConnection() < neighborLimit) {
                int linkableID = FastConfig.getLinkable(pid);
                Linkable linkable = (Linkable) node.getProtocol(linkableID);
                if (linkable.degree() > 0) {
                    Node peer = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree()));
                    P2PProtocol neighbor = (P2PProtocol) peer.getProtocol(pid);
                    if (neighbor.getNumberOfConnection() < neighborLimit && !(this.isAlreadyNeighbor(peer)) && NatUtil.canConnect(neighbor.getNat(), this.getNat())) {
                        ((Transport)node.getProtocol(FastConfig.getTransport(pid))).send(node, peer,
                                new FirebaseMessage(true, node), pid);
                        this.connections[this.getNumberOfConnection()] = peer;
                    }
                }
            }
            if(this.hasRandomWalk()) {
                if(this.getNumberOfConnection() > 0) {
                    //System.out.println("Elek es van setam");
                    int j = 0;
                    Node chosen;
                    do {
                        chosen = this.connections[this.random.nextInt(this.getNumberOfConnection())];
                        if(!this.getMessage().alreadyVisited(chosen)) {
                            //System.out.println("probalok inditani");
                            this.getMessage().pushNode(node);
                            ((Transport)node.getProtocol(FastConfig.getTransport(pid))).send(node, chosen,
                                this.getMessage(), pid);
                            this.deleteMessage();
                            break;
                        }
                        j++;
                    } while(j < this.getNumberOfConnection()+1);
                    if(j == this.getNumberOfConnection()+1) {
                        this.getMessage().increaseTtl();
                        if(this.getMessage().getTtl() == 0) this.deleteMessage();
                    }
                } 
            }
        }

        // did the node lose connection because of network error?
        if (this.random.nextInt(100) > this.networkStability || (this.batteryLevel == 1 && this.isConnected)) {
            for (int i = 0; i < this.connections.length; i++) {
                if(this.connections[i] != null) {
                    ((Transport) node.getProtocol(FastConfig.getTransport(pid))).send(node, this.connections[i],
                        new FirebaseMessage(false, node), pid);
                } else { break; }
            }
            this.connections = new Node[neighborLimit];
            this.generateNat();
        }

        // handle battery level and charger state
        if(this.isConnected) {
            if (this.batteryLevel > 90 && this.random.nextInt(100) > 50) this.isConnected = false;
            else if(this.batteryLevel < 100) this.batteryLevel++; 
        } else {
            if (this.batteryLevel < 30 && this.random.nextInt(100) > 50) this.isConnected = true;
            else if(this.batteryLevel > 0 ) this.batteryLevel--;
        }
    }

    public void processEvent(Node node, int pid, Object event) {
        //this.log(event.getClass() + " :: " + FirebaseMessage.class);
        if (event.getClass() == FirebaseMessage.class) {
            FirebaseMessage fbm = (FirebaseMessage) event;
            if (fbm.isConnecting) {
                this.connections[this.getNumberOfConnection()] = fbm.sender;
            } else {
                for (int i = 0; i < this.connections.length; i++) {
                    if (this.connections[i].getID() == fbm.sender.getID()) {
                        for (int j = i; j < this.connections.length - 1; j++) {
                            this.connections[j] = this.connections[j + 1];
                        }
                        break;
                    }
                }
            }
        } else if(event.getClass() == RandomWalkMessage.class) {
            RandomWalkMessage rmessage = (RandomWalkMessage) event;
            rmessage.resetTtl();
            rmessage.increaseLength();
            this.rw = rmessage;
        }
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    public boolean isAlreadyNeighbor(Node n) {
        for(int i = 0; i < this.connections.length; i++) {
            if(this.connections[i] == null) break;
            if(this.connections[i].getID() == n.getID()) {
                return true;
            }
        }
        return false;
    }

    public void initiateRandomWalk() {
        this.rw = new RandomWalkMessage();
    }

    public RandomWalkMessage getMessage() {
        return this.rw;
    }

    public void deleteMessage() {
        this.rw = null;
    }

    public boolean hasRandomWalk() {
        return this.rw != null;
    }

    public void setChargerState(boolean isCharging) {
        this.isConnected = isCharging;
    }

    public void setBattery(int battery) {
        this.batteryLevel = battery;
    }

    public int getBattery() {
        return this.batteryLevel;
    }

    public void generateStability() {
        this.networkStability = random.nextInt(50)+35;
    }

    public int getStability() {
        return this.networkStability;
    }

    public void generateNat() {
        this.natType = random.nextInt(9)-2;
        this.generateStability();
    }

    public int getNat() {
        return this.natType;
    }


    public int getNumberOfConnection() {
        for (int i = 0; i < this.connections.length; i++) {
            if(this.connections[i] == null) {
                return i;
            }
        }
        return this.connections.length;
    }

}

class FirebaseMessage {

    final boolean isConnecting;
    final Node sender;

    public FirebaseMessage( boolean isConnecting, Node sender )
	{
		this.isConnecting = isConnecting;
		this.sender = sender;
    }
}

class RandomWalkMessage {

    int length;
    int ttl;
    Node[] visited;

    public RandomWalkMessage() {
        //System.out.println("init walk");
        this.length = 0;
        this.visited = new Node[3];
        this.resetTtl();
    }

    public void pushNode(Node n) {
        for(int i = 0; i < this.visited.length-1; i++) {
            this.visited[i+1] = this.visited[i];
        }
        this.visited[0] = n;
    }

    public boolean alreadyVisited(Node n) {
        for(int i = 0; i < this.visited.length; i++) {
            if(this.visited[i] != null) {
                if(this.visited[i].getID() == n.getID()) return true;
            } else { break; }
        }
        return false;
    }

    public void increaseTtl() {
        this.ttl--;
    }

    public void increaseLength() {
        this.length++;
    }

    public int getLength() {
        return this.length;
    }

    public int getTtl() {
        return this.ttl;
    }

    public void resetTtl() {
        this.ttl = 3;
    }
}