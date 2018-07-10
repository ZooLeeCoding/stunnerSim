package stunner;

import peersim.config.*;
import peersim.core.*;

public class StunnerObserver implements Control {


    private static final String PAR_PROT = "protocol";

    private final int pid;

    public StunnerObserver(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    public boolean execute() {

        int maxConnections = 0;
        int maxStability = 0;

        long time = peersim.core.CommonState.getTime();

        for (int i = 0; i < Network.size(); i++) {
            P2PProtocol prot = (P2PProtocol) Network.get(i).getProtocol(pid);
            maxConnections = (prot.getNumberOfConnection() > maxConnections) ? prot.getNumberOfConnection() : maxConnections;
            maxStability = (prot.getStability() > maxStability) ? prot.getStability() : maxStability;
        }

        System.out.println("Time: " + time + " maximum degree: " + maxConnections + ", most stable node: " + maxStability);

        return false;
    }
}
