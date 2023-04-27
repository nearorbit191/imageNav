package ImageInfo.ImageData;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class IndexedImage {

    public boolean confirmedImage;
    String hash, nombreArchivo,path;
    File imageFile;
    List<String> tags;

    byte[] fileBytes;

    public IndexedImage(File imageFile) {
        this.imageFile = imageFile;
        this.nombreArchivo = imageFile.getName();
        this.path = imageFile.getParent();
        this.confirmedImage=false;
        confirmAsImage();

    }

    public IndexedImage(String path, String nombreArchivo) {
        this.path = path;
        this.nombreArchivo = nombreArchivo;
        this.confirmedImage=false;
        createImageFile();
        confirmAsImage();

    }


    //pensado al crear desde una base de datos
    public IndexedImage(String hash, String path, String nombreArchivo, List<String> tags) {
        this.hash = hash;
        this.path = path;
        this.nombreArchivo = nombreArchivo;
        this.tags = tags;
        this.confirmedImage=true;
        createImageFile();

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
                //System.out.println("matched: "+number);
                confirmedImage=true;
                break;
            }
        }
    }


    private void createImageFile() {
        this.imageFile = new File(path+ nombreArchivo);
        System.out.println("imageFile = " + path+ nombreArchivo);
    }








    public String getHash() {
        return hash;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
