import Housekeeping.HashCalculator;
import ImageInfo.DataManager;
import ImageInfo.ImageData.IndexedImage;
import org.apache.logging.log4j.core.util.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TDDCopiarArchivo {

    int numeroDuplicados=1;
    String imagesDirectory="testStuff/image/manyFiles/";
    String copiesDirectory=imagesDirectory+"copias/";
    DataManager dataMan;

    @BeforeEach
    void setUp() {
        try {
            Files.createDirectories(Path.of(imagesDirectory+"nueva/"));
            Files.deleteIfExists(Path.of(imagesDirectory+"nice.db"));
            Files.deleteIfExists(Path.of(imagesDirectory+"db.check"));
            for (File archivo:
                    (new File(copiesDirectory).listFiles())) {
                if (archivo.isDirectory()) continue;
                Files.deleteIfExists(archivo.toPath());
            }
            for (File archivo:
                    (new File(copiesDirectory+"nueva/").listFiles())) {
                if (archivo.isDirectory()) continue;
                Files.deleteIfExists(archivo.toPath());
            }
        }
        catch (Exception e){
            System.err.println("no se pudo borrar");
        }

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
    void creaNuevoArchivo(){


        dataMan.copiarArchivo(1,copiesDirectory,"nuevoArchivo.jpg");
        int archivosEnDirectorio=0;
        for (File archivo:
             new File(copiesDirectory).listFiles()) {
            if (archivo.isDirectory()) continue;
            archivosEnDirectorio++;
        }

        assertEquals(1,archivosEnDirectorio);
    }

    @Test
    void creaArchivoConNombreIndicado(){
        String nombreIndicado="nuevoArchivo.txt";
        dataMan.copiarArchivo(0,copiesDirectory,nombreIndicado);
        assertTrue(new File(copiesDirectory+nombreIndicado).exists());
    }

    @Test
    void permiteIndicarRutaDeDestino(){
        int indiceImagen=3;
        String nombreIndicado="mech_copia.png";
        String rutaDestino=copiesDirectory+"nueva/";
        dataMan.copiarArchivo(indiceImagen,rutaDestino,nombreIndicado);

        assertTrue(new File(rutaDestino+nombreIndicado).exists());
    }

    @Test
    void archivosSonLosMismos(){
        int indiceImagen=0;
        String nombreIndicado="mech_hash.png";
        String hashOriginal=HashCalculator.hashFile(dataMan.imagesInDir.get(indiceImagen).getImageFile());
        dataMan.copiarArchivo(indiceImagen,copiesDirectory,nombreIndicado);
        String hashCopia=HashCalculator.hashFile(new File(copiesDirectory+nombreIndicado));

        assertEquals(hashOriginal,hashCopia);
    }

    @Test
    void archivosSonLosMismosNombreIncorrecto(){
        int indiceImagen=0;
        String nombreIndicado="mech22.txt";
        String hashOriginal=HashCalculator.hashFile(dataMan.imagesInDir.get(indiceImagen).getImageFile());
        dataMan.copiarArchivo(indiceImagen,copiesDirectory,nombreIndicado);
        String hashCopia=HashCalculator.hashFile(new File(copiesDirectory+nombreIndicado));

        assertEquals(hashOriginal,hashCopia);
    }


    @Test
    void lanzaIllegalArgumentException(){
        int indiceImagen=1;
        String nombreDestino="error.archivo";
        dataMan.copiarArchivo(indiceImagen,copiesDirectory,nombreDestino);
        IllegalArgumentException thrown= Assertions.assertThrows(IllegalArgumentException.class,()->{
            dataMan.copiarArchivo(indiceImagen,copiesDirectory,nombreDestino);
        });
        assertEquals("archivo existe",thrown.getMessage());
    }

    @Test
    void lanzaIllegalArgumentExceptionArchivosDistintos(){
        int indiceImagen=1;
        int nuevoIndiceImagen=3;
        String nombreDestino="error.archivo";
        dataMan.copiarArchivo(indiceImagen,copiesDirectory,nombreDestino);
        IllegalArgumentException thrown= Assertions.assertThrows(IllegalArgumentException.class,()->{
            dataMan.copiarArchivo(nuevoIndiceImagen,copiesDirectory,nombreDestino);
        });
        assertEquals("archivo existe",thrown.getMessage());
    }








}