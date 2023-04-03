import ImageInfo.FileIO.DirectoryReader;
import ImageInfo.ImageData.IndexedImage;

public class Main {
    public static void main(String[] args) {
        DirectoryReader directoryReader=new DirectoryReader();
        //directoryReader.readBaseDirectory("src/main/java/ImageInfo");
        //System.out.println(directoryReader.filesToProcess.get(0));

        //Files.probeContentType(Paths.get(directoryReader.filesInDirectory.get(0).getAbsolutePath() + "DbIO.java"));
        //System.out.println(directoryReader.isImage(Path.of("gurasda.jpg")));
       // System.out.println(directoryReader.isImage(Path.of("mona.png")));
       // System.out.println(directoryReader.isImage(Path.of("centos.iso")));
        directoryReader.recursiveReadBaseDirectory("src");

        IndexedImage guraImage=new IndexedImage("testSamples/jpg/","gura.jpg");
        guraImage.hashImage();
        System.out.println(guraImage.getHash());
        System.out.println("guraImage.confirmedImage = " + guraImage.confirmedImage);

        IndexedImage monaImage=new IndexedImage("testSamples/png/","mona.png");
        monaImage.hashImage();
        System.out.println(monaImage.getHash());
        System.out.println("monaImage.confirmedImage = " + monaImage.confirmedImage);

    }
}
