package Hamm;

import java.util.*;
import java.util.Collection;

public class HammingEncoder {
    int messageLength;
    int message[];
    int errorPos;
    //int encodedMessage[];
    Scanner scan = new Scanner(System.in);

/*
    public void printResults() {
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
    }
*/

    public void manualInit() {
        System.out.println("Enter the number of bits for the Hamming data:");
        messageLength = scan.nextInt(); //message length
        message = new int[messageLength]; // the message of 0s and 1s

        for (int i = 0; i < messageLength; i++) {
            System.out.println("Enter bit no. " + (messageLength - i) + ":");
            message[messageLength - i - 1] = scan.nextInt();
        }

        System.out.println("You entered:");
        for (int i = 0; i < messageLength; i++) {
            System.out.print(message[messageLength - i - 1]);
        }
        System.out.println();
    }

    public int[] generateCode(int message[]) {
        // We will return the array as an encoded message


        // We find the number of parity bits required:
        int i = 0, parity_count = 0, j = 0, k = 0;
        while (i < message.length) {
            // 2^(parity bits) must equal the current position
            // Current position is (number of bits traversed + number of parity bits + 1).
            // +1 is needed since array indices start from 0 whereas we need to start from 1.

            if (Math.pow(2, parity_count) == i + parity_count + 1) {
                parity_count++;
            } else {
                i++;
            }
        }

        // Length of 'b' is length of original data (a) + number of parity bits.
        int[] encodedMessage = new int[message.length + parity_count];

        // Initialize this array with '2' to indicate an 'unset' value in parity bit locations:

        for (i = 1; i <= encodedMessage.length; i++) {
            if (Math.pow(2, j) == i) {
                // Found a parity bit location.
                // Adjusting with (-1) to account for array indices starting from 0 instead of 1.

                encodedMessage[i - 1] = 2;
                j++;
            } else {
                encodedMessage[k + j] = message[k++];
            }
        }
        for (i = 0; i < parity_count; i++) {
            // Setting even parity bits at parity bit locations:

            encodedMessage[((int) 1 << i) - 1] = getParity(encodedMessage, i);
        }

        return encodedMessage;
    }

    int getParity(int b[], int power) {
        int parity = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] != 2) {
                // If 'i' doesn't contain an unset value,
                // We will save that index value in k, increase it by 1,
                // Then we convert it into binary:

                int k = i + 1;
                String s = Integer.toBinaryString(k);

                //Nw if the bit at the 2^(power) location of the binary value of index is 1
                //Then we need to check the value stored at that location.
                //Checking if that value is 1 or 0, we will calculate the parity value.

                int x = ((Integer.parseInt(s)) / ((int) Math.pow(10, power))) % 10;
                if (x == 1) {
                    if (b[i] == 1) {
                        parity = (parity + 1) & 1;
                    }
                }
            }
        }
        return parity;
    }

    public int[] addErrors(int[] encodedMessage) {
        System.out.println("Enter position of a bit to alter to check for error detection at the receiver end (0 for no error):");

        int error = scan.nextInt();
        if (errorPos != 0) {
            encodedMessage[errorPos - 1] = (encodedMessage[errorPos - 1] + 1) % 2;
        }
        return encodedMessage;
    }

    public Integer[] receive(int recievedMessage[]) {
        // This is the receiver code. It receives a Hamming code in array 'a'.
        // We also require the number of parity bits added to the original data.
        // Now it must detect the error and correct it, if any.

        int parity_count = recievedMessage.length - messageLength;

        List<Integer> messageHolder = new ArrayList<Integer>();

        int power;
        // We shall use the value stored in 'power' to find the correct bits to check for parity.

        int parity[] = new int[parity_count];
        // 'parity' array will store the values of the parity checks.

        String syndrome = new String();
        // 'syndrome' string will be used to store the integer value of error location.

        for (power = 0; power < parity_count; power++) {
            // We need to check the parities, the same no of times as the no of parity bits added.

            for (int i = 0; i < recievedMessage.length; i++) {
                // Extracting the bit from 2^(power):

                int k = i + 1;
                String s = Integer.toBinaryString(k);
                int bit = ((Integer.parseInt(s)) / ((int) Math.pow(10, power))) % 10;
                if (bit == 1) {
                    if (recievedMessage[i] == 1) {
                        parity[power] = (parity[power] + 1) % 2;
                    }
                }
            }
            syndrome = parity[power] + syndrome;
        }
        // This gives us the parity check equation values.
        // Using these values, we will now check if there is a single bit error and then correct it.

        int error_location = Integer.parseInt(syndrome, 2);
        if (error_location != 0) {
            recievedMessage[error_location - 1] = (recievedMessage[error_location - 1] + 1) % 2;
        }

        // Finally, we shall extract the original data from the received (and corrected) code:

        power = parity_count - 1;
        for (int i = recievedMessage.length; i > 0; i--) {
            if (Math.pow(2, power) != i) {
                messageHolder.add(recievedMessage[i - 1]);
            } else {
                power--;
            }
        }
        System.out.println();

        Collections.reverse(messageHolder);
        Integer[] finalMessage = new Integer[messageHolder.size()];
        finalMessage = messageHolder.toArray(finalMessage);

        return finalMessage;
    }
}