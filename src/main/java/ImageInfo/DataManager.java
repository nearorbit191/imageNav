package ImageInfo;

import ImageInfo.ImageData.IndexedImage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataManager {
    MessageDigest hashCalculator;

    public DataManager() {
        initMessageDigest();
    }

    public String hashFile(File file){
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return calculateHash(fileBytes);
        }
        catch (IOException e) {
            return "";
        }
    }

    public void showImageInViewer(IndexedImage image){
        try {
            Desktop.getDesktop().open(image.getImageFile());
        }
        catch (IOException e) {
            System.err.println("No se puede abrir el archivo.");
        }
    }


    public void hashSetImage(IndexedImage image) {
        image.setHash(calculateHash(image.getFileBytes()));
    }
    private String calculateHash(byte[] fileBytes) {
        byte[] hashBytes=hashCalculator.digest(fileBytes);
        return new BigInteger(1,hashBytes).toString(16);

    }

    private void initMessageDigest(){
        try {
            hashCalculator= MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException nsa){

            System.err.println("SHA-256 no parece estar disponible en esta plataforma. No se puede continuar.");
            System.exit(1);
        }
    }
}
