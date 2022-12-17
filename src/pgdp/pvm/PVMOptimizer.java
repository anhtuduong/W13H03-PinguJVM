package pgdp.pvm;

public class PVMOptimizer {

    private final BasicBlock entryPoint;
    private final int localVariables;
    private final int maxStack;

    public PVMOptimizer(BasicBlock entryPoint, int localVariables, int maxStack) {
        this.entryPoint = entryPoint;
        this.localVariables = localVariables;
        this.maxStack = maxStack;
    }

    public void run(IO io) {
        optimize().run(io);
    }

    public PVMExecutable optimize() {
        for (var block = entryPoint; block != null; block = block.next) {
            if (!block.isReachable()) {
                block.prev.next = block.next;
                if (block.next != null) {
                    block.next.prev = block.prev;
                }
            }
        }
        return new PVMExecutable(entryPoint, localVariables, maxStack);
    }

}
