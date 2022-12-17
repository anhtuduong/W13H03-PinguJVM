package pgdp.pvm;

import java.util.HashSet;
import java.util.Set;

public class Label {
    Set<String> aliases = new HashSet<>();

    Set<LabelInstruction> callers = new HashSet<>();

    int instruction = -1;

    public Label(int instruction) {
        this.instruction = instruction;
    }

    public Label() {
    }
}
