package ru.notsoold.codewars;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

// https://www.codewars.com/kata/52b7ed099cdc285c300001cd
public class Interval {

    public static int sumIntervals(int[][] intervals) {
        if (intervals == null || intervals.length == 0) {
            return 0;
        }
        // This is so dumb and does not take any perfomance issues into account.
        // But it works!
        return (int)Arrays.stream(intervals)
                        .flatMapToInt(interval -> IntStream.range(interval[0], interval[1]))
                        .distinct().count();
    }

    public static int sumIntervals1(int[][] intervals) {
        // How it should be done (of the solutions from the Codewars).
        // The problem with joined intervals is so common I would like to leave
        // this solution here for future references.
        if (intervals == null || intervals.length == 0 || intervals[0].length == 0) {
            return 0;
        }
        java.util.Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
        int sum = 0, min = intervals[0][0], max = intervals[0][1];
        for (int[] interval : intervals) {
            if (min < interval[0] && max >= interval[0]) {
                if (max < interval[1]) {
                    max = interval[1];
                }
            } else if (max < interval[0]) {
                sum += (max - min);
                min = interval[0];
                max = interval[1];
            }
        }
        sum += (max - min);
        return sum;
    }


    public static void main(String[] args) {
        shouldHandleNullOrEmptyIntervals();
        shouldAddDisjoinedIntervals();
        shouldAddAdjacentIntervals();
        shouldAddOverlappingIntervals();
        shouldHandleMixedIntervals();
    }

    public static void shouldHandleNullOrEmptyIntervals() {
        System.out.println(0 + " : " + sumIntervals(null));
        System.out.println(0 + " : " + sumIntervals(new int[][]{}));
        System.out.println(0 + " : " + sumIntervals(new int[][]{{4, 4}, {6, 6}, {8, 8}}));
    }

    public static void shouldAddDisjoinedIntervals() {
        System.out.println(9 + " : " + sumIntervals(new int[][]{{1, 2}, {6, 10}, {11, 15}}));
        System.out.println(11 + " : " + sumIntervals(new int[][]{{4, 8}, {9, 10}, {15, 21}}));
        System.out.println(7 + " : " + sumIntervals(new int[][]{{-1, 4}, {-5, -3}}));
        System.out.println(78 + " : " + sumIntervals(new int[][]{{-245, -218}, {-194, -179}, {-155, -119}}));
    }

    public static void shouldAddAdjacentIntervals() {
        System.out.println(54 + " : " + sumIntervals(new int[][]{{1, 2}, {2, 6}, {6, 55}}));
        System.out.println(23 + " : " + sumIntervals(new int[][]{{-2, -1}, {-1, 0}, {0, 21}}));
    }

    public static void shouldAddOverlappingIntervals() {
        System.out.println(7 + " : " + sumIntervals(new int[][]{{1, 4}, {7, 10}, {3, 5}}));
        System.out.println(6 + " : " + sumIntervals(new int[][]{{5, 8}, {3, 6}, {1, 2}}));
        System.out.println(19 + " : " + sumIntervals(new int[][]{{1, 5}, {10, 20}, {1, 6}, {16, 19}, {5, 11}}));
    }

    public static void shouldHandleMixedIntervals() {
        System.out.println(13 + " : " + sumIntervals(new int[][]{{2, 5}, {-1, 2}, {-40, -35}, {6, 8}}));
        System.out.println(1234 + " : " + sumIntervals(new int[][]{{-7, 8}, {-2, 10}, {5, 15}, {2000, 3150}, {-5400, -5338}}));
        System.out.println(158 + " : " + sumIntervals(new int[][]{{-101, 24}, {-35, 27}, {27, 53}, {-105, 20}, {-36, 26},}));
    }

}
