package CustomExceptions;

public class UnknownInstructionException extends RuntimeException {
    public UnknownInstructionException(String message) {
        super(message);
    }
}
