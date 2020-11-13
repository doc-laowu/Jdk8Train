package com.neusoft.CharMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Title: Solution
 * @ProjectName Jdk8Train
 * @Description: TODO
 * @Author yisheng.wu
 * @Date 2020/9/2310:13
 */
public class Solution {

    public static void main(String[] args) {

        String s = "pwwkew";
        int i = new Solution().lengthOfLongestSubstring(s);
        System.out.println(i);

    }

    /**
     * 寻找两个正序数组的中位数
     * @param num1
     * @param num2
     * @return
     */
    public double median(int[] num1, int[] num2){
        int index1 = 0, index2 = 0;
        int len1=num1.length, len2=num2.length;
        int totalLen = num1.length + num2.length;
        int k = totalLen/2 + 1;
        ArrayList<Integer> integers = new ArrayList<>(totalLen/2);

        while(index1 < len1 || index2 < len2 || integers.size() < k){

            if(num1[index1] < num2[index2]){
                integers.add(num1[index1]);
                index1++;
            }else{
                integers.add(num2[index1]);
                index2++;
            }

        }

        return 0;
    }

    /**
     * 无重复字符的最长子串
     * @param s
     * @return
     */
    public int lengthOfLongestSubstring(String s) {

        Map<Character, Integer> map = new HashMap<>();
        int ans = 0;
        for(int i=0; i<s.length(); i++){
            Integer index = map.get(s.charAt(i));
            if(index != null){
                // 删除重复字符之前所有的字符
                Iterator<Map.Entry<Character, Integer>> iterator = map.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<Character, Integer> next = iterator.next();
                    if(next.getValue() <= index.intValue()){
                        iterator.remove();
                    }
                }
                map.put(s.charAt(i), i);
            }else{
                map.put(s.charAt(i), i);
            }
            ans = Math.max(ans, map.size());
        }

        return ans;
    }



    public static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) { val = x; }
    }

    /**
     *  两数相加
     * @param l1
     * @param l2
     * @return
     */
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {

        ListNode result = new ListNode(-1);
        ListNode p = result;

        ListNode p1 = l1;
        ListNode p2 = l2;
        int carry = 0;
        int ret = 0;
        int val1 = 0;
        int val2 = 0;
        while(null != p1 || null != p2){
            val1 = null != p1 ? p1.val : 0;
            val2 = null != p2 ? p2.val : 0;
            ret = val1 + val2 + carry;
            if(ret >= 10){
                ret = ret % 10;
                p.next = new ListNode(ret);
                carry = 1;
            }else{
                p.next = new ListNode(ret);
                carry = 0;
            }

            if(null != p1) p1 = p1.next;
            if(null != p2) p2 = p2.next;
            p = p.next;
        }

        if(carry > 0){
            p.next = new ListNode(carry);
        }

        result = result.next;

        return result;
    }

    /**
     * 两数之和
     * @param nums
     * @param target
     * @return
     */
    public int[] twoSum(int[] nums, int target) {

        Map<Integer, Integer> map = new HashMap<>();
        int[] ret = new int[2];

        for(int i=0; i< nums.length; i++){
            Integer integer = map.get(target - nums[i]);
            if(integer != null){
                ret[1] = i;
                ret[0] = integer;
                return ret;
            }else{
                map.put(nums[i], i);
            }
        }

        return ret;
    }

}
