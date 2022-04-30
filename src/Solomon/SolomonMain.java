package Solomon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class SolomonMain {
     Encoder e;
     int[] bads;
     Random rnd;
     int s;
     Decoder d;
     static String input;
     HashSet<Integer> bad;
     char[] c28;
     int[] cFFT;
     int[] c257;

     String s2;
     String s3;
     String s4;

    public void init(int[] intInput){
        // setup

        GF28.init();
        GF257.init();

        rnd = new Random();
        ArrayList<Integer> gens28 = GF28.findGenerators();
        HashSet<Integer> gens257 = GF257.findGenerators();

        // ------- Showing amount of generators -------
        // System.out.println("# of Generators of GF(2^8): " + gens28.size());
        // System.out.println("# of Generators of GF(257): " + gens257.size());

        char gen = 255;
        while(gen == 255) {
            int index = rnd.nextInt(gens28.size());
            int temp = gens28.get(index);
            if(gens257.contains(temp)) gen = (char)temp;
        }

        input = Arrays.toString(intInput); //The message to send

        //gen = 10; //If you'd prefer to hardcode the  generator (just make sure its in both GF(2^8) and GF(257)
        s = 5;

        e = new Encoder(input, s, gen);

        // ------- Number of chosen generator -------
        // System.out.println("Generator: " + (int)gen);

        d = new Decoder(gen, input.length());
        bads = new int[]{}; //{0,1,2,3,4,5,6,7,8,9}; //{35, 27, 8, 3, 15, 23, 37, 18}; //{0,1,2,5, 9, 13, 19};

    }

    public  void encode(){
        // encoding

        c28 = e.slow(); // O(nk) with GF(2^8)
        cFFT = e.fast(); // O(nk) with GF(257)
        c257 = e.slow257(); // FFT O(nlogn) with GF(257)

        bad = createSet(bads);
    }

    public  void decode() {
        // decoding

        //// e.slow decoding
        char[] c2 = d.decode(c28); //, bad
        s2 = new String(c2);

        // e.fast decoding
        int[] c4 = d.decode257(cFFT); //, bad
        char[] c5 = new char[c4.length];
        for(int i = 0; i < c4.length; i++) {
            c5[i] = (char)(c4[i]);
        }
        s3 = new String(c5);

        // e.slow257 decoding
        c4 = d.decode257(c257); //, bad
        c5 = new char[c4.length];
        for(int i = 0; i < c4.length; i++) {
            c5[i] = (char)(c4[i]);
        }
        s4 = new String(c5);
    }

    public  void showResults(){

        System.out.print("Erasures: ");
        for(int i = 0; i < 2*s; i++) {
            int b = rnd.nextInt(input.length() + 2*s);
            bad.add(b);
            System.out.print(b + ", ");
        }
        System.out.println("\n");


        System.out.println("OUTPUT FROM O(nk) IN GF(2^8): " + s2);
        System.out.println("FFT OUTPUT DECODED: " + s3);
        System.out.println("OUTPUT FROM O(nk) IN GF(257): " + s4);
    }

    public  HashSet<Integer> createSet(int[] a) {
        HashSet<Integer> h = new HashSet<Integer>();
        for(int a1 : a) h.add(a1);
        return h;
    }
}