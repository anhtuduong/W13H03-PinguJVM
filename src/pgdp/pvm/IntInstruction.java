package pgdp.pvm;

public class IntInstruction extends Instruction {

    public final int value;

    public IntInstruction(InstructionType type, int line, int value) {
        super(type, line);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
