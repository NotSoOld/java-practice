package ru.notsoold.cardcv;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CardCvUtils {

    public static final List<String> cardsMapping = Arrays.asList("As", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "Js", "Qs", "Ks", "Ac", "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "10c", "Jc", "Qc", "Kc", "Ah", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "Jh", "Qh", "Kh", "Ad", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "10d", "Jd", "Qd", "Kd");
    public static final List<String> cardValuesMapping = Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K");
    public static final List<String> cardSuitsMapping = Arrays.asList("s", "c", "h", "d");

    private CardCvUtils() {}

    public static int rgbToHardBW(int rgb) {
        return ( (((rgb & 0xff0000) >> 16) > 120 ? 255 : 0) + (((rgb & 0xff00) >> 8) > 120 ? 255 : 0) + ((rgb & 0xff) > 120 ? 255 : 0)) / 3;
    }

    public static int getIndexOfMaxElement(double[] inputArray) {
        return IntStream.range(0, inputArray.length).reduce((i, j) -> inputArray[i] > inputArray[j] ? i : j).getAsInt();
    }
}
