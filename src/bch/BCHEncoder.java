package bch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class BCHEncoder {
    /*
    * m = order of the field GF(2**5) = 5
    * n = 2**5 - 1 = 31 = length
    * t = 2 = error correcting capability !!!
    * d = 2*t + 1 = 5 = designed minimum distance
    * k = n - deg(g(x)) = 21 = dimension
    * p[] = coefficients of primitive polynomial used to generate GF(2**5)
    * g[] = coefficients of generator polynomial, g(x)
    * alpha_to [] = log table of GF(2**5)
    * index_of[] = antilog table of GF(2**5)
    * data[] = coefficients of data polynomial, i(x)
    * bb[] = coefficients of redundancy polynomial ( x**(10) i(x) ) modulo g(x)
    * numerr = number of errors
    * errpos[] = error positions
    * recd[] = coefficients of received polynomial
    * decerror = number of decoding errors (in MESSAGE positions)
     */

// m = 5
    int m = 5, n = 31, k = 21, t = 2, d = 5;
    int length = 31;
    int p[] = new int[6];
    int alpha_to[] = new int[32];
    int index_of[] = new int[32];
    int g[] = new int[11];
    int recd[] = new int[31];
    int data[] = new int[21];
    int bb[] = new int[11];
    int numerr, decerror = 0;
    int errpos[] = new int[32];
    int seed;

    // temporary
    int i;

    void read_p() {
        p[0] = p[2] = p[5] = 1;
        p[1] = p[3] = p[4] = 0;
    }

    void generate_gf() {
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

    void gen_poly() {
        int ii, jj, ll, kaux;
        int test, aux, nocycles, root, noterms, rdncy;
        int cycle[][] = new int[15][6];
        int size[] = new int[15];
        int min[] = new int[11];
        int zeros[] = new int[11];
        cycle[0][0] = 0;
        size[0] = 1;
        cycle[1][0] = 1;
        size[1] = 1;
        jj = 1;
        do {
            ii = 0;
            do {
                ii++;
                cycle[jj][ii] = (cycle[jj][ii - 1] * 2) % n;
                size[jj]++;
                aux = (cycle[jj][ii] * 2) % n;
            } while (aux != cycle[jj][0]);
            ll = 0;
            do {
                ll++;
                test = 0;
                // (!test)
                for (ii = 1; ((ii <= jj) && (test == 0)); ii++)
                    // (!test)
                    for (kaux = 0; ((kaux < size[ii]) && (test == 0)); kaux++)
                        if (ll == cycle[ii][kaux])
                            test = 1;
            } while ((test != 0) && (ll < (n - 1)));// test
            if (test == 0) {// (!test)
                jj++; /* next cycle set index */
                cycle[jj][0] = ll;
                size[jj] = 1;
            }
        } while (ll < (n - 1));
        nocycles = jj; /* number of cycle sets modulo n */
        kaux = 0;
        rdncy = 0;
        for (ii = 1; ii <= nocycles; ii++) {
            min[kaux] = 0;
            for (jj = 0; jj < size[ii]; jj++)
                for (root = 1; root < d; root++)
                    if (root == cycle[ii][jj])
                        min[kaux] = ii;
            if (min[kaux] != 0) {
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
        System.out.printf("This is a (%d, %d, %d) binary BCH code\n", length,
                k, d);
        g[0] = alpha_to[zeros[1]];
        g[1] = 1; /* g(x) = (X + zeros[1]) initially */
        for (ii = 2; ii <= rdncy; ii++) {
            g[ii] = 1;
            for (jj = ii - 1; jj > 0; jj--)
                if (g[jj] != 0)
                    g[jj] = g[jj - 1]
                            ^ alpha_to[(index_of[g[jj]] + zeros[ii]) % n];
                else
                    g[jj] = g[jj - 1];
            g[0] = alpha_to[(index_of[g[0]] + zeros[ii]) % n];
        }
        System.out.printf("g(x) = ");
        for (ii = 0; ii <= rdncy; ii++) {
            System.out.printf("%d", g[ii]);
            if ((ii != 0) && ((ii % 70) == 0))
                System.out.printf("\n");
        }
        System.out.printf("\n");
    }

    void encode_bch() {
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
                bb[0] = g[0] & feedback;// g[0] && feedback
            } else {
                for (j = length - k - 1; j > 0; j--)
                    bb[j] = bb[j - 1];
                bb[0] = 0;
            }
        }

        for (i = 0; i < length - k; i++)
            recd[i] = bb[i]; /* first (length-k) bits are redundancy */
        for (i = 0; i < k; i++)
            recd[i + length - k] = data[i]; /* last k bits are data */
        System.out.printf("c(x) = ");
        for (i = 0; i < length; i++) {
            System.out.printf("%1d", recd[i]);
            if ((i != 0) && ((i % 70) == 0))
                System.out.printf("\n");
        }
        System.out.printf("\n");
    }

    void decode_bch() {
        int i, j, q;
        int elp[] = new int[3], s[] = new int[5], s3;
        int count = 0, syn_error = 0;
        int loc[] = new int[3], err[] = new int[3], reg[] = new int[3];
        int aux;
        /* first form the syndromes */
        System.out.printf("s[] = (");
        for (i = 1; i <= 4; i++) {
            s[i] = 0;
            for (j = 0; j < length; j++)
                if (recd[j] != 0)
                    s[i] ^= alpha_to[(i * j) % n];
            if (s[i] != 0)
                syn_error = 1; /* set flag if non-zero syndrome */
            /*
             * NOTE: If only error detection is needed, then exit the program
             * here...
             */
            /* convert syndrome from polynomial form to index form */
            s[i] = index_of[s[i]];
            System.out.printf("%3d ", s[i]);
        }
        System.out.printf(")\n");
        if (syn_error != 0) { /* If there are errors, try to correct them */
            if (s[1] != -1) {
                s3 = (s[1] * 3) % n;
                if (s[3] == s3) /* Was it a single error ? */
                {
                    System.out.printf("One error at %d\n", s[1]);
                    recd[s[1]] ^= 1; /* Yes: Correct it */
                } else { /*
                 * Assume two errors occurred and solve for the
                 * coefficients of sigma(x), the error locator
                 * polynomail
                 */
                    if (s[3] != -1)
                        aux = alpha_to[s3] ^ alpha_to[s[3]];
                    else
                        aux = alpha_to[s3];
                    elp[0] = 0;
                    elp[1] = (s[2] - index_of[aux] + n) % n;
                    elp[2] = (s[1] - index_of[aux] + n) % n;
                    System.out.printf("sigma(x) = ");
                    for (i = 0; i <= 2; i++)
                        System.out.printf("%3d ", elp[i]);
                    System.out.printf("\n");
                    System.out.printf("Roots: ");
                    /* find roots of the error location polynomial */
                    for (i = 1; i <= 2; i++)
                        reg[i] = elp[i];
                    count = 0;
                    for (i = 1; i <= n; i++) { /* Chien search */
                        q = 1;
                        for (j = 1; j <= 2; j++)
                            if (reg[j] != -1) {
                                reg[j] = (reg[j] + j) % n;
                                q ^= alpha_to[reg[j]];
                            }
                        if (q == 0) { /* store error location number indices */
                            loc[count] = i % n;
                            count++;
                            System.out.printf("%3d ", (i % n));
                        }
                    }
                    System.out.printf("\n");
                    if (count == 2)
                        /* no. roots = degree of elp hence 2 errors */
                        for (i = 0; i < 2; i++)
                            recd[loc[i]] ^= 1;
                    else
                        /* Cannot solve: Error detection */
                        System.out.printf("incomplete decoding\n");
                }
            } else if (s[2] != -1) /* Error detection */
                System.out.printf("incomplete decoding\n");
        }
    }

    void init(){
        read_p(); /* read generator polynomial g(x) */
        generate_gf(); /* generate the Galois Field GF(2**m) */
        gen_poly(); /* Compute the generator polynomial of BCH code */
        seed = 1;
        // srand(seed);
        Random random = new Random(seed);
        /* Randomly generate DATA */

        for (i = 0; i < k; i++)
            data[i] = (random.nextInt() & 67108864) >> 26; // 0100000000000000000000000000
    }

    public void addErrors() {
        System.out.printf("Enter the number of errors and their positions: ");

        BufferedReader wt = new BufferedReader(new InputStreamReader(System.in));
        String numerrStr = null;
        try {
            numerrStr = wt.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        numerr = Integer.valueOf(numerrStr);
        for (i = 0; i < numerr; i++) {
            String errposStr = null;
            try {
                errposStr = wt.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            errpos[i] = Integer.valueOf(errposStr);
            recd[errpos[i]] ^= 1;
        }
        System.out.printf("r(x) = ");
        for (i = 0; i < length; i++)
            System.out.printf("%1d", recd[i]);
        System.out.printf("\n");
    }

    public void printResults(){
        System.out.printf("Results:\n");
        System.out.printf("original data  = ");
        for (i = 0; i < k; i++)
            System.out.printf("%1d", data[i]);
        System.out.printf("\nrecovered data = ");
        for (i = length - k; i < length; i++)
            System.out.printf("%1d", recd[i]);
        System.out.printf("\n");
        /* decoding errors: we compare only the data portion */
        for (i = length - k; i < length; i++)
            if (data[i - length + k] != recd[i])
                decerror++;
        if (decerror != 0)
            System.out.printf("%d message decoding errors\n", decerror);
        else
            System.out.printf("Succesful decoding\n");
    }

    public static void main(String[] args) {
        BCHEncoder bchEncoder = new BCHEncoder();
        bchEncoder.init();
        bchEncoder.encode_bch();
        bchEncoder.addErrors();
        bchEncoder.decode_bch();
        bchEncoder.printResults();
    }
}
