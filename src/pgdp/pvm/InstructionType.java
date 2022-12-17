package pgdp.pvm;

public enum InstructionType {
    //arithmetic instructions
    NEG,
    ADD,
    SUB,
    MUL,
    DIV,
    MOD,
    //boolean instructions
    NOT,
    AND,
    OR,
    //comparison operations
    LESS,
    LEQ,
    EQ,
    NEQ,
    //loading constants
    CONST,
    TRUE,
    FALSE,
    //local variables
    LOAD,
    STORE,
    //jump
    JUMP,
    FJUMP,
    //io
    READ,
    WRITE,
    //stack modification
    DUP,
    SWAP,
    POP,
    //end program
    HALT,
    //local variable allocation pseudo instruction
    ALLOC

}
