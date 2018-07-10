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

    private Random random;

    public P2PProtocol(String prefix) {
        super();
        this.natType = -2;
        this.isConnected = false;
        this.batteryLevel = 0;
        this.neighborLimit = 60;
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
        if (this.getNumberOfConnection() < neighborLimit && this.batteryLevel > 0) {
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

        // did the node lose connection because of network error?
        if (this.random.nextInt(100) > this.networkStability || this.batteryLevel == 1 && this.isConnected) {
            for (int i = 0; i < this.connections.length; i++) {
                if(this.connections[i]) {
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
        }
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
        this.networkStability = random.nextInt(50)+50;
    }

    public int getStability() {
        return this.networkStability;
    }

    public void generateNat() {
        this.natType = random.nextInt(9)-2;
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

    public RandomWalkMessage() {}
}