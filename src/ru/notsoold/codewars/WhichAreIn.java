package ru.notsoold.codewars;

import java.util.Arrays;

// https://www.codewars.com/kata/550554fd08b86f84fe000a58
public class WhichAreIn {

    public static String[] inArray(String[] array1, String[] array2) {
        return Arrays.stream(array1)
                        .filter(s1 -> Arrays.stream(array2).anyMatch(s2 -> s2.contains(s1)))
                        .distinct()
                        .sorted()
                        .toArray(String[]::new);
        /*return Arrays.stream(array2)
                        .map(s2 -> a1.stream()
                                        .filter(s2::contains)
                                        .collect(Collectors.toList()))
                        .flatMap(Collection::stream)
                        .distinct()
                        .sorted()
                        .toArray(String[]::new);*/
    }

    public static void main(String[] args) {
        String a[] = new String[]{ "arp", "live", "strong" };
        String b[] = new String[] { "lively", "alive", "harp", "sharp", "armstrong" };
        String r[] = new String[] { "arp", "live", "strong" };
        WhichAreIn.inArray(a, b);
    }

}
