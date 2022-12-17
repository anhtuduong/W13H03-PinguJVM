package pgdp.pvm;

public class PVMSplitter {

    private final Instruction[] instructions;
    private final int localVariables;

    public PVMSplitter(Instruction[] instructions, int localVariables) {
        this.instructions = instructions;
        this.localVariables = localVariables;
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

    public PVMVerifier split() throws PVMError {
        var length = instructions.length;
        var entry = new BasicBlock(instructions);
        for (var block = entry; block != null; block = block.next)
            for (int i = 0; i < block.getInstructions().length; i++) {
                var instruction = block.getInstructions()[i];
                switch (instruction.type) {
                    case JUMP, FJUMP -> {
                        int next = block.index + i + 1;
                        //if we are at the end of the last block, we don't have to split.
                        if (next != length) {
                            block.split(next).getInbounds().add(instruction.line);
                        }
                        var target = block.split(((LabelInstruction) instruction).label.instruction);
                        target.getInbounds().add(instruction.line);
                        block.getInstructions()[i] =
                                new BlockInstruction(instruction.type, instruction.line,
                                        target
                                );
                    }
                    case HALT -> {
                        int next = block.index + i + 1;
                        //if we are at the end of the last block, we don't have to split.
                        if (next != length) {
                            block.split(next);
                        }
                    }
                }
            }

        return new PVMVerifier(entry, localVariables);
    }
}
