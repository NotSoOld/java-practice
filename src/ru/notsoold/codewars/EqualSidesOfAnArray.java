package ru.notsoold.codewars;

import java.util.stream.IntStream;

// https://www.codewars.com/kata/5679aa472b8f57fb8c000047
public class EqualSidesOfAnArray {

    public static int findEvenIndex(int[] arr) {
        int leftSum = 0;
        int rightSum = IntStream.of(arr).sum();
        for (int i = 0; i < arr.length; i++) {
            if (leftSum == rightSum) {
                return i;
            }
            leftSum += arr[i];
            if (leftSum == rightSum) {
                return i;
            }
            rightSum -= arr[i];
        }
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(findEvenIndex(new int[] { 1, 2, 3, 4, 3, 2, 1 }));
    }

}
