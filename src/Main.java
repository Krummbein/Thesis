import Hamm.HammingEncoder;

import java.util.*;

public class Main {
    public static void main(String args[]) {
        HammingEncoder hamm = new HammingEncoder();
        //hamm.manualInit();
        int[] encodedMessage = hamm.generateCode(new int[] {1,0});
        int[] messedUpMessage = hamm.addErrors(encodedMessage);

    }
}


