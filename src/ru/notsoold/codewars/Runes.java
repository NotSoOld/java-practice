package ru.notsoold.codewars;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://www.codewars.com/kata/546d15cebed2e10334000ed9
public class Runes {

    public static int solveExpression(final String expression) {
        Matcher numbersMatcher = Pattern.compile("(-?[0-9?]+)([+\\-*]?)(-?[0-9?]+)=(-?[0-9?]+)").matcher(expression);
        numbersMatcher.find();
        String firstNumber = numbersMatcher.group(1);
        String operation = numbersMatcher.group(2);
        String secondNumber = numbersMatcher.group(3);
        String resultNumber = numbersMatcher.group(4);

        List<Integer> digits = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        firstNumber.chars().forEach(ch -> digits.remove((Integer)(ch - 0x30)));
        secondNumber.chars().forEach(ch -> digits.remove((Integer)(ch - 0x30)));
        resultNumber.chars().forEach(ch -> digits.remove((Integer)(ch - 0x30)));

        // Special check for i = 0.
        if (digits.contains(0)) {
            Pattern suitableForZero = Pattern.compile("-?[0-9][0-9?]*|-?\\?");
            if (suitableForZero.matcher(firstNumber).matches()
                            && suitableForZero.matcher(secondNumber).matches()
                            && suitableForZero.matcher(resultNumber).matches()) {
                if (check(0, firstNumber, secondNumber, resultNumber, operation)) {
                    return 0;
                }
            }
            digits.remove((Integer)0);
        }

        for (Integer digit: digits) {
            if (check(digit, firstNumber, secondNumber, resultNumber, operation)) {
                return digit;
            }
        }
        return -1;
    }

    private static boolean check(int i, String firstNumber, String secondNumber, String resultNumber, String operation) {
        int firstNumberTemp = Integer.parseInt(firstNumber.replace("?", i+""));
        int secondNumberTemp = Integer.parseInt(secondNumber.replace("?", i+""));
        int resultNumberTemp = Integer.parseInt(resultNumber.replace("?", i+""));
        switch (operation) {
            case "+": return resultNumberTemp == firstNumberTemp + secondNumberTemp;
            case "-": return resultNumberTemp == firstNumberTemp - secondNumberTemp;
            case "*": return resultNumberTemp == firstNumberTemp * secondNumberTemp;
            default: return false;
        }
    }


    public static void main(String[] args) {
        System.out.println("'1+1=?' " + "2 " + Runes.solveExpression("1+1=?"));
        System.out.println("'123*45?=5?088' " + "6 " + Runes.solveExpression("123*45?=5?088"));
        System.out.println("'-5?*-1=5?' " + "0 " + Runes.solveExpression("-5?*-1=5?"));
        System.out.println("'19--45=5?' " + "-1 " + Runes.solveExpression("19--45=5?"));
        System.out.println("'??*??=302?' " + "5 " + Runes.solveExpression("??*??=302?"));
        System.out.println("'?*11=??' " + "2 " + Runes.solveExpression("?*11=??"));
        System.out.println("'??*1=??' " + "2 " + Runes.solveExpression("??*1=??"));
        System.out.println("'??+??=??' " + "-1 " + Runes.solveExpression("??+??=??"));
        System.out.println("'123?45*?=?' " + "0 " + Runes.solveExpression("123?45*?=?"));
        System.out.println("'123?45+?=123?45' " + "0 " + Runes.solveExpression("123?45+?=123?45"));
        System.out.println("'123?45-?=123?45' " + "0 " + Runes.solveExpression("123?45-?=123?45"));
    }
    
}
