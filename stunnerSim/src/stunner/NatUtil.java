package stunner;

import java.util.HashMap;
import java.util.Random;

public class NatUtil {
    public static HashMap<String, Integer> natChance = new HashMap<String, Integer>();

    static {
        natChance.put("-1 - 0", 5);
        natChance.put("-1 - 2", 1);
        natChance.put("-1 - 3", 3);
        natChance.put("-1 - 4", 12);
        natChance.put("-1 - 5", 8);
        natChance.put("-1 - 6", 2);
        natChance.put("0 - 0", 100);
        natChance.put("0 - 1", 20);
        natChance.put("0 - 2", 10);
        natChance.put("0 - 3", 90);
        natChance.put("0 - 4", 90);
        natChance.put("0 - 5", 100);
        natChance.put("0 - 6", 10);
        natChance.put("1 - 1", 5);
        natChance.put("1 - 2", 1);
        natChance.put("1 - 3", 10);
        natChance.put("1 - 4", 15);
        natChance.put("1 - 5", 20);
        natChance.put("1 - 6", 1);
        natChance.put("2 - 3", 5);
        natChance.put("2 - 4", 10);
        natChance.put("2 - 5", 10);
        natChance.put("3 - 3", 80);
        natChance.put("3 - 4", 50);
        natChance.put("3 - 5", 100);
        natChance.put("3 - 6", 10);
        natChance.put("4 - 4", 100);
        natChance.put("4 - 5", 100);
        natChance.put("4 - 6", 20);
        natChance.put("5 - 5", 100);
        natChance.put("5 - 6", 20);
        natChance.put("6 - 6", 10);
    }

    public static canConnect(int a, int b) {
        String key;
        if (a < b) {
            key = a + " - " b;
        } else {
            key = b + " - " a;
        }
        
        int chance = natChance.get(key);
        if(chance) {
            Random r = new Random();
            return r.nextInt(101) <= chance;
        } else {
            return false;
        }
    }
}