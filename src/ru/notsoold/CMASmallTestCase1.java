package ru.notsoold;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Имеется корневая папка. В этой папке могут находиться текстовые файлы,
 * а также другие папки. В других папках также могут находится текстовые
 * файлы и папки (уровень вложенности может оказаться любым).
 * Найти все текстовые файлы, отсортировать их по имени и склеить
 * содержимое в один текстовый файл.
 */

public class CMASmallTestCase1 {

    public static void main(String[] args) throws IOException {
        Path resultPath = Files.createFile(Paths.get("result.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultPath.toFile()));

        Files.find(Paths.get("cmaSmallTestCase1RootFolder"), Integer.MAX_VALUE,
                (path, fileAttrs) -> fileAttrs.isRegularFile() && path.getFileName().toString().endsWith(".txt"))
            .sorted(Comparator.comparing(Path::getFileName))
            .flatMap(CheckedFunction.wrapFunction(Files::lines))
            .forEach(CheckedConsumer.wrapConsumer(writer::append));
        writer.close();

        Files.lines(resultPath).forEach(System.out::println);
    }
}

interface CheckedFunction<T, R> {

    R apply(T t) throws Exception;

    static <T, R> Function<T, R> wrapFunction(CheckedFunction<T, R> func) {
        return t -> {
            try {
                return func.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

interface CheckedConsumer<T> {

    void accept(T t) throws Exception;

    static <T> Consumer<T> wrapConsumer(CheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
