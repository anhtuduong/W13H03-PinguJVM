package pgdp.pvm;

public class Instruction {

    public final InstructionType type;
    public final int line;

    public Instruction(InstructionType type, int line) {
        this.type = type;
        this.line = line;
    }

    public InstructionType getType() {
        return type;
    }
}
