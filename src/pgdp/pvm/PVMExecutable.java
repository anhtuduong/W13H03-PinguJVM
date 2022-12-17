package pgdp.pvm;

public class PVMExecutable {

    private final BasicBlock entryPoint;
    private final int localVariables;
    private final int maxStack;

    public PVMExecutable(BasicBlock entryPoint, int localVariables, int maxStack) {
        this.entryPoint = entryPoint;
        this.localVariables = localVariables;
        this.maxStack = maxStack;
    }

    /** Simuliert das nun verifizierte und optimierte Bytecode-Programm.
     *  Wie genau die Nutzereingaben und die Ausgaben funktionieren sollen, wird dabei durch das übergebene
     *  Objekt 'io' beschrieben (io.read() für eine Eingabe, io.write für eine Ausgabe).
     *
     * @param io Implementierung von IO für Eingaben (READ) und Ausgaben (WRITE).
     */
    public void run(IO io) {
        // Beginne mit der Simulation bei dem 'entryPoint'
        BasicBlock currentBlock = entryPoint;
        // Im Verifikationsschritt wurde festgehalten, auf welche Größe der Stack maximal wachsen kann.
        // Lege nun genau so viele Felder auf dem Stack an.
        int[] stack = new int[maxStack];
        // Lege zudem ausreichend Platz für die Variablen an (Simulation von ALLOC).
        int[] variables = new int[localVariables];
        // Speichere stets, an welchem Index in 'stack' das aktuell oberste Stack-Element liegt.
        // Da anfangs noch nichts auf dem Stack liegt, ist dieser Wert erstmal -1.
        int stackPtr = -1;
        // Speichere, ob (durch einen HALT-Befehl) das Programm angehalten wurde.
        boolean halted = false;

        // Solange nicht angehalten wurde, führe die Simulation fort.
        while (!halted) {
            // Merke stets, ob gesprungen wurde, um am Ende, falls nicht, einfach in den nächsten Block übergehen zu können.
            boolean jumped = false;

            // Simuliere jede Instruktion in dem aktuellen Block nacheinander
            for (Instruction instruction : currentBlock.getInstructions()) {
                switch (instruction.type) {
                    // Arithmetische Operationen
                    case ADD -> {
                        stackPtr--;
                        stack[stackPtr] = stack[stackPtr] + stack[stackPtr + 1];
                    }
                    case SUB -> {
                        // TODO
                        throw new RuntimeException("case SUB not yet implemented!");
                    }
                    case MUL -> {
                        // TODO
                        throw new RuntimeException("case MUL not yet implemented!");
                    }
                    case DIV -> {
                        // TODO
                        throw new RuntimeException("case DIV not yet implemented!");
                    }
                    case MOD -> {
                        // TODO
                        throw new RuntimeException("case MOD not yet implemented!");
                    }
                    case NEG -> {
                        // TODO
                        throw new RuntimeException("case NEG not yet implemented!");
                    }

                    // Boolesche Operationen
                    case AND -> {
                        // TODO
                        throw new RuntimeException("case AND not yet implemented!");
                    }
                    case OR -> {
                        // TODO
                        throw new RuntimeException("case OR not yet implemented!");
                    }
                    case NOT -> {
                        // TODO
                        throw new RuntimeException("case NOT not yet implemented!");
                    }

                    // Vergleichs-Operationen
                    case LESS -> {
                        // TODO
                        throw new RuntimeException("case LESS not yet implemented!");
                    }
                    case LEQ -> {
                        // TODO
                        throw new RuntimeException("case LEQ not yet implemented!");
                    }
                    case EQ -> {
                        // TODO
                        throw new RuntimeException("case EQ not yet implemented!");
                    }
                    case NEQ -> {
                        // TODO
                        throw new RuntimeException("case NEQ not yet implemented!");
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
                    case TRUE -> {
                        // TODO
                        throw new RuntimeException("case TRUE not yet implemented!");
                    }
                    case FALSE -> {
                        // TODO
                        throw new RuntimeException("case FALSE not yet implemented!");
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

                    // Kontrollfluss-Befehle
                    case JUMP -> {
                        // TODO
                        throw new RuntimeException("case JUMP not yet implemented!");
                    }
                    case FJUMP -> {
                        // TODO
                        throw new RuntimeException("case FJUMP not yet implemented!");
                    }
                    case HALT -> {
                        // TODO
                        throw new RuntimeException("case HALT not yet implemented!");
                    }
                }
            }

            // Wenn nicht in einen anderen Block gesprungen oder angehalten wurde, gehe einfach zum nächsten Block weiter.
            if (!jumped && !halted) {
                currentBlock = currentBlock.next;
            }
        }
    }

    public BasicBlock getEntryPoint() {
        return entryPoint;
    }
}
