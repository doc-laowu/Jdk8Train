package com.neusoft.CharMatch;

/**
 * @Title: ArrayFindValue
 * @ProjectName Jdk8Train
 * @Description: TODO 在一个二维数组中（每个一维数组的长度相同），每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。
 * @Author yisheng.wu
 * @Date 2020/5/2015:00
 */
public class ArrayFindValue {

    public static boolean findValue(int[][] array, int target){

        int row = array.length;
        int col = array[0].length;
        if (row == 0 || col == 0)
            return false;
        if (target < array[0][0] || target > array[row - 1][col - 1])
            return false;
        int i = 0;
        int j = col - 1;
        while (i < row && j >= 0){
            if (array[i][j] > target)
            {
                j--;
            }
            else if (array[i][j] < target)
            {
                i++;
            }
            else
            {
                return true;
            }
        }

        return  false;
    }

    public static void main(String[] args) {

        int [][] arr = {
                {1,2,3,4,5},
                {2,3,4,5,6},
                {3,4,5,6,7},
                {4,5,6,7,8},
                {5,6,7,8,9}
        };

        boolean value = findValue(arr, 6);

        System.out.println(value);

    }

}
