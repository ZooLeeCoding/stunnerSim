package stunner;

import java.util.Random;

import peersim.config.*;
import peersim.core.*;

public class StunnerInit implements Control {


    private static final String PAR_PROT = "protocol";

    private final int pid;

    public StunnerInit(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    public boolean execute() {
        for (int i = 0; i < Network.size(); i++) {
            P2PProtocol prot = (P2PProtocol) Network.get(i).getProtocol(pid);
            prot.generateStability();
            prot.generateNat();
            prot.setBattery(new Random().nextInt(50)+50);
        }
        return false;
    }
}
