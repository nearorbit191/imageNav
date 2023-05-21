package ImageInfo.DbConnection;

import ImageInfo.DataManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DbManager {
    String checkFileName="db.check";
    int retries=0;
    public void createCheckFile(String databaseHash){
        try {
            Files.write(Path.of(checkFileName), databaseHash.getBytes());
        }
        catch(IOException ioEx){
            if (retries<3) {
                createCheckFile(databaseHash);
                retries++;
            }
            else {
                System.err.println("no se pudo crear el archivo de verificacion. Tal vez los permisos de lectura/escritura no estan bien configurados?");
                System.exit(1);
            }

        }
    }

    public boolean isDatabaseValid(String databaseHash){
        try {
            String hashInFile = Files.readString(Path.of(checkFileName));
            String currentDBHash = databaseHash;
            return hashInFile.equals(currentDBHash);
        }
        catch (IOException ioEx){
            System.err.println("no se pudo leer el archivo de verificación");
            return false;
        }
    }

    public boolean checkFileExists(){
        return Files.exists(Path.of(checkFileName));
    }


}
