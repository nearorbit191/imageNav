package ImageInfo.FileIO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectoryReader {

    public ArrayList<File> filesToProcess, directoriesInBaseDirectory;
    List<File> imagesInDirectory;

    public DirectoryReader() {
        filesToProcess = new ArrayList<>();
        directoriesInBaseDirectory = new ArrayList<>();
        imagesInDirectory = new ArrayList<>();
    }

    public List<File> readDirectory(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        return Arrays.asList(files);
    }

    public void readBaseDirectory(String path) {
        List<File> files = readDirectory(path);
        for (File file : files) {
            if (file.isFile()) {
                filesToProcess.add(file);
            }
            else if (file.isDirectory()) {
                directoriesInBaseDirectory.add(file);
            }
        }
        //filesInDirectory=new ArrayList<>(Arrays.asList(files));
    }
    public void recursiveReadBaseDirectory(String path) {
        List<File> recursiveDirectoryContent;
        readBaseDirectory(path);
        for (File directory:
                directoriesInBaseDirectory) {
            System.out.println("directory.getName() = " + directory.getName());
            recursiveDirectoryContent=readDirectory(directory.getPath());
            recursiveReadDirectory(recursiveDirectoryContent);
        }
        System.out.println(filesToProcess);
    }

    private void recursiveReadDirectory(List<File> contents) {
        for (File content:
             contents) {
            System.out.println("content.getName() = " + content.getName());
            if (content.isFile()) {
                //System.out.println("file.getName() = " + content.getName());
                //System.out.println("file.getPath() = " + content.getPath().replace(content.getName(), "namewashere"));
                filesToProcess.add(content);
            }
            else if (content.isDirectory()) {
                recursiveReadDirectory(readDirectory(content.getPath()));
            }
        }
    }



    void selectImageFiles(){
        imagesInDirectory= filesToProcess.stream().filter(file-> isImage(file)).collect(Collectors.toList());
    }

    public boolean isImage(File file) {
        /*try {
            String fileType=Files.probeContentType(file.toPath()).toLowerCase();
            return fileType.contains("image") && (fileType.contains("jpeg") || fileType.contains("jpg") || fileType.contains("png"));
        }
        catch (IOException e) {
            System.err.println("No se pudo leer el archivo "+file.toPath());
            return false;
        }
        catch (Exception e) {
            return false;
        }*/
        return isImage(file.toPath());
    }
    public boolean isImage(Path path) {
        try {
            String fileType=Files.probeContentType(path).toLowerCase();
            System.out.println("fileType = " + fileType);
            //aparentemente no reconoce iso como imagen. Pero es mejor asegurarse pidiendo que incluya el tipo.
            return fileType.contains("image") && containsViewableImageData(fileType);
        }
        catch (IOException e) {
            System.err.println("No se pudo leer el archivo "+path);
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }
    private boolean containsViewableImageData(String fileType){
        return fileType.contains("jpeg") || fileType.contains("jpg") || fileType.contains("png") || fileType.contains("gif");
    }
}
