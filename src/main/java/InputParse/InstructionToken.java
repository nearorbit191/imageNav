package InputParse;

public enum InstructionToken {
    VIEW(1),LIST(0),EXIT(0),DELETE(1),RECURSIVE(1),RECURSIVE_CHECK(0),MODIFY(1),SEARCH(1);

    int argumentCount;

    InstructionToken(int argumentCount) {
        this.argumentCount = argumentCount;
    }
}
