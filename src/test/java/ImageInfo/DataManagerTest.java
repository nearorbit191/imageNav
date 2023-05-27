package ImageInfo;

import ImageInfo.ImageData.IndexedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest {

    int numeroDuplicados=1;
    String imagesDirectory="testStuff/image/manyFiles/";
    DataManager dataMan;

    @BeforeEach
    void setUp() {
        dataMan=new DataManager(imagesDirectory);
        dataMan.readDirectoryInit();

    }

    @AfterEach
    void tearDown() {
        try {
            dataMan.tearDown();
            dataMan=null;

            Files.deleteIfExists(Path.of(imagesDirectory+"nice.db"));
            Files.deleteIfExists(Path.of(imagesDirectory+"db.check"));
        }
        catch (Exception e){
            System.err.println("no se pudo borrar");
        }
    }

    @Test
    void hashFileMech1() {
        String thisFile="mech1.png";
        String hashMech1="9860A3C0B814D28D6AE11EBA1E3BEAEB9BE8E860971E6EA81F4378CDE88B4719".toLowerCase();
        String calculatedHash=DataManager.hashFile(new File(imagesDirectory+thisFile));
        assertEquals(hashMech1, calculatedHash);
    }

    @Test
    void excludesNonImageFiles(){
        File directoryFile=new File(imagesDirectory);
        int amountOfNonImageFiles=(int) (Arrays.stream(directoryFile.listFiles()).filter(file->!dataMan.directoryRead.isImage(file)).count());
        int numberOfImagesInDir =directoryFile.listFiles().length-amountOfNonImageFiles;
        assertEquals(numberOfImagesInDir,dataMan.imagesInDir.size()+numeroDuplicados);
    }

    @Test
    void recognizesDuplicatedFiles(){
        assertEquals(numeroDuplicados,dataMan.amountOfDuplicates());


    }

    @ParameterizedTest
    @ValueSource(strings={"\\tag","something","test"})
    void tagsGetProperlyTrimmedOnlyOne(String original){
        String expectedResult=original;
        original="    "+original+"      ";
        String[] result= dataMan.sanitizeTags(original.split(","));
        assertEquals(expectedResult,result[0]);

    }

    @Test
    void allFilesAreInsertedIntoDatabase(){
        int filesInDb=0;
        File directoryFile=new File(imagesDirectory);
        int amountOfNonImageFiles=(int) (Arrays.stream(directoryFile.listFiles()).filter(file->!dataMan.directoryRead.isImage(file)).count());
        int numberOfImagesInDir =directoryFile.listFiles().length-amountOfNonImageFiles;
        try{
            String sqlGetNumberOfFilesInDB="select COUNT(nombre_archivo) as total_archivos from archivo;";
            ResultSet result=dataMan.dbio.executeQueryStatement(sqlGetNumberOfFilesInDB);
            if (result.next()){
                filesInDb=result.getInt("total_archivos");
            }
        }
        catch (SQLException sqlex){

        }

        assertEquals(numberOfImagesInDir-numeroDuplicados,filesInDb);
    }

    @Test
    void searchReturnsAppropiateResultsOneResOneTags(){
        dataMan.associateImageWithTags(1,"test, tag, something");
        List<IndexedImage> result=dataMan.searchForImages(new String[]{" something"});
        assertEquals(1,result.size());
    }

    @Test
    void searchReturnsAppropiateResultsOneResTwoTags(){
        dataMan.associateImageWithTags(1,"test, tag, something");
        List<IndexedImage> result=dataMan.searchForImages(new String[]{"test","tag"});
        assertEquals(1,result.size());
    }

    @Test
    void searchReturnsAppropiateResultsTwoResOneTags(){
        dataMan.associateImageWithTags(1,"   foo,bar   ");
        dataMan.associateImageWithTags(2,"  bar   ");
        List<IndexedImage> result=dataMan.searchForImages(new String[]{" bar  "});
        assertEquals(2,result.size());
    }




}