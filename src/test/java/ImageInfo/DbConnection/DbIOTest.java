package ImageInfo.DbConnection;

import ImageInfo.DataManager;
import ImageInfo.ImageData.IndexedImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


class DbIOTest {
    DbIO ioBaseDatos;
    String testDirectory="testStuff/";
    String testImagesDirectory="testSamples/";

    @BeforeEach
    void setUp() {
        ioBaseDatos=new DbIO();
        ioBaseDatos.createDatabase(testDirectory);


    }

    @AfterEach
    void tearDown() {

        try {
            ioBaseDatos.connection.close();
            ioBaseDatos=null;
            Files.deleteIfExists(Path.of(testDirectory+"nice.db"));
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
        DataManager man=new DataManager();
        IndexedImage testImage=new IndexedImage(testImagesDirectory+"jpg/","gura.jpg");
        man.hashSetImage(testImage);
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
}