package ImageInfo.ImageData;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;



public class IndexedImage {

    public boolean confirmedImage;
    String hash,filename,path;
    File imageFile;
    List<String> tags;
    MessageDigest hashCalculator;
    byte[] fileBytes;

    public IndexedImage(String path, String filename) {
        this.path = path;
        this.filename = filename;
        confirmedImage=false;
        createImageFile();
        confirmAsImage();
        initMessageDigest();
    }

    public IndexedImage(String hash,String path, String filename, List<String> tags) {
        this.hash = hash;
        this.path = path;
        this.filename = filename;
        this.tags = tags;
        this.confirmedImage=true;
        createImageFile();
        initMessageDigest();
    }

    private void confirmAsImage() {
        try {

            fileBytes = Files.readAllBytes(imageFile.toPath());
            String hexFileBytes= new BigInteger(1,fileBytes).toString(16).substring(0,9);
            compareToMagicNumbers(hexFileBytes);

        }
        catch (IOException io){
            System.err.println("No se pudo leer el archivo");
            confirmedImage=false;
        }

    }

    private void compareToMagicNumbers(String hexFileBytes){
        for (MagicNumbers number:
                MagicNumbers.values()) {
            if (hexFileBytes.contains(number.magicValues)){
                System.out.println("matched: "+number);
                confirmedImage=true;
                break;
            }
        }
    }


    private void createImageFile() {
        this.imageFile = new File(path+filename);
        System.out.println("imageFile = " + path+filename);
    }

    private void initMessageDigest(){
        try {
            hashCalculator = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException nsa){
            nsa.printStackTrace();
        }
    }

    public void hashImage(){
            byte[] hashBytes=hashCalculator.digest(fileBytes);
            hash=new BigInteger(1,hashBytes).toString(16);
            //System.out.println("hashBytes = " + new BigInteger(1,fileBytes).toString(16));
    }


    public String getHash() {
        return hash;
    }
}
