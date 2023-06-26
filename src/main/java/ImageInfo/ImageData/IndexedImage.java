package ImageInfo.ImageData;

import Housekeeping.HashCalculator;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


public class IndexedImage {

    private boolean  previouslyInDb;
    private boolean isDuplicate;
    private boolean confirmedImage;
    private final String hash, nombreArchivo,path;
    private int id;
    private final File imageFile;
    private ArrayList<String> tags;

    //public byte[] fileBytes;

    public IndexedImage(File imageFile) {
        this.isDuplicate=false;
        this.previouslyInDb =false;
        this.imageFile = imageFile;
        this.nombreArchivo = imageFile.getName();
        this.path = imageFile.getParent();
        this.confirmedImage=false;
        this.tags=new ArrayList<>();
        this.hash= HashCalculator.hashImage(imageFile);
        //confirmAsImage();

    }







    private void confirmAsImage() {
        try {

            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            byte[] subArray= Arrays.copyOfRange(fileBytes,0,12);
            //usar subarray es mejor. No se si dejarlo asi,lol
            String hexFileBytes= new BigInteger(1,subArray).toString(16);
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
                confirmedImage=true;
                break;
            }
        }
        if (!confirmedImage){
                System.out.println("didnt match! "+nombreArchivo);
        }
    }


    public boolean wasPreviouslyInDb() {
        return previouslyInDb;
    }

    public String getHash() {
        return hash;
    }


    public File getImageFile() {
        return imageFile;
    }

    public String getName() {
        return this.nombreArchivo;
    }

    public String getPath() {
        return path;
    }

    public void addTag(String newTag){
        tags.add(newTag);
    }

    public void removeTag(String remTag){
        tags.remove(remTag);
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    public int getId() {
        return id;
    }
}
