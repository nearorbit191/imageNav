package InputParse;

import CustomExceptions.UnknownInstructionException;
import Housekeeping.NavLogger;
import ImageInfo.DataManager;
import sun.misc.Signal;

import java.nio.file.Path;
import java.util.Scanner;


public class InstructionParser {
    private String pathToUse;
    private Scanner teclado;
    private static DataManager datManager;
    private static boolean allGood;

    public InstructionParser(String path){
        Signal.handle(new Signal("INT"),signal->unexpectedShutdown());
        //esto debera hacer lo necesario para evitar que un cierre forzado deje sin actualizar los archivos y deje los registros correspondientes
        NavLogger.logInfo("starting session at "+ Path.of(pathToUse).toAbsolutePath());

        allGood=true;
        teclado = new Scanner(System.in);
        datManager=new DataManager(path);
        pathToUse=path;
    }

    public void startProgram(){
        if (allGood) {
            System.out.println("Welcome to NICE. Type a command to start, or use 'help' for more information");

            datManager.readDirectoryInit();
        }
        while(true){
            if (!allGood) somethingWrongShutdown();
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
            matchesInstructionWithArgCount(separatedInstruction);
        }
        catch(UnknownInstructionException e){
            System.out.println(e.getMessage());
        }
        catch (IllegalArgumentException e){
            System.out.println(e.getMessage());

        }
    }

    private void  matchesInstructionWithArgCount(String[] instruction) throws UnknownInstructionException,IllegalArgumentException{
        InstructionToken candidate;
        switch(instruction[0].toLowerCase()){
            case "ls":
            case "list":
                candidate = InstructionToken.LIST;
                if (instruction.length-1!=candidate.argumentCount) break;
                listFilesInDir();
                return;
            case "recursive":
                candidate = instruction.length==1?InstructionToken.RECURSIVE_CHECK:InstructionToken.RECURSIVE;
                if (candidate==InstructionToken.RECURSIVE){
                    if (instruction[1].equals("on")||instruction[1].equals("off")){
                        boolean newValue= instruction[1].equals("on");
                        datManager.setRecursiveMode(newValue);
                        System.out.println("recursive is now: " + datManager.isRecursiveMode());
                    }
                    else{
                        System.out.println("bad argument for recursive option");
                    }
                }
                else{
                    System.out.println("recursive: "+datManager.isRecursiveMode());
                }
                return;
            case "mod":
            case "modify":
                candidate=InstructionToken.MODIFY;
                if (instruction.length-1!=candidate.argumentCount) break;
                tryModify(instruction[1]);
                return;

            case "show":
            case "view":
                candidate = InstructionToken.VIEW;
                if (instruction.length-1!=candidate.argumentCount) break;
                tryShowImage(instruction[1]);
                return;
            case "delete":
                candidate= InstructionToken.DELETE;
                if (instruction.length-1!=candidate.argumentCount) break;
                tryDeleteImage(instruction[1]);
                return;
            case "dupes":
                System.out.println("duplicates found: "+datManager.amountOfDuplicates());
                return;
            case "lazy":
                lazymode();
                return;
            case "search":
                searchForImages(instruction[1]);
                return;
            case "exit":
                System.out.println("bye.");
                NavLogger.logInfo("clean exit");
                datManager.tearDown();
                System.exit(0);
                return;
            default:
                throw new UnknownInstructionException("unknown instruction '"+instruction[0]+"'");

        }
        throw new IllegalArgumentException("bad argument count for "+instruction[0]+" expected: "+candidate.argumentCount);
    }

    private void lazymode(){
        System.out.println("enabling lazy mode");
        //datManager.imagesInDir.stream().forEach(image->image.fileBytes=null);
        System.gc();
    }

    private void listFilesInDir(){
        if (!datManager.imagesInDir.isEmpty()){
            datManager.listImages();
        }
        else{
            System.out.println("empty directory!");
        }
    }

    private void tryShowImage(String position){
        int humanPos=intTryParse(position);
        if (humanPos>0 && humanPos<=datManager.imagesInDir.size()){
            datManager.showImageInViewer(datManager.imagesInDir.get(humanPos-1));
        }
        else{
            System.out.println("bad position argument");
        }
    }

    private void tryModify(String position){
        int humanPos=intTryParse(position);
        if (humanPos>0 && humanPos<=datManager.imagesInDir.size()){
            datManager.showImageTags(humanPos-1);
            System.out.println("input the tags to associate (or append) with this image, you can provide a comma separated list. leave the line empty to save and exit");
            while (true){

                System.out.print("[MOD]>");
                String newTags=teclado.nextLine();
                if (newTags.equals("")) break;
                datManager.associateImageWithTags(humanPos-1,newTags);
            }
        }
        else{
            System.out.println("bad position argument");
        }
    }

    private void searchForImages(String tagsToSearch){
        datManager.listSearchResults(tagsToSearch.split(","));

    }

    private void tryDeleteImage(String position){
        int humanPos=intTryParse(position);
        if (humanPos>0 && humanPos<=datManager.imagesInDir.size()){
            System.out.println("feature still WIP");
        }
        else{
            System.out.println("bad position argument");
        }
    }

    private int intTryParse(String thingToParse){
        try{
            return Integer.parseInt(thingToParse);
        }
        catch(Exception e){
            return -1;
        }
    }


    public static void somethingWentWrong(String msg){
        System.err.println("shutting down due to "+msg);
        allGood=false;
    }

    private static void somethingWrongShutdown(){
        datManager.somethingWrong();
        System.exit(1);

    }


    private static void unexpectedShutdown(){
        NavLogger.logInfo("señal de interrupción recibida");
        datManager.tearDown();
        System.out.println("\nexec unexpected shutdown");
        NavLogger.logInfo("apagado por interrupción");
        System.exit(1);
    }




}
