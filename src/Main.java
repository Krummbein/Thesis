import Hamm.HammingEncoder;
import Solomon.SolomonMain;

import java.util.*;

public class Main {
    public static void main(String args[]) {
        SolomonMain sol = new SolomonMain();
        HammingEncoder hamm = new HammingEncoder();

        int[] starterMessage = new int[] {1,0,0,1,0,1,1,1,1,0,1};
        sol.init(starterMessage);
        int[] encodedMessage = sol.encode();
        int[] decodedMessage = sol.decode(encodedMessage);

        System.out.println(Arrays.toString(encodedMessage));
        System.out.println(Arrays.toString(decodedMessage));
        //sol.showResults();

        /*
        int[] hammEncodedMessage = hamm.generateCode(starterMessage);

        int[] messedUpMessage = hamm.addErrors(encodedMessage);
        Integer[] recievedMessage = hamm.receive(messedUpMessage);


        System.out.println("Generated code is:");
        for (int i = 0; i < encodedMessage.length; i++) {
            System.out.print(encodedMessage[encodedMessage.length - i - 1]);
        }
        System.out.println();

        System.out.println("Sent code is:");
        for (int i = 0; i < encodedMessage.length; i++) {
            System.out.print(encodedMessage[encodedMessage.length - i - 1]);
        }
        System.out.println();

        System.out.println("Recieved code is:");
        for (int i = 0; i < recievedMessage.length; i++) {
            System.out.print(recievedMessage[recievedMessage.length - i - 1]);
        }
        System.out.println();
        */

    }
}


