package ImageInfo.ImageData;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


public class IndexedImage {

    public boolean confirmedImage, previouslyInDb,isDuplicate;
    private String hash, nombreArchivo,path;
    private int id;
    private File imageFile;
    private ArrayList<String> tags;

    private byte[] fileBytes;

    public IndexedImage(File imageFile) {
        this.isDuplicate=false;
        this.previouslyInDb =false;
        this.imageFile = imageFile;
        this.nombreArchivo = imageFile.getName();
        this.path = imageFile.getParent();
        this.confirmedImage=false;
        this.tags=new ArrayList<>();
        confirmAsImage();

    }

    public IndexedImage(String path, String nombreArchivo) {
        this.path = path;
        this.nombreArchivo = nombreArchivo;
        this.confirmedImage=false;
        this.tags=new ArrayList<>();
        createImageFile();
        confirmAsImage();

    }




    //pensado al crear desde una base de datos
    public IndexedImage(String hash, String path, String nombreArchivo, ArrayList<String> tags) {
        this.previouslyInDb =true;
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
                //System.out.println("matched: "+number+" ("+nombreArchivo+")");
                confirmedImage=true;
                break;
            }
        }
        if (!confirmedImage){
                System.out.println("didnt match! "+nombreArchivo);
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

    public String getName() {
        return this.nombreArchivo;
    }

    public String getPath() {
        return path;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void addTag(String newTag){
        tags.add(newTag);
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
