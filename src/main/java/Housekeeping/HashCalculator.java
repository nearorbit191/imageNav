package Housekeeping;


import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {


    public static String hashImage(File imageFile) {
        try {
            return calculateHash(Files.readAllBytes(imageFile.toPath()));
        }
        catch (Exception e){
            System.err.println("couldn't read file");
            return null;
        }


    }

    public static String hashFile(File file){
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return calculateHash(fileBytes);
        }
        catch (IOException e) {
            return "";
        }
    }

    private static String calculateHash(byte[] fileBytes) {
            MessageDigest digestInstance=initInstance();
            byte[] hashBytes = digestInstance.digest(fileBytes);
            return new BigInteger(1, hashBytes).toString(16);
    }

    private static MessageDigest initInstance(){
        try {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException nsa){

            System.err.println("SHA-256 no parece estar disponible en esta plataforma. No se puede continuar.");
            System.exit(1);
            return null;
        }
    }

}
