package ImageInfo;

import Housekeeping.HashCalculator;
import ImageInfo.DbConnection.DbEssential;
import ImageInfo.FileIO.DirectoryReader;
import ImageInfo.ImageData.IndexedImage;
import InputParse.InstructionParser;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataManager{
    private static MessageDigest hashCalculator;
    static Lock lock=new ReentrantLock();
    DbEssential dbio;

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
        dbio = new DbEssential(workPath);
        //hashCalc=new HashCalculator();
    }


    public void readDirectoryInit(){
        System.out.println("reading and processing images in directory, please wait...");

        readDirectory();
        //createIndexedImages();
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
        catch (Exception e){}

        System.out.println("[DEBUG] elapsed:"+(System.nanoTime()-startTime));



    }


    private int fileListHalf(){
         return goodFilesInDir.size()%2==0?(goodFilesInDir.size()/2):(goodFilesInDir.size()/2)+1;
    }

    private void createIndexedImages(List<File> subsetList){
        for (File imageFile:
             subsetList) {
            IndexedImage newImage = new IndexedImage(imageFile);
            HashCalculator.hashSetImage(newImage);
            imagesInDir.add(newImage);

        }
    }

    private void insertImagesIntoDatabase(){
        for (IndexedImage image :
             imagesInDir) {
            imageInitialInsert(image);
        }
        dbio.writeHash();
        cleanupDupesInList();

    }

    private void cleanupDupesInList(){
        for (IndexedImage image:
             imagesInDir) {
            if (image.isDuplicate) imagesInDir.remove(image);
        }
    }

    private void imageInitialInsert(IndexedImage image){
        try{
            dbio.beginTransaction();
            boolean insertedNow=dbio.insertImageIntoDatabase(image);

            dbio.commitTransaction(image);
            if (insertedNow) {
                dbio.updateHash();
            }
            else{
                loadFromDb(image);
            }
        }
        catch(SQLException sqlex){
            dbio.rollbackTransaction(image);
            dbio.updateHash();
        }
    }

    private void loadFromDb(IndexedImage targetImage){
        try{
            dbio.rebuildImage(targetImage);
        }
        catch (SQLException sqlex){
            System.out.println(sqlex.getMessage());

            InstructionParser.somethingWentWrong("could not retrieve already inserted image");
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
                removeTag(targetImage, tag);
            }
            else if (!targetImage.getTags().contains(tag) && (!tag.equals("") || !tag.equals(" "))){
                targetImage.addTag(tag);
            }
        }
        writeTagsToDb(targetImage);
    }

    private void removeTag(IndexedImage targetImage,String removedTag){
        removedTag=removedTag.replace("-","");
        try{
            dbio.beginTransaction();
            dbio.removeTagFromImage(targetImage.getId(),removedTag);
            dbio.commitTransaction(targetImage);
            targetImage.removeTag(removedTag);
        }
        catch (SQLException e){
            dbio.rollbackTransaction(targetImage);
        }

    }

    private void writeTagsToDb(IndexedImage targetImage){
        try{
            dbio.beginTransaction();
            dbio.associateTagsToImage(targetImage);
            dbio.commitTransaction(targetImage);
        }
        catch (SQLException sqle){
            dbio.rollbackTransaction(targetImage);
            System.out.println(sqle.getMessage());
            sqle.printStackTrace();
            System.out.println("could not write tags to database...");
        }
    }

    String[] sanitizeTags(String[] separatedTags){
        for (int i = 0; i < separatedTags.length; i++) {
            separatedTags[i]=separatedTags[i].trim().toLowerCase();
        }
        return separatedTags;

    }

    public void somethingWrong(){
        dbio.closeConnection();
    }

    public void tearDown(){
        dbio.closeConnection();
        dbio.updateDbHashAndWrite();
    }


    public int amountOfDuplicates(){
        try{
            String sqlEncontrarDuplicado="select COUNT(nombre_duplicado) as total from duplicado;";
            ResultSet resultado=dbio.executeQueryStatement(sqlEncontrarDuplicado);
            if (resultado.next()){
                return resultado.getInt("total");
            }

        }
        catch (SQLException e){
            return -1;
        }
        return -1;
    }



    public boolean isRecursiveMode() {
        return recursiveMode;
    }

    public void setRecursiveMode(boolean recursiveMode) {
        this.recursiveMode = recursiveMode;
    }


    public static void hashSetImage(IndexedImage image) {
        lock.lock();
        try{
            image.setHash(calculateHash(null));
        } finally {
            lock.unlock();
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
        if (hashCalculator == null) initMessageDigest();
        byte[] hashBytes = hashCalculator.digest(fileBytes);
        return new BigInteger(1, hashBytes).toString(16);

    }

    private static void initMessageDigest(){
        try {
            hashCalculator= MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException nsa){

            System.err.println("SHA-256 no parece estar disponible en esta plataforma. No se puede continuar.");
            System.exit(1);
        }
    }
}
