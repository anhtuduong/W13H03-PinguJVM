package pgdp.pvm;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class PVMVerifier {
    private final BasicBlock entryPoint;

    private final int localVariables;

    public PVMVerifier(BasicBlock entryPoint, int localVariables) {
        this.entryPoint = entryPoint;
        this.localVariables = localVariables;
    }

    /** Methoden, die bereits mehrere Schritte der noch kommenden Pipeline auf einmal ausführen.
     */
    public void run(IO io) {
        optimize().run(io);
    }

    public PVMExecutable optimize() {
        return verify().optimize();
    }

    /** Diese Methode führt die Verifikation des von 'entryPoint' aus beschriebenen Codes aus.
     *  Dazu werden alle möglichen Abläufe des Bytecode-Programms (also unabhängig von den Nutzer-Eingaben)
     *  nur mit den jeweiligen Typen (INT, BOOLEAN oder UNDEFINED) auf dem Stack anstelle konkreter Werte simuliert.
     *
     *  Begonnen wird mit dem BasicBlock 'entryPoint' und einem Stack, auf dem 'localVariables' viele UNDEFINED-Felder
     *  liegen (also so viele, wie mit ALLOC-Befehlen am Anfang alloziert werden). Bei Verzweigungen (FJUMP) werden
     *  stets beide Zweige simuliert. Es wird solange simuliert, bis sich die Typen der mit ALLOC angelegten Felder
     *  nicht mehr von UNDEFINED auf etwas anderes ändern.
     *
     *  Wenn bei dieser Simulation irgendwann eine Unstimmigkeit auftreten sollte (wie z.B., dass ADD aufgerufen wird,
     *  ohne dass aktuell zwei INTs oben auf dem Stack liegen), so wird ein 'PVMError' geworfen. Sollte ein Befehl
     *  an einer Stelle angetroffen werden, wo dieser nicht auftauchen darf, so wird eine 'IllegalStateException'
     *  geworfen.
     *
     * @return Ein PVMOptimizer-Objekt mit der Liste an (in verify() evtl. modifizierten) BasicBlocks,
     *          welches den nächsten Schritt in der Simulator-Pipeline beschreibt.
     */
    // Hinweis: PVMError und IllegalStateExceptions sind Runtime-Exceptions. Es ist also nicht unbedingt nötig, sie hier anzugeben.
    public PVMOptimizer verify() throws PVMError, IllegalStateException {
        // Deque mit allen noch zu durchlaufenden BasicBlocks
        Deque<BasicBlock> toVerify = new ArrayDeque<>();

        // Begonnen wird mit der Simulation beim im Konstruktor übergebenen 'entryPoint'
        toVerify.add(entryPoint);

        // Simulation der im Parser weggeschnittenen ALLOCs:
        // Setzt das initiale Array von 'entryPoint' auf ein leeres und die initialen Variablen auf 'undefinedLocals'
        Type[] undefinedLocals = new Type[localVariables];
        Arrays.fill(undefinedLocals, Type.UNDEFINED);
        entryPoint.incoming(new ArrayDeque<>(), undefinedLocals, null);

        // Merkt sich die maximal verwendete Stack-Höhe bei der Simulation
        int maxStack = 0;

        // Solange es noch zu durchlaufende BasicBlocks gibt, nimmt jeweils den obersten und simuliere ihn
        while (!toVerify.isEmpty()) {
            // Nimm den ersten BasicBlock in 'toVerify'
            BasicBlock block = toVerify.pop();
            // Initialisiere den Stack, mit dem simuliert werden soll, auf den bei dem Block gespeicherten Anfangsstack
            ArrayDeque<Type> stack = new ArrayDeque<>(block.getInitialStack());
            // Initialisiere die Typen der Variablen aus dem, was im aktuellen Block gespeichert ist
            Type[] variables = Arrays.copyOf(block.getInitalVars(), localVariables);
            // Lade die Instruktionen aus dem aktuellen Block
            Instruction[] instructions = block.getInstructions();

            // Verifiziere alle Instruktionen, indem deren Effekt auf den Stack allein mit Typen
            // anstelle von konkreten Werten simuliert wird.
            for (int i = 0; i < instructions.length; i++) {
                Instruction instruction = instructions[i];
                switch (instruction.getType()) {
                    // Arithmetische Operationen
                    case ADD, SUB, MUL, DIV, MOD -> {
                        checkSize(2, stack, instruction);
                        tryPop(Type.INT, stack, instruction);
                        tryPeek(Type.INT, stack, instruction);
                    }
                    case NEG -> {
                        // TODO
                        throw new RuntimeException("Not yet implemented!");
                    }

                    // Boolesche Operationen
                    case OR, AND -> {
                        // TODO
                        throw new RuntimeException("case OR, AND not yet implemented!");
                    }
                    case NOT -> {
                        // TODO
                        throw new RuntimeException("case NOT not yet implemented!");
                    }

                    // Vergleichs-Operationen
                    case EQ, LEQ, LESS, NEQ -> {
                        // TODO
                        throw new RuntimeException("case EQ, LEQ, LESS, NEQ not yet implemented!");
                    }

                    // IO-Befehle
                    case READ -> {
                        // TODO
                        throw new RuntimeException("case READ not yet implemented!");
                    }
                    case WRITE -> {
                        // TODO
                        throw new RuntimeException("case WRITE not yet implemented!");
                    }

                    // Konstanten
                    case CONST -> {
                        // TODO
                        throw new RuntimeException("case CONST not yet implemented!");
                    }
                    case TRUE, FALSE -> {
                        // TODO
                        throw new RuntimeException("case TRUE, FALSE not yet implemented!");
                    }

                    // Laden/Speichern
                    case LOAD -> {
                        // TODO
                        throw new RuntimeException("case LOAD not yet implemented!");
                    }
                    case STORE -> {
                        // TODO
                        throw new RuntimeException("case STORE not yet implemented!");
                    }

                    // Stack-Befehle
                    case POP -> {
                        // TODO
                        throw new RuntimeException("case POP not yet implemented!");
                    }
                    case DUP -> {
                        // TODO
                        throw new RuntimeException("case DUP not yet implemented!");
                    }
                    case SWAP -> {
                        // TODO
                        throw new RuntimeException("case SWAP not yet implemented!");
                    }

                    // Kontrollfluss
                    case HALT, JUMP -> {
                        // TODO
                        throw new RuntimeException("case HALT, JUMP not yet implemented!");
                    }
                    case FJUMP -> {
                        // TODO
                        throw new RuntimeException("case FJUMP not yet implemented!");
                    }

                    // Allokation
                    case ALLOC -> {
                        // TODO
                        throw new RuntimeException("case ALLOC not yet implemented!");
                    }
                }

                // Wenn der Stack über die bisher maximale Höhe hinausgewachsen ist, merke dir dies.
                maxStack = Math.max(maxStack, stack.size());
            }

            // Füge noch für die letzte Instruktion des Blocks den/die möglichen Nachfolger hinzu,
            // wenn für diese die Methode 'incoming()' jeweils noch 'true' zurückgibt.
            Instruction end = instructions[instructions.length - 1];
            switch (end.type) {
                case HALT -> {
                    // Nichts zu prüfen/tun bei HALT. Nachfolger gibt es keine, da das Programm hier angehalten wird.
                }
                case JUMP -> {
                    BlockInstruction jump = (BlockInstruction) end;
                    if (jump.target.incoming(stack, variables, end)) {
                        toVerify.push(jump.target);
                    }
                }
                case FJUMP -> {
                    // TODO
                    throw new RuntimeException("case FJUMP not yet implemented!");
                }
                default -> {
                    // TODO
                    throw new RuntimeException("default not yet implemented!");
                }
            }
        }

        // Gib den entsprechenden PVMOptimizer als das Objekt, das den nächsten Schritt in der Pipeline beschreibt, zurück.
        return new PVMOptimizer(entryPoint, localVariables, maxStack);
    }

    // ========================================================================================= //
    // -------------------------------- Nützliche Hilfsmethoden -------------------------------- //
    // ========================================================================================= //

    /** Wirft einen PVMError, wenn der übergebene Stack nicht die übergebene Minimalhöhe hat.
     *
     * @param min Minimale geforderte Stack-Höhe
     * @param stack Der aktuelle Stack von Typen
     * @param instruction Die zu testende Instruktion (nur für die eventuelle Error-Message)
     */
    private void checkSize(int min, Deque<Type> stack, Instruction instruction) {
        if (stack.size() < min) {
            throw new PVMError("Not enough variables on the stack for " + instruction.type, instruction.line);
        }
    }

    /** Nimmt das oberste Element vom übergebenen Stack.
     *  Wenn dieses nicht dem übergebenen erwarteten Typen entspricht, wird ein PVMError geworfen.
     *
     * @param expected Erwarteter Typ des obersten Stack-Elements
     * @param stack Der aktuelle Stack von Typen
     * @param instruction Die zu testende Instruktion (nur für die eventuelle Error-Message)
     */
    private void tryPop(Type expected, Deque<Type> stack, Instruction instruction) {
        if (stack.pop() != expected) {
            throw new PVMError("First Argument must be " + expected + " at " + instruction.type, instruction.line);
        }
    }

    /** Sieht sich das oberste Element vom übergebenen Stack an, ohne es von diesem herunterzunehmen.
     *  Wenn das Element nicht dem übergebenen erwarteten Typen entspricht, wird ein PVMError geworfen.
     *
     * @param expected Erwarteter Typ des obersten Stack-Elements
     * @param stack Der aktuelle Stack von Typen
     * @param instruction Die zu testende Instruktion (nur für die eventuelle Error-Message)
     */
    private void tryPeek(Type expected, Deque<Type> stack, Instruction instruction) {
        if (stack.peek() != expected) {
            throw new PVMError("First Argument must be " + expected + " at " + instruction.type, instruction.line);
        }
    }
}
