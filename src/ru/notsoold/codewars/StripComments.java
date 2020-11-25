package ru.notsoold.codewars;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// https://www.codewars.com/kata/51c8e37cee245da6b40000bd
public class StripComments {

    public static String stripComments(String text, String[] commentSymbols) {
        List<String> processedLines = new ArrayList<>();
        for (String line: text.split("\n")) {
            for (String commentSymbol : commentSymbols) {
                String regex = commentSymbol + ".*$";
                line = Pattern.compile(regex, Pattern.MULTILINE).matcher(line).replaceAll("")
                /*.stripTrailing()*/    // this will work in Java 11
                ;
            }
            processedLines.add(line);
        }
        return String.join("\n", processedLines);
    }

    public static void main(String[] args) {
        System.out.println("Expected: apples, pears\ngrapes\nbananas");
        System.out.println("Got:      " + StripComments.stripComments(
                        "apples, pears # and bananas\ngrapes\nbananas !apples", new String[] { "#", "!" }));

        System.out.println("Expected: a\nc\nd");
        System.out.println("Got:      " + StripComments.stripComments(
                        "a #b\nc\nd $e f g", new String[] { "#", "$" }));
    }
}
