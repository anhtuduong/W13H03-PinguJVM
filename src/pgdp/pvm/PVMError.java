package pgdp.pvm;

public class PVMError extends RuntimeException {
    public PVMError(String message, int line) {
        super("Error in line " + line + ": " + message);
    }

    public PVMError(String message) {
        super(message);
    }
}
