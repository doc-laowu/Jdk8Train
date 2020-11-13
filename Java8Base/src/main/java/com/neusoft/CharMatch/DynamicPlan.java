package com.neusoft.CharMatch;

/**
 * @Title: DynamicPlan
 * @ProjectName Jdk8Train
 * @Description: TODO 动态规划问题
 * @Author yisheng.wu
 * @Date 2020/8/610:38
 */
public class DynamicPlan {

    /** 求最长公共字串问题, 使用矩阵法求解
      * @Param [s, t]
      * @return int
      **/
    public static int getLCS(String s, String t) {
        if (s == null || t == null) {
            return 0;
        }
        int result = 0;
        int sLength = s.length();
        int tLength = t.length();
        int[][] dp = new int[sLength + 1][tLength + 1];
        for (int i = 1; i <= sLength; i++) {
            for (int k = 1; k <= tLength; k++) {
                if (s.charAt(i - 1) == t.charAt(k - 1)) {
                    dp[i][k] = dp[i - 1][k - 1] + 1;
                    result = Math.max(dp[i][k], result);
                }
            }
        }

        int max = 0;
        int index = 0;
        for (int i = 1; i <= sLength + 1; i++) {
            for (int k = 1; k <= tLength + 1; k++) {
                if(dp[i-1][k-1] > max){
                    max = dp[i-1][k-1];
                    index = i-1;
                }
            }
        }

        System.out.println(s.substring(index-result, index));

        return result;
    }

    public static void main(String[] args) {

        String str1 = "aaabcab";
        String str2 = "ababcbac";

        System.out.println(getLCS(str1, str2));
    }
}
