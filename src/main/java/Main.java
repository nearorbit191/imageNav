import ImageInfo.FileIO.DirectoryReader;
import InputParse.InstructionParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Specified path: " + Arrays.toString(args));
        if (args.length<1){
            System.out.println("Usage: imagenav [directory to operate]");
            System.exit(-1);
        }
        if (!Files.isDirectory(Path.of(args[0]))){
            System.out.println("Provided target is not a directory");
            System.exit(-1);
        }
        String workPath= DirectoryReader.addTrailingSlashToPath(args[0]);
        workPath=workPath.replace("\\\\","/");


        InstructionParser parser = new InstructionParser(workPath);
        parser.startProgram();
    }
}
