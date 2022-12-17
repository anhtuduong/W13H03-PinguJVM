package pgdp.pvm;

public class LabelInstruction extends Instruction {
    Label label;

    public LabelInstruction(InstructionType type, int line, Label label) {
        super(type, line);
        this.label = label;
    }

}
