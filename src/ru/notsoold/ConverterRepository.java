package ru.notsoold;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConverterRepository {

    private static volatile ConverterRepository instance;
    private static final Map<Class<?>, Map<Object, Function<?, ?>>> repository = new HashMap<>();

    public static ConverterRepository getInstance() {
        if (instance == null) {
            synchronized (ConverterRepository.class) {
                if (instance == null) {
                    instance = new ConverterRepository();
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T, R> Function<T, R> getConverter(Class<T> from, Class<R> to) {
        return (Function<T, R>)repository.getOrDefault(from, new HashMap<>()).get(to);
    }

    public <T, R> ConverterRepository addConverter(Function<T, R> converter, Class<T> from, Class<R> to) {
        repository.computeIfAbsent(from, (k) -> new HashMap<>()).put(to, converter);
        return this;
    }

}



abstract class MyClass {

    public static void main(String[] args) {
        ConverterRepository repository = ConverterRepository.getInstance();
        System.out.println(repository.addConverter(Date::toString, Date.class, String.class).getConverter(Date.class, String.class).apply(new Date()));
    }
}

