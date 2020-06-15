package com.neusoft.CharMatch;

/**
 * @Title: KmpSearch
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2019/12/1214:58
 */
public class KmpSearch {

    /**
      * @Author: yisheng.wu
      * @Description TODO kmp查找
      * @Date 15:03 2019/12/12
      * @Param [str, pattern]
      * @return int
      **/
    public int match(String str, String pattern, int next[]){

        char[] s = str.toCharArray();
        char[] p = pattern.toCharArray();

        int i = 0;
        int j = 0;
        int sLen = s.length;
        int pLen = p.length;

        while (i < sLen && j < pLen)
        {
            //①如果j = -1，或者当前字符匹配成功（即S[i] == P[j]），都令i++，j++
            if (j == -1 || s[i] == p[j])
            {
                i++;
                j++;

            } else {
                //②如果j != -1，且当前字符匹配失败（即S[i] != P[j]），则令 i 不变，j = next[j]
                //next[j]即为j所对应的next值
                j = next[j];
            }
        }
        if (j == pLen)
            return i - j;
        else
            return -1;

    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 计算next数组
      * @Date 15:03 2019/12/12
      * @Param [p, next]
      * @return void
      **/
    void GetNext(char p[], int next[])
    {
        int pLen = p.length;
        next[0] = -1;
        int k = -1;
        int j = 0;
        while (j < pLen - 1)
        {
            //p[k]表示前缀，p[j]表示后缀
            if (k == -1 || p[j] == p[k])
            {
                ++k;
                ++j;
                next[j] = k;
            }
            else
            {
                k = next[k];
            }
        }
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO 优化过后的next 数组求法
      * @Date 17:16 2019/12/12
      * @Param [next]
      * @return void
      **/
    void GetNextval(char p[], int next[])
    {
        int pLen = p.length;
        next[0] = -1;
        int k = -1;
        int j = 0;
        while (j < pLen - 1)
        {
            //p[k]表示前缀，p[j]表示后缀
            if (k == -1 || p[j] == p[k])
            {
                ++j;
                ++k;
                //较之前next数组求法，改动在下面4行
                if (p[j] != p[k])
                    next[j] = k;   //之前只有这一行
                else
                    //因为不能出现p[j] = p[ next[j ]]，所以当出现时需要继续递归，k = next[k] = next[next[k]]
                    next[j] = next[k];
            }
            else
            {
                k = next[k];
            }
        }
    }

    public static void main(String[] args) {

        KmpSearch kmpSearch = new KmpSearch();

        String str = "BBC ABCDAB ABCABCDABDE";
        String pattrn = "ABCABC";

        int next1[] = new int[pattrn.length()];
        int next2[] = new int[pattrn.length()];
        kmpSearch.GetNext(pattrn.toCharArray(), next1);
        kmpSearch.GetNextval(pattrn.toCharArray(), next2);

        for(int o1 : next1){
            System.out.print(o1+" ");
        }
        System.out.println();

        for(int o2 : next2){
            System.out.print(o2+" ");
        }
        System.out.println();

        System.out.println(kmpSearch.match(str, pattrn, next1));

        System.out.println(kmpSearch.match(str, pattrn, next2));

    }

}
