package pgdp.pvm;

public class BlockInstruction extends Instruction {
    public final BasicBlock target;

    public BlockInstruction(InstructionType type, int line, BasicBlock target) {
        super(type, line);
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }
}
