package ru.notsoold.codewars;

import java.util.*;

// https://www.codewars.com/kata/58e24788e24ddee28e000053
public class SimpleAssembler {

    public static Map<String, Integer> interpret(String[] program) {
        List<AsmOperation> commands = new ArrayList<>();
        for (String programLine: program) {
            String[] opcodeAndOperands = programLine.split(" ");
            String opcode = opcodeAndOperands[0];
            String[] operands = Arrays.stream(opcodeAndOperands).skip(1).toArray(String[]::new);
            commands.add(AsmOpFactory.getOp(opcode, operands));
        }
        return new AssemblerVM(commands).execute();
    }

    public static void main(String[] args) {
        new SimpleAssembler().simple_1();
        new SimpleAssembler().simple_2();
    }

    public void simple_1() {
        String[] program = new String[]{"mov a 5","inc a","dec a","dec a","jnz a -1","inc a"};
        Map<String, Integer> out = new HashMap<>();
        out.put("a", 1);
        System.out.println(SimpleAssembler.interpret(program));
        System.out.println(out);
    }

    public void simple_2() {
        String[] program = new String[]{"mov a -10","mov b a","inc a","dec b","jnz a -2"};
        Map<String, Integer> out = new HashMap<>();
        out.put("a", 0);
        out.put("b", -20);
        System.out.println(SimpleAssembler.interpret(program));
        System.out.println(out);
    }
}

class AssemblerVM {

    private Map<String, Integer> registers = new HashMap<>();
    private List<AsmOperation> commands;

    public AssemblerVM(List<AsmOperation> commands) {
        this.commands = new ArrayList<>(commands);
    }

    public Map<String, Integer> execute() {
        int cmdCounter = 0;
        while (cmdCounter < commands.size()) {
            int optionalTransition = commands.get(cmdCounter).execute(registers);
            if (optionalTransition != 0) {
                cmdCounter += optionalTransition;
                continue;
            }
            cmdCounter++;
        }
        return new HashMap<>(registers);
    }
}


class AsmOpFactory {

    static AsmOperation getOp(String opcode, String... operands) {
        switch (opcode) {
            case Mov.OP: return new Mov(operands);
            case Inc.OP: return new Inc(operands);
            case Dec.OP: return new Dec(operands);
            case Jnz.OP: return new Jnz(operands);
            default: throw new AssertionError("Unknown opcode " + opcode);
        }
    }

}

interface AsmOperation {

    int execute(Map<String, Integer> context);

    default int parseOrGetFromContext(String strToParse, Map<String, Integer> context) {
        try {
            return Integer.parseInt(strToParse);

        } catch (NumberFormatException ignored) {
            if (!context.containsKey(strToParse)) {
                throw new RuntimeException(strToParse + " is not in the context");
            }
            return context.get(strToParse);
        }
    }
}

class Mov implements AsmOperation {

    public static final String OP = "mov";
    private String dest;
    private String src;

    public Mov(String... operands) {
        if (operands.length != 2) {
            throw new IllegalArgumentException("Must be 2 args");
        }
        this.dest = operands[0];
        this.src = operands[1];
    }

    @Override
    public int execute(Map<String, Integer> context) {
        int srcValue = parseOrGetFromContext(src, context);
        context.put(dest, srcValue);
        return 0;
    }

}

class Inc implements AsmOperation {

    public static final String OP = "inc";
    private String dest;

    public Inc(String... operands) {
        if (operands.length != 1) {
            throw new IllegalArgumentException("Must be 1 arg");
        }
        this.dest = operands[0];
    }

    @Override
    public int execute(Map<String, Integer> context) {
        if (!context.containsKey(dest)) {
            throw new RuntimeException("Operation inc " + dest + " : 'dest' is not in the context.");
        }
        int newValue = context.get(dest) + 1;
        context.put(dest, newValue);
        return 0;
    }
}

class Dec implements AsmOperation {

    public static final String OP = "dec";
    private String dest;

    public Dec(String... operands) {
        if (operands.length != 1) {
            throw new IllegalArgumentException("Must be 1 arg");
        }
        this.dest = operands[0];
    }

    @Override
    public int execute(Map<String, Integer> context) {
        if (!context.containsKey(dest)) {
            throw new RuntimeException("Operation dec " + dest + " : 'dest' is not in the context.");
        }
        int newValue = context.get(dest) - 1;
        context.put(dest, newValue);
        return 0;
    }

}

class Jnz implements AsmOperation {

    public static final String OP = "jnz";
    private String valueToTest;
    private String jumpAmount;

    public Jnz(String... operands) {
        if (operands.length != 2) {
            throw new IllegalArgumentException("Must be 2 args");
        }
        this.valueToTest = operands[0];
        this.jumpAmount = operands[1];
    }

    @Override
    public int execute(Map<String, Integer> context) {
        int parsedValueToTest = parseOrGetFromContext(valueToTest, context);
        if (parsedValueToTest != 0) {
            return parseOrGetFromContext(jumpAmount, context);
        }
        return 0;
    }
}
