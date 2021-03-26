package ru.notsoold.yaforensic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class YaForensicTask {

    private List<String> linesList;

    private final Map<String, List<String>> knownLetters = new HashMap<>();

    private int lineLength;

    public static void main(String[] args) throws IOException {
        YaForensicTask taskInstance = new YaForensicTask();
        String parsedStr = taskInstance.start();
        taskInstance.displayKnownLetters();
        System.out.println(parsedStr);
    }

    public String start() throws IOException {
        linesList = Files.lines(Paths.get("Тестовое задание.txt"))
                        .filter(str -> !str.isEmpty()).collect(Collectors.toList());
        lineLength = linesList.get(0).length();

        Scanner scanner = new Scanner(System.in);
        int i = 1;
        int lastCheckpoint = 0;
        StringBuilder resultingText = new StringBuilder();
        while (i < lineLength) {
            // Print the text from the last checkpoint to the current i value.
            // Also print some columns after the current i value (delimited by a vertical line).
            showColumns(lastCheckpoint, i);
            // Make a lookahead and try to recognize current letter automatically using already known letters.
            Optional<String> recognitionResultOpt = tryRecognizeNextLetter(lastCheckpoint, i, knownLetters);
            if (recognitionResultOpt.isPresent()) {
                String recognitionResult = recognitionResultOpt.get();
                resultingText.append(recognitionResult);
                // We're done with this letter, we can skip it completely.
                int letterLength = knownLetters.get(recognitionResult).get(0).length();
                lastCheckpoint += letterLength;
                i += letterLength;
                continue;
            }

            // Auto skip spaces.
            if (currentColumnIsSpace(lastCheckpoint)) {
                resultingText.append(' ');
                lastCheckpoint++;
                i++;
                continue;
            }

            String input = scanner.next();
            switch (input) {
            case "выход":
                return resultingText.toString();

            case "буква":
                // When we confirm that we have the whole letter (between lastCheckpoint and i).
                String letterDesc = scanner.next();
                if (!knownLetters.containsKey(letterDesc)) {
                    knownLetters.put(letterDesc, extractLetter(lastCheckpoint, i));
                }
                resultingText.append(letterDesc);
                lastCheckpoint = i;
                break;

            case "скип":
                if (currentColumnIsSpace(lastCheckpoint)) {
                    resultingText.append(' ');
                }
                lastCheckpoint++;
                break;

            case "назад":
                if (i > 0) {
                    i -= 2;
                }
                break;
            }
            i++;
        }
        return resultingText.toString();
    }

    private void showColumns(int from, int to) {
        // Math.min is to prevent errors when the columns' sequence is about to end.
        linesList.stream().map(line -> line.substring(from, Math.min(to, line.length()))
                                + "|" + line.substring(to, Math.min(to + 10, line.length())))
                        .forEach(System.out::println);
    }
    
    private List<String> extractLetter(int from, int to) {
        return linesList.stream().map(line -> line.substring(from, to)).collect(Collectors.toList());
    }

    public void displayKnownLetters() {
        System.out.println("All known letters:");
        knownLetters.forEach((letterDesc, letterRows) -> {
            System.out.println(letterDesc);
            letterRows.forEach(System.out::println);
            System.out.println();
        });
    }

    private boolean currentColumnIsSpace(int currentPosition) {
        return linesList.stream().map(line -> line.charAt(currentPosition)).allMatch(chr -> chr == ' ');
    }

    private Optional<String> tryRecognizeNextLetter(int startIndex, int currentPosition, Map<String, List<String>> knownLetters) {
        int i = 1;
        while (i < 10 && !currentColumnIsSpace(currentPosition) && currentPosition + i <= lineLength) {
            List<String> currentLetterLookahead = extractLetter(startIndex, currentPosition + i);
            for (Map.Entry<String, List<String>> letterEntry: knownLetters.entrySet()) {
                if (lettersAreEqual(letterEntry.getValue(), currentLetterLookahead)) {
                    return Optional.of(letterEntry.getKey());
                }
            }
            i++;
        }
        return Optional.empty();
    }

    private boolean lettersAreEqual(List<String> first, List<String> second) {
        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

}
