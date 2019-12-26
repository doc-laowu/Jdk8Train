package com.neusoft.CharMatch;

/**
 * @Title: ViolentMatch
 * @ProjectName Jdk8Train
 * @Description: TODO 暴力匹配算法
 * @Author yisheng.wu
 * @Date 2019/12/1214:18
 */
public class ViolentMatch {

    /**
      * @Author: yisheng.wu
      * @Description TODO 暴力搜索的方式进行字符串匹配
      * @Date 14:44 2019/12/12
      * @Param [str, pattern]
      * @return int
      **/
    public int BF_match1(String str, String pattern){

        char[] s = str.toCharArray();
        char[] p = pattern.toCharArray();

        int sLen = s.length;
        int pLen = p.length;

        int i = 0;
        int j = 0;
        while (i < sLen && j < pLen)
        {
            if (s[i] == p[j])
            {
                //①如果当前字符匹配成功（即S[i] == P[j]），则i++，j++
                i++;
                j++;
            }
            else
            {
                //②如果失配（即S[i]! = P[j]），令i = i - (j - 1)，j = 0
                i = i - j + 1;
                j = 0;
            }
        }
        //匹配成功，返回模式串p在文本串s中的位置，否则返回-1
        if (j == pLen)
            return i - j;
        else
            return -1;

    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 暴力搜索的方式进行字符串匹配
      * @Date 14:44 2019/12/12
      * @Param [str, pattern]
      * @return int
      **/
    public int BF_match2(String str, String pattern){

        char[] s = str.toCharArray();
        char[] p = pattern.toCharArray();

        int slen = s.length;
        int plen = p.length;

        for(int i=0; i<slen; i++){

            for(int j=0, k=i; j<plen; j++, k++){

                if(s[k] == p[j] && (j == plen-1) ){

                    System.out.println("index:"+i);
//                    return i;
                } else if(s[k] != p[j]){

                    break;
                }

            }
        }

        return -1;
    }



    private boolean same(char[] a, char[] b, int m)
    {
        for(int i = 0; i < m; ++i)
        {
            if(a[i] != b[i])
                return false;
        }
        return true;
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO
      * @Date 19:10 2019/12/12
      * @Param [str, pattern]
      * @return int
      **/
    public int RK_match(String str, String pattern){

        char[] s = str.toCharArray();
        char[] t = pattern.toCharArray();

        int n = s.length, m = t.length;
        int[] table = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43,
                47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101};//质数表对应a-z
        int i, j, hash_val, value = 0;
        for(i = 0; i < m; ++i) //计算模式串的hash值value
        {
            value += table[t[i]-'a'];
        }
        for(i = 0; i < n-m+1; ++i)//最多n-m+1次比较
        {
            hash_val = 0;
            for(j = i; j < m+i; ++j)//计算第i个子串的哈希值
            {
                hash_val += table[s[j]-'a'];
            }
            if(hash_val == value && same(str.substring(i, n).toCharArray(), t, m))
            {//如果子串哈希值等于模式串的，且"真的"字符串匹配（避免冲突带来的假匹配）
                return i+1;//返回匹配位置，第i位开始，i从1开始
            }
        }
        return 0;
    }

    public static void main(String[] args) {

        ViolentMatch violentMatch = new ViolentMatch();

        String str = "BBCABCDABABCDABCDABDE";
        String pattrn = "AB";

//        System.out.println(violentMatch.BF_match1(str, pattrn));
//
//        System.out.println(violentMatch.BF_match2(str, pattrn));

//        System.out.println(violentMatch.RK_match(str, pattrn));

    }

}
