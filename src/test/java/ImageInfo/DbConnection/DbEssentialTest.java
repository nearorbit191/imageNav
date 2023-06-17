package ImageInfo.DbConnection;

import Housekeeping.HashCalculator;
import ImageInfo.ImageData.IndexedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


class DbEssentialTest {
    DbEssential ioBaseDatos;
    String testDirectory="testStuff/";
    String testImagesDirectory=testDirectory+"image/";

    @BeforeEach
    void setUp() {
        ioBaseDatos=new DbEssential(testDirectory);


    }

    @AfterEach
    void tearDown() {

        try {
            ioBaseDatos.closeConnection();

            Files.deleteIfExists(Path.of(testDirectory+"nice.db"));
            Files.deleteIfExists(Path.of(testDirectory+"db.check"));
        }
        catch (Exception e){
            System.err.println("no se pudo borrar");
        }
    }

    @Test
    void createInitialDatabase() {
        String selectIfExists="select count(name) as tablas from sqlite_master where type='table' and name not like 'sqlite_sequence';";
        int countResultado=0;
        try {
            ResultSet resultado = ioBaseDatos.executeQueryStatement(selectIfExists);
            resultado.next();
            countResultado = resultado.getInt("tablas");
        }
        catch (Exception e) {}
        assertEquals(DbCreationStatements.values().length,countResultado);
        //por usar una pk con autoincrement, sqlite crea una tabla interna para rastrear los rowid usados.
        //por eso la consulta excluye el nombre sqlite_sequence

    }

    @Test
    void estadoArchivoTieneValoresNecesarios(){
        String sqlEstadosArchivo ="select count(des_estado_archivo) as total_estado from estado_archivo;";
        int countResultado=0;
        try {
            ResultSet resultado =ioBaseDatos.executeQueryStatement(sqlEstadosArchivo);
            resultado.next();
            countResultado=resultado.getInt("total_estado");
        }
        catch (Exception e){}
        assertEquals(3,countResultado);

    }

    @Test
    void clavesForaneasFuncionanVacio(){
        boolean valores=false;
        String sqlIn="insert into etiqueta_clasifica_archivo(id_etiqueta,id_archivo) values (1,1);";
        String sqlOut="select id_etiqueta,id_archivo from etiqueta_clasifica_archivo;";
        try {
            ioBaseDatos.executeUpdateStatement(sqlIn);
            ResultSet resultado=ioBaseDatos.executeQueryStatement(sqlOut);
            valores=resultado.next();
            if (valores){
                System.out.println("resultado.getInt(\"id_etiqueta\") = " + resultado.getInt("id_etiqueta"));
            }

        }
        catch (SQLException e){
            System.out.println("bad at "+e.getMessage());
        }
        assertFalse(valores);
    }

    @Test
    void clavesForaneasFuncionanValores(){
        boolean valores=false;

        IndexedImage testImage=new IndexedImage(new File(testImagesDirectory+"mech1.png"));
        HashCalculator.hashSetImage(testImage);
        try {
            String sqlInTag="insert into etiqueta(des_etiqueta) values('weapon');";
            ioBaseDatos.insertImageIntoDatabase(testImage);
            String sqlIn="insert into etiqueta_clasifica_archivo(id_etiqueta,id_archivo) values (1,1);";
            String sqlOut="select id_etiqueta,id_archivo from etiqueta_clasifica_archivo;";
            ioBaseDatos.executeUpdateStatement(sqlInTag);
            ioBaseDatos.executeUpdateStatement(sqlIn);
            ResultSet resultado=ioBaseDatos.executeQueryStatement(sqlOut);
            valores=resultado.next();
            if (valores){
                System.out.println("resultado.getInt(\"id_etiqueta\") = " + resultado.getInt("id_etiqueta"));
            }

        }
        catch (SQLException e){
            System.out.println("bad at "+e.getMessage());
        }
        assertTrue(valores);
    }

    @Test
    void recuperarImagenPreviamenteGuardadas(){
        boolean valores=false;

        IndexedImage testImage=new IndexedImage(new File(testImagesDirectory+"mech1.png"));
        IndexedImage recreatedImage=new IndexedImage(new File(testImagesDirectory+"mech1.png"));
        HashCalculator.hashSetImage(testImage);
        HashCalculator.hashSetImage(recreatedImage);
        try {
            testImage.addTag("weapon");
            testImage.addTag("mecha");

            ioBaseDatos.insertImageIntoDatabase(testImage);
            ioBaseDatos.associateTagsToImage(testImage);

            ioBaseDatos.rebuildImage(recreatedImage);

        }
        catch (SQLException e){
            System.out.println("bad at "+e.getMessage());
        }
        assertEquals(testImage.getId(),recreatedImage.getId());
    }
}