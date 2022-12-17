package pgdp.pvm;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public interface IO {

    class SystemIO implements IO{
        Scanner in=new Scanner(System.in);

        private SystemIO(){}

        @Override
        public void write(int i) {
            System.out.println(i);
        }

        @Override
        public int read() {
            while (true){
                try {
                    return in.nextInt();
                }catch (InputMismatchException e){
                    System.err.println("Not an int. Try again.");
                }
            }
        }
        static private final IO INSTANCE=new SystemIO();
        static private final IO INSTANCE_RAW=of(()-> {
            try {
                return System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, System.out::write);
    }

    static IO system() {
        return SystemIO.INSTANCE;
    }

    static IO systemRaw(){
        return SystemIO.INSTANCE_RAW;
    }

    static IO of(IntSupplier in, IntConsumer out) {
        return new IO() {
            @Override
            public void write(int i) {
                out.accept(i);
            }

            @Override
            public int read() {
                return in.getAsInt();
            }
        };
    }

    void write(int i);

    int read();
}
