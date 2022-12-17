package pgdp.pvm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PVMParser {

    private final Stream<String> lines;

    public PVMParser(String resource) throws URISyntaxException, IOException {
        this(Objects.requireNonNull(PVMParser.class.getClassLoader().getResource(resource)));
    }

    public PVMParser(URL file) throws URISyntaxException, IOException {
        this(file.toURI());
    }

    public PVMParser(URI file) throws IOException {
        this(Path.of(file));
    }

    public PVMParser(Path file) throws IOException {
        this(Files.lines(file));
    }

    public PVMParser(Stream<String> lines) {
        this.lines = lines;
    }

    public void run(IO io) {
        optimize().run(io);
    }

    public PVMExecutable optimize() {
        return verify().optimize();
    }

    public PVMOptimizer verify() {
        return split().verify();
    }

    public PVMVerifier split() {
        return parse().split();
    }

    public PVMSplitter parse() throws PVMError {
        try (var lines = Objects.requireNonNull(this.lines)) {
            Map<String, Label> label_by_name = new HashMap<>();
            Map<Integer, Label> label_by_instruction = new HashMap<>();
            final int[] currentInstruction = {0};
            //remove comments
            Instruction[] instructions = lines
                    .sequential()
                    .map(new Function<String, WithLine<String>>() {
                        int line = 0;

                        @Override
                        public WithLine<String> apply(String s) {
                            return new WithLine<>(++line, s);
                        }
                    })
                    .map(line -> {
                        int index = line.value.indexOf("//");
                        if (index != -1) {
                            line = new WithLine<>(line.line, line.value.substring(0, index));
                        }
                        return line;
                    })
                    .map(line -> new WithLine<>(line.line, Arrays.stream(line.value.split(" "))
                            .map(String::trim)
                            .filter(token -> !token.isEmpty()).toArray(String[]::new))
                    ).filter(tokens -> tokens.value.length != 0)
                    .map(tokens -> {
                        if (tokens.value.length == 1 && tokens.value[0].endsWith(":")) {
                            var name = tokens.value[0].substring(0, tokens.value[0].length() - 1);
                            var label = label_by_instruction.get(currentInstruction[0]);
                            if (label == null) {
                                label = new Label(currentInstruction[0]);
                                label_by_instruction.put(currentInstruction[0], label);
                            }
                            label.aliases.add(name);
                            var existing = label_by_name.get(name);
                            if (existing != null) {
                                for (String alias : existing.aliases) {
                                    label_by_name.remove(alias);
                                }
                                for (LabelInstruction caller : existing.callers) {
                                    caller.label = label;
                                }
                            }
                            label_by_name.put(name, label);
                            return null;
                        }
                        InstructionType instructionType;
                        try {
                            instructionType = InstructionType.valueOf(tokens.value[0].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new PVMError("Unknown instruction: " + tokens.value[0], tokens.line);
                        }
                        var instruction = switch (instructionType) {
                            case NEG, ADD, SUB, MUL, DIV, MOD, NOT, AND, OR, LESS, LEQ, EQ, NEQ, TRUE, FALSE, READ, WRITE, HALT, DUP, SWAP, POP -> {
                                if (tokens.value.length != 1) {
                                    throw new PVMError("Junk after instruction: " + tokens.value[1], tokens.line);
                                }
                                yield new Instruction(instructionType, tokens.line);
                            }
                            case LOAD, STORE, CONST, ALLOC -> {
                                if (tokens.value.length == 1) {
                                    throw new PVMError(
                                            "Missing integer immediate after " + tokens.value[0], tokens.line);
                                }
                                if (tokens.value.length != 2) {
                                    throw new PVMError("Junk after instruction: " + tokens.value[2], tokens.line);
                                }
                                try {
                                    var value = Integer.parseInt(tokens.value[1]);
                                    yield new IntInstruction(instructionType, tokens.line, value);
                                } catch (NumberFormatException e) {
                                    throw new PVMError("Not an integer: " + tokens.value[1], tokens.line);
                                }
                            }
                            case JUMP, FJUMP -> {
                                if (tokens.value.length == 1) {
                                    throw new PVMError("Missing jump target", tokens.line);
                                }
                                if (tokens.value.length != 2) {
                                    throw new PVMError("Junk after instruction: " + tokens.value[2], tokens.line);
                                }
                                try {
                                    var instr = Integer.parseInt(tokens.value[1]);
                                    var label = label_by_instruction.get(instr);
                                    if (label == null) {
                                        label = new Label(instr);
                                        label_by_instruction.put(instr, label);
                                    }
                                    var i = new LabelInstruction(instructionType, tokens.line, label);
                                    label.callers.add(i);
                                    yield i;

                                } catch (NumberFormatException e) {
                                    var label = label_by_name.get((tokens.value[1]));
                                    if (label == null) {
                                        label = new Label();
                                        label.aliases.add(tokens.value[1]);
                                        label_by_name.put(tokens.value[1], label);
                                    }
                                    var i = new LabelInstruction(instructionType, tokens.line, label);
                                    label.callers.add(i);
                                    yield i;
                                }

                            }
                        };
                        currentInstruction[0]++;
                        return instruction;
                    }).filter(Objects::nonNull).toArray(Instruction[]::new);
            List<String> unresolvedNames = new ArrayList<>();
            List<Integer> unresolvedIntegers = new ArrayList<>();
            boolean unresolved = false;
            for (Instruction instruction : instructions) {
                if (instruction instanceof LabelInstruction labelInstruction) {
                    if (labelInstruction.label.instruction == -1) {
                        unresolvedNames.addAll(labelInstruction.label.aliases);
                        unresolved = true;
                    }
                    if (labelInstruction.label.instruction < 0 ||
                            labelInstruction.label.instruction >= instructions.length) {
                        unresolvedIntegers.add(labelInstruction.label.instruction);
                        unresolved = true;
                    }
                }

            }
            if (unresolved) {
                throw new PVMError("Unresolved labels: "
                        + String.join(", ", unresolvedNames)
                        + System.lineSeparator()
                        + "Out of bounds instructions: " + unresolvedIntegers.stream().map(Object::toString).collect(
                        Collectors.joining(", ")));
            }
            int localVariables = 0;
            if (instructions.length != 0 && instructions[0].type == InstructionType.ALLOC) {
                localVariables = ((IntInstruction) instructions[0]).value;
                if (localVariables < 0) throw new PVMError("Negative nubers of local variables are impossible");
                //remove ALLOC
                instructions = Arrays.copyOfRange(instructions, 1, instructions.length);
                //adjust labels
                Arrays.stream(instructions).filter(i -> i instanceof LabelInstruction)
                        .map(i -> ((LabelInstruction) i).label)
                        .filter(l -> l.instruction != 0)
                        .distinct()
                        .forEach(l -> l.instruction--);
            }
            for (Instruction instruction : instructions) {
                if (instruction.type == InstructionType.ALLOC) {
                    throw new PVMError(
                            "There can only be one alloc instruction and it must be the very first instruction.");
                }
            }
            return new PVMSplitter(instructions, localVariables);
        }
    }

    record WithLine<T>(int line, T value) {
    }
}
