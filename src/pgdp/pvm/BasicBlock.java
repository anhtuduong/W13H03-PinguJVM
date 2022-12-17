package pgdp.pvm;

import java.util.*;
import java.util.stream.Collectors;

public class BasicBlock {
    public final int index;
    //source lines of incoming edges to this block
    private final List<Integer> inbounds = new ArrayList<>();
    public BasicBlock prev, next;
    private Instruction[] instructions;
    private Deque<Type> initialStack;
    private Type[] initialVars;
    private boolean reachable = false;

    private BasicBlock(Instruction[] instructions, int index) {
        this.instructions = instructions;
        this.index = index;
    }

    public BasicBlock(Instruction[] instructions) {
        this.instructions = instructions;
        index = 0;
    }

    public Instruction[] getInstructions() {
        return instructions;
    }

    public BasicBlock split(int index) {

        if (index < this.index || index >= this.index + instructions.length) {
            return find(index).split(index);
        }
        if (index == this.index) return this;
        var split = index - this.index;
        var second = new BasicBlock(Arrays.copyOfRange(instructions, split, instructions.length), index);
        instructions = Arrays.copyOf(instructions, split);
        second.next = next;
        next = second;
        second.prev = this;
        return second;
    }

    public BasicBlock find(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (index < this.index) {
            return prev.find(index);
        } else if (index >= this.index + instructions.length) {
            if (next == null) {
                throw new ArrayIndexOutOfBoundsException(index);
            } else {
                return next.find(index);
            }
        } else {
            return this;
        }
    }

    public Deque<Type> getInitialStack() {
        return initialStack;
    }

    public Type[] getInitalVars() {
        return initialVars;
    }

    public boolean isReachable() {
        return reachable;
    }

    /** Prüft, ob ein Branch zu diesem Block mit dieser Stack Belegung valide ist. Wenn nicht, wird ein PVMError geworfen.
     *  Gegebenenfalls wird die Belegung der lokalen Variablen den übergebenen Neuerungen angepasst.
     *  Es wird 'true' zurückgegeben, wenn dieser BasicBlock nochmal behandelt werden muss, weil sich die lokalen Variablen
     *  geändert haben, false, wenn nicht.
     *
     * @param stack Der Stack zum Zeitpunkt des Aufrufs
     * @param variables Die mit ALLOC angelegten lokalen Variablen zum Zeitpunkt des Aufrufs
     * @param from Die Instruktion zum Zeitpunkt des Aufrufs (nur für die eventuelle Error-Message)
     * @return 'true' gdw. dieser BasicBlock noch ein weiteres Mal behandelt werden muss, weil sich die lokalen
     *          Variablen (variables)
     */
    public boolean incoming(Deque<Type> stack, Type[] variables, Instruction from) {
        if (initialStack == null) {
            initialStack = new ArrayDeque<>(stack);
            initialVars = Arrays.copyOf(variables, variables.length);
            reachable = true;
            return true;
        } else {
            if (stack.size() != initialStack.size()) {
                throw new PVMError("Mismatch of stack size at " + instructions[0].type +
                        " on inbound edge from  "
                        + from.getType() + " at line " + from.line + ".\nExpected: " + initialStack.size() +
                        ", Actual: " + stack.size() + "\nall inbound edges: " +
                        inbounds.stream().map(Object::toString).collect(Collectors.joining(", ")),
                        instructions[0].line);
            }
            List<String> differences = new ArrayList<>();
            var stackIter = stack.iterator();
            var initialStackIter = initialStack.iterator();
            for (int i = 0; i < stack.size(); i++) {
                var stackType = stackIter.next();
                var initialStackType = initialStackIter.next();
                if (stackType != initialStackType) {
                    differences.add(
                            "Difference at stack position " + i + ": Expected: " + initialStackType + ", Actual: " +
                                    stackType);
                }
            }
            if (!differences.isEmpty()) {
                throw new PVMError("Stack type mismatch at " + instructions[0].type + "on inbound edge from "
                        + from.getType() + " at line " + from.line + ".\n" + String.join("\n", differences) +
                        "\nall inbound edges: " +
                        inbounds.stream().map(Object::toString).collect(Collectors.joining(", ")),
                        instructions[0].line);
            }

            boolean changed = false;
            for (int i = 0; i < variables.length; i++) {
                if (initialVars[i] != variables[i] && initialVars[i] != Type.UNDEFINED) {
                    changed = true;
                    initialVars[i] = Type.UNDEFINED;
                }
            }
            return changed;

        }
    }

    public List<Integer> getInbounds() {
        return inbounds;
    }
}
