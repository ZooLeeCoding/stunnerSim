package stunner;

import peersim.config.*;
import peersim.core.*;
import java.util.Random;

public class StunnerObserver implements Control {


    private static final String PAR_PROT = "protocol";

    private final int pid;

    public StunnerObserver(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    public boolean execute() {

        int maxConnections = 0;
        int maxStability = 0;
        int minStability = 100;
        int maxBattery = 0;
        int minBattery = 100;
        int disabled = 0;
        int unconnected = 0;

        int numberOfWalks = 0;
        int longestWalk = 0;

        long time = peersim.core.CommonState.getTime();

        for (int i = 0; i < Network.size(); i++) {
            P2PProtocol prot = (P2PProtocol) Network.get(i).getProtocol(pid);
            maxConnections = (prot.getNumberOfConnection() > maxConnections) ? prot.getNumberOfConnection() : maxConnections;
            maxStability = (prot.getStability() > maxStability) ? prot.getStability() : maxStability;
            minStability = (prot.getStability() < minStability) ? prot.getStability() : minStability;
            maxBattery = (prot.getBattery() > maxBattery) ? prot.getBattery() : maxBattery;
            minBattery = (prot.getBattery() < minBattery) ? prot.getBattery() : minBattery;
            if(prot.getBattery() == 0) disabled++;
            if(prot.getNumberOfConnection() == 0) unconnected++;
            if(prot.hasRandomWalk()) {
                numberOfWalks++;
                int length = prot.getMessage().getLength();
                longestWalk = (length > longestWalk) ? length : longestWalk;
            }
        }

        if(time > 3) {
            while(numberOfWalks < 10) {
                    P2PProtocol prot = (P2PProtocol) Network.get(new Random().nextInt(Network.size())).getProtocol(pid);
                    if(!prot.hasRandomWalk()) {
                        prot.initiateRandomWalk();
                        numberOfWalks++;
                    }
            }
        }

        System.out.println("Time: " + time + " maximum degree: " + maxConnections + ", most stable node: " + maxStability + ", least stable node: " + minStability + ", max battery level: " + 
            maxBattery + ", min battery level: " + minBattery + ", disabled: " + disabled + ", unconnected: " + unconnected + ", number of random walks: " + numberOfWalks + ",  longest walk length: " + longestWalk);

        return false;
    }
}
