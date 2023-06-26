package ImageInfo;

import Housekeeping.NavLogger;

import ImageInfo.DbConnection.DbInOut;
import ImageInfo.FileIO.DirectoryReader;
import ImageInfo.ImageData.IndexedImage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataManager{
    DbInOut dbio;

    DirectoryReader directoryRead;

    private List<File> goodFilesInDir;
    public List<IndexedImage> imagesInDir;
    public static final int amountToStartUsingThreads=200;
    String workPath;

    //HashCalculator hashCalc;


    private boolean recursiveMode;

    public DataManager(String workPath) {
        imagesInDir=new CopyOnWriteArrayList<>();
        directoryRead = new DirectoryReader();
        recursiveMode=true;
        this.workPath=workPath;
        dbio = new DbInOut(workPath);
        //hashCalc=new HashCalculator();
    }


    public void readDirectoryInit(){
        System.out.println("reading and processing images in directory, please wait...");

        readDirectory();
        threadedCreateIndexedImages();
        System.out.println("done processing. Inserting into database...");

        insertImagesIntoDatabase();
        System.out.println("done! ready to go!");
    }

    private void readDirectory(){
        if(recursiveMode){
            goodFilesInDir=directoryRead.recursiveReadBaseDirectory(workPath);
        }
        else{
            goodFilesInDir=directoryRead.nonRecursiveReadDirectory(workPath);
        }
    }

    private void threadedCreateIndexedImages(){

        if (goodFilesInDir.size()<amountToStartUsingThreads){
            createIndexedImages(goodFilesInDir);
            return;
        }
        long startTime=System.nanoTime();

        Runnable secondHalf = ()->{
                createIndexedImages(goodFilesInDir.subList(fileListHalf(),goodFilesInDir.size()));
        };
        Thread helpy=new Thread(secondHalf);
        helpy.start();
        createIndexedImages(goodFilesInDir.subList(0,fileListHalf()));
        try{
            helpy.join();
        }
        catch (Exception e){
            NavLogger.logError("Failed to join thread?");
        }

        System.out.println("[DEBUG] elapsed:"+(System.nanoTime()-startTime));



    }


    private int fileListHalf(){
         return goodFilesInDir.size()%2==0?(goodFilesInDir.size()/2):(goodFilesInDir.size()/2)+1;
    }

    private void createIndexedImages(List<File> subsetList){
        for (File imageFile:
             subsetList) {
            IndexedImage newImage = new IndexedImage(imageFile);
            imagesInDir.add(newImage);

        }
    }

    private void insertImagesIntoDatabase(){
        for (IndexedImage image :
             imagesInDir) {
            dbio.imageInitialInsert(image);
        }
        dbio.writeHash();
        cleanupDupesInList();

    }

    private void cleanupDupesInList(){
        for (IndexedImage image:
             imagesInDir) {
            if (image.isDuplicate()) imagesInDir.remove(image);
        }
    }



    public void listImages(){
        for (int i = 0; i < imagesInDir.size(); i++) {
            int humanIndex=i+1;
            IndexedImage image=imagesInDir.get(i);
            System.out.println("["+humanIndex+"] | "+image.getName()+" | "+image.getTags());
        }
    }



    public void listSearchResults(String[] tags){
        List<IndexedImage> results=searchForImages(tags);
        if (results.isEmpty()) System.out.println("no results!");
        for (int i = 0; i < imagesInDir.size(); i++) {
            int humanIndex=i+1;
            IndexedImage image=imagesInDir.get(i);
            if (results.contains(image)) System.out.println("["+humanIndex+"] | "+image.getName()+" | "+image.getTags());
        }
    }



    List<IndexedImage> searchForImages(String[] tags){
        String[] usableSearchTags=sanitizeTags(tags);
        List<IndexedImage> resultList=new ArrayList<>();
        imagesInDir.stream().forEach(image->{
            boolean shouldAdd=false;
            for (String searchedTag:
                 usableSearchTags) {
                if (!image.getTags().contains(searchedTag)){
                    shouldAdd=false;
                    break;
                }
                shouldAdd=true;

            }
            if (shouldAdd) resultList.add(image);
        });

        return resultList;

    }



    public void showImageInViewer(IndexedImage image){
        try {
            Desktop.getDesktop().open(image.getImageFile());
        }
        catch (IOException e) {
            System.err.println("No se puede abrir el archivo.");
        }
    }



    public void showImageTags(int positionInList){
        IndexedImage targetImage=imagesInDir.get(positionInList);
        System.out.println(targetImage.getTags().isEmpty()?"No tags associated":"Current tags:");
        for (int i = 0; i < targetImage.getTags().size(); i++) {
            System.out.println("+ "+targetImage.getTags().get(i));
        }
    }



    public void associateImageWithTags(int imagePos,String rawTags){
        String[] sanitizedTags=sanitizeTags(rawTags.split(",",0));
        IndexedImage targetImage=imagesInDir.get(imagePos);
        for (String tag:
             sanitizedTags) {
            if (tag.charAt(0)=='-') {
                dbio.removeTag(targetImage, tag);
            }
            else if (!targetImage.getTags().contains(tag) && (!tag.equals("") || !tag.equals(" "))){
                targetImage.addTag(tag);
            }
        }
        dbio.writeTagsToDb(targetImage);
    }



    String[] sanitizeTags(String[] separatedTags){
        for (int i = 0; i < separatedTags.length; i++) {
            separatedTags[i]=separatedTags[i].trim().toLowerCase();
        }
        return separatedTags;

    }



    public boolean isRecursiveMode() {
        return recursiveMode;
    }



    public void setRecursiveMode(boolean recursiveMode) {
        this.recursiveMode = recursiveMode;
    }



    public void somethingWrong(){
        dbio.somethingWrong();
    }



    public void tearDown(){
        dbio.tearDown();
    }



    public int amountOfDuplicates(){
        return dbio.amountOfDuplicates();
    }
}
