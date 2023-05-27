package ImageInfo.DbConnection;

import ImageInfo.DataManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DbManager {
    static String checkFileName="db.check";

    public static void writeCheckFile(String path,String databaseHash){
        try {
            Files.write(Path.of(path+checkFileName), databaseHash.getBytes());
        }
        catch(IOException ioEx){
            System.out.println("No se pudo escribir el archivo de verificacion.");
            System.exit(1);


        }
    }

    public static boolean isDatabaseValid(String path,String databaseHash){
        if (!checkFileExists(path)) return false;
        try {
            String hashInFile = Files.readString(Path.of(path+checkFileName));
            String currentDBHash = databaseHash;
            return hashInFile.equals(currentDBHash);
        }
        catch (IOException ioEx){
            System.err.println("no se pudo leer el archivo de verificación");
            return false;
        }
    }

    public static boolean checkFileExists(String path){
        return Files.exists(Path.of(path+checkFileName));
    }


}
