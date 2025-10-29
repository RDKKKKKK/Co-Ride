package com.coride.config;

import java.util.Scanner;

public class Solution {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        int m = 2*n-1;

        int[][] dp = new int[m][];


        for(int i = 0;i<=n-1;i++){
            dp[i][0] = 1;
            dp[i][i] = 1;
            dp[m-1-i][0] = 1;
            dp[m-1-i][i] = 1;

            if(i == 1){
                dp[i][1] = 2;
                dp[m-1-i][1] = 2;
                continue;
            }

            for(int j=1;j<i;j++){
                dp[i][j] = dp[i-1][j-1] + dp[i-1][j];
                dp[m-1-i][j] = dp[i][j];
            }
        }

        for(int i = 0;i<m/2;i++){
            for(int j=0;j<i+1;j++){
                System.out.println(dp[i][j]);
            }
        }

        for(int i = m/2;i<m;i++){
            for(int j=0;j<(m-i);j++){
                System.out.println(dp[i][j]);
            }
        }

    }

    public void test(int n){







    }

}


/**
 *
 *
 * input 1:
 *   1
 *
 * input 2:
 *     1
 * 1   2   1
 *     1
 *
 * input 3:
 *       1
 *   1   2   1
 * 1   3   3   1
 *   1   2   1
 *       1
 *
 * input 4:
 *         1
 *     1   2   1
 *   1   3   3   1
 * 1   4   6   4   1
 *   1   3   3   1
 *     1   2   1
 *         1
 *
 * input 5:
 *           1
 *       1   2   1
 *     1   3   3   1
 *   1   4   6   4   1
 * 1   5  10  10   5   1
 *   1   4   6   4   1
 *     1   3   3   1
 *       1   2   1
 *           1
 *
 * input 6:
 *             1
 *         1   2   1
 *       1   3   3   1
 *     1   4   6   4   1
 *   1   5  10  10   5   1
 * 1   6  15  20  15   6   1
 *   1   5  10  10   5   1
 *     1   4   6   4   1
 *       1   3   3   1
 *         1   2   1
 *             1
 *
 * input 7:
 *               1
 *           1   2   1
 *         1   3   3   1
 *       1   4   6   4   1
 *     1   5  10  10   5   1
 *   1   6  15  20  15   6   1
 * 1   7  21  35  35  21   7   1
 *   1   6  15  20  15   6   1
 *     1   5  10  10   5   1
 *       1   4   6   4   1
 *         1   3   3   1
 *           1   2   1
 *               1
 *
 * input 8:
 *                 1
 *             1   2   1
 *           1   3   3   1
 *         1   4   6   4   1
 *       1   5  10  10   5   1
 *     1   6  15  20  15   6   1
 *   1   7  21  35  35  21   7   1
 * 1   8  28  56  70  56  28   8   1
 *   1   7  21  35  35  21   7   1
 *     1   6  15  20  15   6   1
 *       1   5  10  10   5   1
 *         1   4   6   4   1
 *           1   3   3   1
 *             1   2   1
 *                 1
 */