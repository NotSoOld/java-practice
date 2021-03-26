package ru.notsoold;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * В одной далекой стране правил крайне сумасбродный король, который больше всего на свете любил власть.
 * Ему подчинялось множество людей, но вот незадача, у его подчиненных тоже были свои слуги.
 * Король обезумел от мысли, что какой-нибудь дворянин или даже зажиточный холоп может иметь больше слуг, чем он сам.
 * И приказал всем людям на бумаге через запятую написать свое имя и имена своих прямых подчиненных.
 *
 * По результатам опроса король получил огромный список из имен (see "pollResults")
 *
 * У короля разболелась голова. Что с этими данными делать, король не знал и делегировал задачу невезучему слуге.

 * Помогите слуге правильно составить иерархию и подготовить  отчет для короля следующим образом:
 *
 * король
 *     дворянин Кузькин
 *         управляющий Семен Семеныч
 *             крестьянин Федя
 *             доярка Нюра
 *         жена Кузькина
 *         ...
 *     секретарь короля
 *         зажиточный холоп
 *         ...
 *     ...
 *
 * Помните:
 *  1. Те, у кого нет подчиненных, просто написали свое имя.
 *  2. Те, кого никто не указал как слугу, подчиняются напрямую королю (ну, пускай бедный король так думает).
 *  3. Итоговый список должен быть отсортирован в алфавитном порядке на каждом уровне иерархии.
 *
 */

public class LazyKing extends Vassal {
    private static final List<String> pollResults = Arrays.asList(
                    "служанка Аня",
                    "управляющий Семен Семеныч: крестьянин Федя, доярка Нюра",
                    "дворянин Кузькин: управляющий Семен Семеныч, жена Кузькина, экономка Лидия Федоровна",
                    "экономка Лидия Федоровна: дворник Гена, служанка Аня",
                    "доярка Нюра",
                    "кот Василий: человеческая особь Катя",
                    "дворник Гена: посыльный Тошка",
                    "киллер Гена",
                    "зажиточный холоп: крестьянка Таня",
                    "секретарь короля: зажиточный холоп, шпион Т",
                    "шпион Т: кучер Д",
                    "посыльный Тошка: кот Василий",
                    "аристократ Клаус",
                    "просветленный Антон"
    );

    public LazyKing() {
        super("король");
    }

    public static void main(String... args) {
        UnluckyVassal unluckyVassal = new UnluckyVassal();
        unluckyVassal.printReportForKing(pollResults);
    }
}

class UnluckyVassal {

    public void printReportForKing(List<String> pollResults) {
        // Найдем всех уникальных существующих вассалов по их именам, создадим объекты и соотнесем их с именами.
        // Такой маппинг понадобится нам позже.
        Map<String, Vassal> vassalsMapping = pollResults.stream()
                .map(pollLine -> {
                    String[] bits = pollLine.split(": ");
                    Vassal mainVassal = new Vassal(bits[0]);
                    if (bits.length == 1) {
                        return Collections.singletonList(mainVassal);
                    }
                    String[] descendants = bits[1].split(", ");
                    List<Vassal> descendantsVassals = Arrays.stream(descendants).map(Vassal::new).collect(Collectors.toList());
                    descendantsVassals.add(mainVassal);
                    return descendantsVassals;

                })
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(Vassal::getName, Function.identity(), (a, b) -> a));

        // Теперь соединим всех созданных вассалов в иерархию.
        // Дополнительно проставляется признак "находится в подчинении",
        // чтобы потом всех неподчиненных можно было легко подчинить королю.
        pollResults.forEach(pollLine -> {
            String[] bits = pollLine.split(": ");
            if (bits.length > 1) {
                Vassal mainVassal = vassalsMapping.get(bits[0]);
                List<Vassal> descendants = Arrays.stream(bits[1].split(", "))
                                .map(vassalsMapping::get)
                                .peek(Vassal::setHasMainVassal)
                                .collect(Collectors.toList());
                mainVassal.addDescendants(descendants);
            }
        });

        // Подчиняем оставшихся вассалов королю и рекурсивно печатаем иерархию.
        LazyKing lazyKing = new LazyKing();
        lazyKing.addDescendants(vassalsMapping.values().stream()
                        .filter(vassal -> !vassal.hasMainVassal()).collect(Collectors.toList()));
        System.out.println(lazyKing.toString(0));
    }
}

class Vassal implements Comparable<Vassal> {

    private final String name;
    private final Set<Vassal> descendants = new TreeSet<>();
    private boolean hasMainVassal;

    public Vassal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean hasMainVassal() {
        return hasMainVassal;
    }

    public void addDescendants(List<Vassal> vassals) {
        descendants.addAll(vassals);
    }

    public void setHasMainVassal() {
        this.hasMainVassal = true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vassal && ((Vassal)obj).name.equals(this.name);
    }

    @Override
    public int compareTo(Vassal other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name;
    }

    public String toString(int depth) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < depth * 4; i++) { ret.append(" "); }
        ret.append(name).append("\n").append(descendants.stream()
                                .map(descendant -> descendant.toString(depth + 1))
                                .collect(Collectors.joining()));
        return ret.toString();
    }
}