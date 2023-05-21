package ImageInfo.DbConnection;

import ImageInfo.ImageData.IndexedImage;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;

public class DbIO {
    Connection connection;
    private Statement statement;
    SQLiteConfig config;

    public DbIO() {


    }

    void createDatabase(String path){
        createDatabase(path,0);
    }

    private void createDatabase(String path, int tries){

        try {
            if (this.connection==null) connectToDatabase(path);
            createInitialDatabase();
        }
        catch (SQLException sqlEx){
            if (tries>=3){
                System.err.println("no se pudo escribir los archivos base necesarios.");
            }
            createDatabase(path,tries+1);
        }
    }

    void connectToDatabase(String pathToDatabase) throws SQLException {
        //config=new SQLiteConfig();
        //config.setPragma(SQLiteConfig.Pragma.FOREIGN_KEYS,"ON");
        pathToDatabase=Character.toString(pathToDatabase.charAt(pathToDatabase.length()-1)).equals("/")?pathToDatabase:pathToDatabase+"/";
        connection= DriverManager.getConnection("jdbc:sqlite:"+pathToDatabase+"nice.db");
        statement = connection.createStatement();
        statement.setQueryTimeout(30);

        statement.executeUpdate("PRAGMA foreign_keys=ON");

    }

    public void executeUpdateStatement(String sql) throws SQLException {
        statement.executeUpdate(sql);
    }

    public ResultSet executeQueryStatement(String sql) throws SQLException {
        return statement.executeQuery(sql);
    }

    private void createInitialDatabase() throws SQLException {
        createBaseTables();
        insertIntoFileStatus();
    }

    private void createBaseTables() throws SQLException {
        for (DbCreationStatements creationStatement :
                DbCreationStatements.values())
        {
            executeUpdateStatement(creationStatement.sqlStatement);
        }

    }

    private void insertIntoFileStatus() throws SQLException {
        String[] statuses=new String[]{"present","missing","deleted"};
        String sqlBase="insert into estado_archivo(des_estado_archivo) values('";
        for (String status : statuses
             ) {
            executeUpdateStatement(sqlBase+status+"');");
        }
    }

    public void insertImageIntoDatabase(IndexedImage image) throws SQLException{
        if (isDuplicado(image)){
            System.out.println("Es un duplicado");
            return;
        }
        String sqlInstruction="insert into archivo(hash,nombre_archivo,ruta_archivo,id_estado_archivo) values('";
        sqlInstruction+=image.getHash()+"','"+image.getName()+"','"+image.getPath()+"',1);";
        executeUpdateStatement(sqlInstruction);
    }



    private int retrieveImageId(IndexedImage image) throws SQLException{
        ResultSet idResult =executeQueryStatement("select id_archivo from archivo where hash='"+image.getHash()+"';");
        if (idResult.next()){
            return idResult.getInt("id_archivo");
        }
        return -1;
    }
    private boolean isDuplicado(IndexedImage posibleDupe) throws SQLException{
        ResultSet dupeResult =executeQueryStatement("select id_archivo from archivo where hash='"+posibleDupe.getHash()+"';");
        if (dupeResult.next()){
            addDupe(posibleDupe,dupeResult.getInt("id_archivo"));
            return true;
        }
        return false;
    }

    private void addDupe(IndexedImage dupe,int originalID) throws SQLException{
        String statement="insert into duplicado(ruta_duplicado,nombre_duplicado,id_archivo) values('"+dupe.getPath()+"','"+dupe.getName()+"','"+originalID+"');";
        executeUpdateStatement(statement);
        System.out.println("image "+dupe.getName()+" is a duplicate of "+originalID);

    }


    private void addTags(IndexedImage image) throws SQLException{
        String[] tagsInDB=getAllTags();
        String baseStatement="insert into etiqueta_clasifica_archivo(id_archivo,id_etiqueta) values(";
        ArrayList<String> imgTags = image.getTags();
        ArrayList<String> newTags=new ArrayList<String>(imgTags);
        if (!imgTags.isEmpty()){
            for (int i = 0; i < tagsInDB.length; i++) {
                for (int j = 0; j < imgTags.size(); j++) {
                    if (imgTags.get(j).equals(tagsInDB[i])){
                        String tagStatement=baseStatement+i+","+image.getId()+");";
                        executeUpdateStatement(tagStatement);
                        newTags.remove(j);
                    }
                }
            }
            if (!newTags.isEmpty()) addNewTags(image.getId(),newTags);
        }
    }

    private void addNewTags(int imgId,ArrayList<String> newTags) throws SQLException{
        String newTagQuery ="insert into etiqueta(des_etiqueta) values('";
        String insertIntoRelation="insert into etiqueta_clasifica_archivo(id_archivo,id_etiqueta) values(";
        for (String tag:
             newTags) {
            executeUpdateStatement(newTagQuery +tag+"');");
            int tagId=getTagId(tag);
            executeUpdateStatement(insertIntoRelation+tagId+","+imgId+");");
        }

    }

    private int getTagId(String tag) throws SQLException {
        ResultSet idResult =executeQueryStatement("select id_etiqueta from etiqueta where des_etiqueta='"+tag+"';");
        if (idResult.next()){
            return idResult.getInt("id_archivo");
        }
        return -1;
    }

    private String[] getAllTags() throws SQLException{
        String[] allTags;
        String sqlMaxTags="select MAX(id_etiqueta) as maxTags from etiqueta;";
        ResultSet tagNumber=executeQueryStatement(sqlMaxTags);
        if (!tagNumber.next()) return null;
        allTags=new String[tagNumber.getInt("maxTags")];
        String sqlGetTags="select id_etiqueta,des_etiqueta from etiqueta;";
        ResultSet tagsObtenidos=executeQueryStatement(sqlGetTags);

        while(tagsObtenidos.next()){
            allTags[tagsObtenidos.getInt("id_etiqueta")]=tagsObtenidos.getString("des_etiqueta");
        }

        return allTags;

    }

}
