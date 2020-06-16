package com.neusoft.CharMatch;

/**
 * @Title: BKDRHash
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/6/1614:39
 */
public class BKDRHash {

    // BKDR Hash Function
    public static int BKDRHash(String str)
    {
        int seed = 131; // 31 131 1313 13131 131313 etc..
        int hash = 0;

        char[] chars = str.toCharArray();

        for(int i=0; i<chars.length; i++){
            hash = hash * seed + (chars[i]);
        }

        return (hash & 0x7FFFFFFF);
    }

    public static void main(String[] args) {

        String str = "master_1725";
        String str2 = "master_9527";

        int i = BKDRHash(str);
        System.out.println(i);

        i = BKDRHash(str2);
        System.out.println(i);

    }

}
