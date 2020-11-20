package ru.notsoold;

import java.util.*;
import java.util.stream.Collectors;

public class TestCase {

    /**
     * Streams API version.
     */
    private static void groupFilterAndPrintWithStreams(String inputStr) {
	Map<Character, List<String>> groupedWords = Arrays.stream(inputStr.split(" "))
		// Sort all the input words in the specified order.
		.sorted((str1, str2) -> {
		    int lenDiff = str2.length() - str1.length();
		    if (lenDiff != 0) {
			return lenDiff;
		    }
		    return str1.compareTo(str2);
		})
		// Group words by its first letter in the TreeMap to make keys be sorted as well.
		.collect(Collectors.groupingBy(
			s -> s.charAt(0),
			TreeMap::new,
			Collectors.mapping(s -> s, Collectors.toList())
		));

	// Filter and print groups.
	groupedWords.entrySet().stream()
		.filter(entry -> entry.getValue().size() > 1)
		.forEach(System.out::println);
    }

    /**
     * The way to do the job in Java 7 (without new methods / streams / lambdas).
     */
    private static void groupFilterAndPrintTheOldWay(String inputStr) {
        String[] words = inputStr.split(" ");
        Map<Character, List<String>> groupedWords = new HashMap<>();
        // Group words by its first letter.
        for (String word: words) {
            if (!groupedWords.containsKey(word.charAt(0))) {
                groupedWords.put(word.charAt(0), new ArrayList<>());
	    }
	    // Or use putIfAbsent:
	    // groupedWords.putIfAbsent(word.charAt(0), new ArrayList<>());

            groupedWords.get(word.charAt(0)).add(word);

	    // Could use getOrDefault from Java 8 as well:
	    // List<String> alphabeticGroup = groupedWords.getOrDefault(word.charAt(0), new ArrayList<>());
            // alphabeticGroup.add(word);
	    // groupedWords.put(word.charAt(0), alphabeticGroup);

	    // Or just do merge():
	    // groupedWords.merge(word.charAt(0), new ArrayList<String>() {{ add(word); }},
	    //			(existingWordsList, listAddition) -> { existingWordsList.addAll(listAddition); return existingWordsList; });

	}

	Map<Character, List<String>> groupedWordsFiltered = new TreeMap<>();
        // Now we can filter groups which size is 1, and sort the words in remaining groups.
	// Keys sorting will be performed automatically as we use TreeMap.
        for (Map.Entry<Character, List<String>> wordsGroup: groupedWords.entrySet()) {
            if (wordsGroup.getValue().size() > 1) {
		wordsGroup.getValue().sort(new Comparator<String>() {		// or just use a lambda as in Streams example
		    @Override public int compare(String str1, String str2) {
			int lenDiff = str2.length() - str1.length();
			if (lenDiff != 0) {
			    return lenDiff;
			}
			return str1.compareTo(str2);
		    }
		});
                groupedWordsFiltered.put(wordsGroup.getKey(), wordsGroup.getValue());
	    }
	}

	// Print the data.
	System.out.println(groupedWordsFiltered);

    }

    public static void main(String[] args) {
	String inputStr = "они сарай мелочь сапог она арбуз ли ливень дочь болт бокс дерево биржа онтология";

	groupFilterAndPrintWithStreams(inputStr);
	groupFilterAndPrintTheOldWay(inputStr);

    }

}
