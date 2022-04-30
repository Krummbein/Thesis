package bch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;

public class BCHBetterEncoder {
    /*
     * File:    bch3.c
     * Title:   Encoder/decoder for binary BCH codes in C (Version 3.1)
     * Author:  Robert Morelos-Zaragoza
     * Date:    August 1994
     * Revised: June 13, 1997
     *
     * ===============  Encoder/Decoder for binary BCH codes in C =================
     *
     * Version 1:   Original program. The user provides the generator polynomial
     *              of the code (cumbersome!).
     * Version 2:   Computes the generator polynomial of the code.
     * Version 3:   No need to input the coefficients of a primitive polynomial of
     *              degree m, used to construct the Galois Field GF(2**m). The
     *              program now works for any binary BCH code of length such that:
     *              2**(m-1) - 1 < length <= 2**m - 1
     *
     * Note:        You may have to change the size of the arrays to make it work.
     *
     * The encoding and decoding methods used in this program are based on the
     * book "Error Control Coding: Fundamentals and Applications", by Lin and
     * Costello, Prentice Hall, 1983.
     *
     * Thanks to Patrick Boyle (pboyle@era.com) for his observation that 'bch2.c'
     * did not work for lengths other than 2**m-1 which led to this new version.
     * Portions of this program are from 'rs.c', a Reed-Solomon encoder/decoder
     * in C, written by Simon Rockliff (simon@augean.ua.oz.au) on 21/9/89. The
     * previous version of the BCH encoder/decoder in C, 'bch2.c', was written by
     * Robert Morelos-Zaragoza (robert@spectra.eng.hawaii.edu) on 5/19/92.
     *
     * NOTE:
     *          The author is not responsible for any malfunctioning of
     *          this program, nor for any damage caused by it. Please include the
     *          original program along with these comments in any redistribution.
     *
     *  For more information, suggestions, or other ideas on implementing error
     *  correcting codes, please contact me at:
     *
     *                           Robert Morelos-Zaragoza
     *                           5120 Woodway, Suite 7036
     *                           Houston, Texas 77056
     *
     *                    email: r.morelos-zaragoza@ieee.org
     *
     * COPYRIGHT NOTICE: This computer program is free for non-commercial purposes.
     * You may implement this program for any non-commercial application. You may
     * also implement this program for commercial purposes, provided that you
     * obtain my written permission. Any modification of this program is covered
     * by this copyright.
     *
     * == Copyright (c) 1994-7,  Robert Morelos-Zaragoza. All rights reserved.  ==
     *
     * m = order of the Galois field GF(2**m)
     * n = 2**m - 1 = size of the multiplicative group of GF(2**m)
     * length = length of the BCH code
     * t = error correcting capability (max. no. of errors the code corrects)
     * d = 2*t + 1 = designed min. distance = no. of consecutive roots of g(x) + 1
     * k = n - deg(g(x)) = dimension (no. of information bits/codeword) of the code
     * p[] = coefficients of a primitive polynomial used to generate GF(2**m)
     * g[] = coefficients of the generator polynomial, g(x)
     * alpha_to [] = log table of GF(2**m)
     * index_of[] = antilog table of GF(2**m)
     * data[] = information bits = coefficients of data polynomial, i(x)
     * bb[] = coefficients of redundancy polynomial x^(length-k) i(x) modulo g(x)
     * numerr = number of errors
     * errpos[] = error positions
     * recd[] = coefficients of the received polynomial
     * decerror = number of decoding errors (in _message_ positions)
     *
     */


    int m, n, length, k, t, d;
    int p[] = new int[21];
    int alpha_to[] = new int[1048576];
    int index_of[] = new int[1048576];
    int g[] = new int[548576];
    int recd[] = new int[1048576];
    int data[] = new int[1048576];
    int bb[] = new int[548576];
    int seed;
    int numerr = -1;
    int decerror = 0;
    int errpos[] = new int[1024];

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    void read_p() throws IOException {
        /*
         *	Read m, the degree of a primitive polynomial p(x) used to compute the
         *	Galois field GF(2**m). Get precomputed coefficients p[] of p(x). Read
         *	the code length.
         */
        int i, ninf;

        System.out.print("bch3: An encoder/decoder for binary BCH codes\n");
        System.out.print("Copyright (c) 1994-7. Robert Morelos-Zaragoza.\n");
        System.out.print("This program is free, please read first the copyright notice.\n");
        System.out.print("\nFirst, enter a value of m such that the code length is\n");
        System.out.print("2**(m-1) - 1 < length <= 2**m - 1\n\n");
        do {
            System.out.print("Enter m (between 2 and 20): ");
            m = Integer.parseInt(reader.readLine());
        } while (!(m > 1) || !(m < 21));
        for (i = 1; i < m; i++)
            p[i] = 0;
        p[0] = p[m] = 1;
        if (m == 2) p[1] = 1;
        else if (m == 3) p[1] = 1;
        else if (m == 4) p[1] = 1;
        else if (m == 5) p[2] = 1;
        else if (m == 6) p[1] = 1;
        else if (m == 7) p[1] = 1;
        else if (m == 8) p[4] = p[5] = p[6] = 1;
        else if (m == 9) p[4] = 1;
        else if (m == 10) p[3] = 1;
        else if (m == 11) p[2] = 1;
        else if (m == 12) p[3] = p[4] = p[7] = 1;
        else if (m == 13) p[1] = p[3] = p[4] = 1;
        else if (m == 14) p[1] = p[11] = p[12] = 1;
        else if (m == 15) p[1] = 1;
        else if (m == 16) p[2] = p[3] = p[5] = 1;
        else if (m == 17) p[3] = 1;
        else if (m == 18) p[7] = 1;
        else if (m == 19) p[1] = p[5] = p[6] = 1;
        else if (m == 20) p[3] = 1;
        System.out.print("p(x) = ");
        n = 1;
        for (i = 0; i <= m; i++) {
            n *= 2;
            System.out.print(p[i]);
        }
        System.out.print("\n");
        n = n / 2 - 1;
        ninf = (n + 1) / 2 - 1;
        do {
            System.out.print("Enter code length (" + ninf + " < length <= " + n + "): ");
            length = Integer.parseInt(reader.readLine());
        } while (!((length <= n) && (length > ninf)));
    }

    void generate_gf() {
        /*
         * Generate field GF(2**m) from the irreducible polynomial p(X) with
         * coefficients in p[0]..p[m].
         *
         * Lookup tables:
         *   index->polynomial form: alpha_to[] contains j=alpha^i;
         *   polynomial form -> index form:	index_of[j=alpha^i] = i
         *
         * alpha=2 is the primitive element of GF(2**m)
         */
        int i, mask;

        mask = 1;
        alpha_to[m] = 0;
        for (i = 0; i < m; i++) {
            alpha_to[i] = mask;
            index_of[alpha_to[i]] = i;
            if (p[i] != 0)
                alpha_to[m] ^= mask;
            mask <<= 1;
        }
        index_of[alpha_to[m]] = m;
        mask >>= 1;
        for (i = m + 1; i < n; i++) {
            if (alpha_to[i - 1] >= mask)
                alpha_to[i] = alpha_to[m] ^ ((alpha_to[i - 1] ^ mask) << 1);
            else
                alpha_to[i] = alpha_to[i - 1] << 1;
            index_of[alpha_to[i]] = i;
        }
        index_of[0] = -1;
    }

    void gen_poly() throws IOException {
        /*
         * Compute the generator polynomial of a binary BCH code. Fist generate the
         * cycle sets modulo 2**m - 1, cycle[][] =  (i, 2*i, 4*i, ..., 2^l*i). Then
         * determine those cycle sets that contain integers in the set of (d-1)
         * consecutive integers {1..(d-1)}. The generator polynomial is calculated
         * as the product of linear factors of the form (x+alpha^i), for every i in
         * the above cycle sets.
         */
        int ii, jj, ll, kaux;
        int aux, nocycles, root, noterms, rdncy;
        int cycle[][] = new int[1024][21];
        int size[] = new int[1024];
        Integer min[] = new Integer[1024];
        int zeros[] = new int[1024];
        boolean test;

        /* Generate cycle sets modulo n, n = 2**m - 1 */
        cycle[0][0] = 0;
        size[0] = 1;
        cycle[1][0] = 1;
        size[1] = 1;
        jj = 1;            /* cycle set index */
        if (m > 9) {
            System.out.print("Computing cycle sets modulo" + n + "\n");
            System.out.print("(This may take some time)...\n");
        }
        do {
            /* Generate the jj-th cycle set */
            ii = 0;
            do {
                ii++;
                cycle[jj][ii] = (cycle[jj][ii - 1] * 2) % n;
                size[jj]++;
                aux = (cycle[jj][ii] * 2) % n;
            } while (aux != cycle[jj][0]);
            /* Next cycle set representative */
            ll = 0;
            do {
                ll++;
                test = false;
                for (ii = 1; ((ii <= jj) && (!test)); ii++)
                    /* Examine previous cycle sets */
                    for (kaux = 0; ((kaux < size[ii]) && (!test)); kaux++)
                        if (ll == cycle[ii][kaux])
                            test = true;
            } while ((test) && (ll < (n - 1)));
            if (!(test)) {
                jj++;    /* next cycle set index */
                cycle[jj][0] = ll;
                size[jj] = 1;
            }
        } while (ll < (n - 1));
        nocycles = jj;        /* number of cycle sets modulo n */

        System.out.print("Enter the error correcting capability, t: ");
        t = Integer.parseInt(reader.readLine());

        d = 2 * t + 1;

        /* Search for roots 1, 2, ..., d-1 in cycle sets */
        kaux = 0;
        rdncy = 0;
        for (ii = 1; ii <= nocycles; ii++) {
            min[kaux] = 0;
            test = false;
            for (jj = 0; ((jj < size[ii]) && (!test)); jj++)
                for (root = 1; ((root < d) && (!test)); root++)
                    if (root == cycle[ii][jj]) {
                        test = true;
                        min[kaux] = ii;
                    }
            if (!min[kaux].equals(null)) {             // <--------------------- MIGHT BE PROBLEM
                rdncy += size[min[kaux]];
                kaux++;
            }
        }
        noterms = kaux;
        kaux = 1;
        for (ii = 0; ii < noterms; ii++)
            for (jj = 0; jj < size[min[ii]]; jj++) {
                zeros[kaux] = cycle[min[ii]][jj];
                kaux++;
            }

        k = length - rdncy;

        if (k < 0) {
            System.out.print("Parameters invalid!\n");
            return;
        }

        System.out.print("This is a (" + length + ", " + k + ", " + d + ") binary BCH code\n");

        /* Compute the generator polynomial */
        g[0] = alpha_to[zeros[1]];
        g[1] = 1;        /* g(x) = (X + zeros[1]) initially */
        for (ii = 2; ii <= rdncy; ii++) {
            g[ii] = 1;
            for (jj = ii - 1; jj > 0; jj--)
                if (g[jj] != 0)
                    g[jj] = g[jj - 1] ^ alpha_to[(index_of[g[jj]] + zeros[ii]) % n];
                else
                    g[jj] = g[jj - 1];
            g[0] = alpha_to[(index_of[g[0]] + zeros[ii]) % n];
        }
        System.out.print("Generator polynomial:\ng(x) = ");
        for (ii = 0; ii <= rdncy; ii++) {
            System.out.print(g[ii]);
            if (ii != 0 && ((ii % 50) == 0))            // <------------ MIGHT BE A PROBLEM
                System.out.print("\n");
        }
        System.out.print("\n");
    }

    void encode_bch() {
        /*
         * Compute redundacy bb[], the coefficients of b(x). The redundancy
         * polynomial b(x) is the remainder after dividing x^(length-k)*data(x)
         * by the generator polynomial g(x).
         */
        int i, j;
        int feedback;

        for (i = 0; i < length - k; i++)
            bb[i] = 0;
        for (i = k - 1; i >= 0; i--) {
            feedback = data[i] ^ bb[length - k - 1];
            if (feedback != 0) {
                for (j = length - k - 1; j > 0; j--)
                    if (g[j] != 0)
                        bb[j] = bb[j - 1] ^ feedback;
                    else
                        bb[j] = bb[j - 1];
                if (g[0] != 0 && feedback != 0)         // <------------ MIGHT BE A PROBLEM
                    bb[0] = 1;
                else bb[0] = 0;
            } else {
                for (j = length - k - 1; j > 0; j--)
                    bb[j] = bb[j - 1];
                bb[0] = 0;
            }
        }
    }

    void decode_bch() {

        /*
         * Simon Rockliff's implementation of Berlekamp's algorithm.
         *
         * Assume we have received bits in recd[i], i=0..(n-1).
         *
         * Compute the 2*t syndromes by substituting alpha^i into rec(X) and
         * evaluating, storing the syndromes in s[i], i=1..2t (leave s[0] zero) .
         * Then we use the Berlekamp algorithm to find the error location polynomial
         * elp[i].
         *
         * If the degree of the elp is >t, then we cannot correct all the errors, and
         * we have detected an uncorrectable error pattern. We output the information
         * bits uncorrected.
         *
         * If the degree of elp is <=t, we substitute alpha^i , i=1..n into the elp
         * to get the roots, hence the inverse roots, the error location numbers.
         * This step is usually called "Chien's search".
         *
         * If the number of errors located is not equal the degree of the elp, then
         * the decoder assumes that there are more than t errors and cannot correct
         * them, only detect them. We output the information bits uncorrected.
         */

        int i, j, u, q, t2, count = 0, syn_error = 0;
        int elp[][] = new int[1026][1024];
        int d[] = new int[1026];
        int l[] = new int[1026];
        int u_lu[] = new int[1026];
        int s[] = new int[1025];
        int root[] = new int[200];
        int loc[] = new int[200];
        int err[] = new int[1024];
        int reg[] = new int[201];

        t2 = 2 * t;

        /* first form the syndromes */
        System.out.print("S(x) = ");
        for (i = 1; i <= t2; i++) {
            s[i] = 0;
            for (j = 0; j < length; j++)
                if (recd[j] != 0)
                    s[i] ^= alpha_to[(i * j) % n];
            if (s[i] != 0)
                syn_error = 1; /* set error flag if non-zero syndrome */
            /*
             * Note:    If the code is used only for ERROR DETECTION, then
             *          exit program here indicating the presence of errors.
             */
            /* convert syndrome from polynomial form to index form  */
            s[i] = index_of[s[i]];
            System.out.print(s[i]);
        }
        System.out.print("\n");

        if (syn_error != 0) {    /* if there are errors, try to correct them */
            /*
             * Compute the error location polynomial via the Berlekamp
             * iterative algorithm. Following the terminology of Lin and
             * Costello's book :   d[u] is the 'mu'th discrepancy, where
             * u='mu'+1 and 'mu' (the Greek letter!) is the step number
             * ranging from -1 to 2*t (see L&C),  l[u] is the degree of
             * the elp at that step, and u_l[u] is the difference between
             * the step number and the degree of the elp.
             */
            /* initialise table entries */
            d[0] = 0;            /* index form */
            d[1] = s[1];        /* index form */
            elp[0][0] = 0;        /* index form */
            elp[1][0] = 1;        /* polynomial form */
            for (i = 1; i < t2; i++) {
                elp[0][i] = -1;    /* index form */
                elp[1][i] = 0;    /* polynomial form */
            }
            l[0] = 0;
            l[1] = 0;
            u_lu[0] = -1;
            u_lu[1] = 0;
            u = 0;

            do {
                u++;
                if (d[u] == -1) {
                    l[u + 1] = l[u];
                    for (i = 0; i <= l[u]; i++) {
                        elp[u + 1][i] = elp[u][i];
                        elp[u][i] = index_of[elp[u][i]];
                    }
                } else
                    /*
                     * search for words with greatest u_lu[q] for
                     * which d[q]!=0
                     */ {
                    q = u - 1;
                    while ((d[q] == -1) && (q > 0))
                        q--;
                    /* have found first non-zero d[q]  */
                    if (q > 0) {
                        j = q;
                        do {
                            j--;
                            if ((d[j] != -1) && (u_lu[q] < u_lu[j]))
                                q = j;
                        } while (j > 0);
                    }

                    /*
                     * have now found q such that d[u]!=0 and
                     * u_lu[q] is maximum
                     */
                    /* store degree of new elp polynomial */
                    if (l[u] > l[q] + u - q)
                        l[u + 1] = l[u];
                    else
                        l[u + 1] = l[q] + u - q;

                    /* form new elp(x) */
                    for (i = 0; i < t2; i++)
                        elp[u + 1][i] = 0;
                    for (i = 0; i <= l[q]; i++)
                        if (elp[q][i] != -1)
                            elp[u + 1][i + u - q] =
                                    alpha_to[(d[u] + n - d[q] + elp[q][i]) % n];
                    for (i = 0; i <= l[u]; i++) {
                        elp[u + 1][i] ^= elp[u][i];
                        elp[u][i] = index_of[elp[u][i]];
                    }
                }
                u_lu[u + 1] = u - l[u + 1];

                /* form (u+1)th discrepancy */
                if (u < t2) {
                    /* no discrepancy computed on last iteration */
                    if (s[u + 1] != -1)
                        d[u + 1] = alpha_to[s[u + 1]];
                    else
                        d[u + 1] = 0;
                    for (i = 1; i <= l[u + 1]; i++)
                        if ((s[u + 1 - i] != -1) && (elp[u + 1][i] != 0))
                            d[u + 1] ^= alpha_to[(s[u + 1 - i]
                                    + index_of[elp[u + 1][i]]) % n];
                    /* put d[u+1] into index form */
                    d[u + 1] = index_of[d[u + 1]];
                }
            } while ((u < t2) && (l[u + 1] <= t));

            u++;
            if (l[u] <= t) {/* Can correct errors */
                /* put elp into index form */
                for (i = 0; i <= l[u]; i++)
                    elp[u][i] = index_of[elp[u][i]];

                System.out.print("sigma(x) = ");
                for (i = 0; i <= l[u]; i++)
                    System.out.print(elp[u][i]);
                System.out.print("\n");
                System.out.print("Roots: ");

                /* Chien search: find roots of the error location polynomial */
                for (i = 1; i <= l[u]; i++)
                    reg[i] = elp[u][i];
                count = 0;
                for (i = 1; i <= n; i++) {
                    q = 1;
                    for (j = 1; j <= l[u]; j++)
                        if (reg[j] != -1) {
                            reg[j] = (reg[j] + j) % n;
                            q ^= alpha_to[reg[j]];
                        }
                    if (q != 1) {    /* store root and error  <----------- MIGHT BE A PROBLEM
                     * location number indices */
                        root[count] = i;
                        loc[count] = n - i;
                        count++;
                        System.out.print(n - i);
                    }
                }
                System.out.print("\n");
                if (count == l[u])
                    /* no. roots = degree of elp hence <= t errors */
                    for (i = 0; i < l[u]; i++)
                        recd[loc[i]] ^= 1;
                else    /* elp has degree >t hence cannot solve */
                    System.out.print("Incomplete decoding: errors detected\n");
            }
        }
    }

    public void run() throws IOException {
        int i;

        read_p();               /* Read m */
        generate_gf();          /* Construct the Galois Field GF(2**m) */
        gen_poly();             /* Compute the generator polynomial of BCH code */

        /* Randomly generate DATA */
        seed = 131073;
        //srandom(seed);
        Random random = new Random(seed);
        for (i = 0; i < k; i++)
            data[i] = (random.nextInt() & 65536) >> 16;

        encode_bch();           /* encode data */

        /*
         * recd[] are the coefficients of c(x) = x**(length-k)*data(x) + b(x)
         */
        for (i = 0; i < length - k; i++)
            recd[i] = bb[i];
        for (i = 0; i < k; i++)
            recd[i + length - k] = data[i];
        System.out.print("Code polynomial:\nc(x) = ");
        for (i = 0; i < length; i++) {
            System.out.print(recd[i]);
            if (i != 0 && ((i % 50) == 0))    // <----------- MIGHT BE A PROBLEM
                System.out.print("\n");
        }
        System.out.print("\n");

        System.out.print("Enter the number of errors:\n");
        numerr = Integer.parseInt(reader.readLine());    /* CHANNEL errors */
        System.out.print("Enter error locations (integers between");
        System.out.print(" 0 and " + (length - 1) + "): ");
        /*
         * recd[] are the coefficients of r(x) = c(x) + e(x)
         */
        for (i = 0; i < numerr; i++)
            errpos[i] = Integer.parseInt(reader.readLine());
        if (numerr != -1)
            for (i = 0; i < numerr; i++)
                recd[errpos[i]] ^= 1;
        System.out.print("r(x) = ");
        for (i = 0; i < length; i++) {
            System.out.print(recd[i]);
            if (i != 0 && ((i % 50) == 0))
                System.out.print("\n");
        }
        System.out.print("\n");

        decode_bch();             /* DECODE received codeword recv[] */

        /*
         * print out original and decoded data
         */
        System.out.print("Results:\n");
        System.out.print("original data  = ");
        for (i = 0; i < k; i++) {
            System.out.print(data[i]);
            if (i != 0 && ((i % 50) == 0))
                System.out.print("\n");
        }
        System.out.print("\nrecovered data = ");
        for (i = length - k; i < length; i++) {
            System.out.print(recd[i]);
            if ((i - length + k) != 0 && (((i - length + k) % 50) == 0))
                System.out.print("\n");
        }
        System.out.print("\n");

        /*
         * DECODING ERRORS? we compare only the data portion
         */
        for (i = length - k; i < length; i++)
            if (data[i - length + k] != recd[i])
                decerror++;
        if (decerror != 0)
            System.out.print("There were " + decerror + " decoding errors in message positions\n");
        else
            System.out.print("Succesful decoding\n");
    }

    public static void main(String[] args) throws IOException{
        BCHBetterEncoder bchb = new BCHBetterEncoder();
        bchb.run();
    }
}
