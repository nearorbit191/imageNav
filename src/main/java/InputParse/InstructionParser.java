package InputParse;

import CustomExceptions.UnknownInstructionException;

import java.util.Scanner;


public class InstructionParser {
    Scanner teclado;

    public InstructionParser(){
        teclado = new Scanner(System.in);
    }

    public void startProgram(){
        System.out.println("Welcome to imageNav. Type a command to start, or use 'help' for more information");
        while(true){
            System.out.print(">");
            readInstruction(teclado.nextLine());
        }
    }

    private void readInstruction(String instruction) {
        String[] receivedInstruction=instruction.split(" ",2);
        selectInstruction(instruction);

    }




    private void selectInstruction(String instruction){
        String[] separatedInstruction = instruction.split(" ",2);
        try{
            InstructionToken token=matchesInstructionWithArgCount(separatedInstruction);
        }
        catch(UnknownInstructionException e){
            System.out.println(e.getMessage());
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());

        }
    }

    private InstructionToken matchesInstructionWithArgCount(String[] instruction) throws UnknownInstructionException,IllegalArgumentException{
        InstructionToken candidate;
        switch(instruction[0].toLowerCase()){
            case "list":
                candidate = InstructionToken.LIST;
                if (instruction.length-1!=candidate.argumentCount) break;
                return candidate;
            case "recursive":
                candidate = instruction.length==1?InstructionToken.RECURSIVE_CHECK:InstructionToken.RECURSIVE;
                return candidate;
            case "view":
                candidate = InstructionToken.VIEW;
                if (instruction.length-1!=candidate.argumentCount) break;
                return candidate;
            case "delete":
                candidate= InstructionToken.DELETE;
                if (instruction.length-1!=candidate.argumentCount) break;
                return candidate;
            case "exit":
                System.out.println("bye.");
                System.exit(0);
            default: throw new UnknownInstructionException("unknown instruction '"+instruction[0]+"'");
        }
        throw new IllegalArgumentException("bad argument count for "+instruction[0]+" expected: "+candidate.argumentCount);
    }




}
