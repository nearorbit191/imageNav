package ImageInfo.DbConnection;

import ImageInfo.DataManager;
import ImageInfo.ImageData.IndexedImage;
import InputParse.InstructionParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class DbIO {
    Connection connection;
    private Statement statement;
    private String dbPath;
    private String dirpath;
    private String dbHash;

    private Set<String> hashesInDatabase;


    public DbIO(String dirPath) {
        hashesInDatabase = new TreeSet<>();
        this.dbPath =dirPath+"nice.db";
        this.dirpath=dirPath;
        createDBifNotExistsAndConnect();
        if (statement!=null) getExistingHashes();
    }

    void createDBifNotExistsAndConnect() {
        if (!Files.exists(Path.of(dbPath))) {
            createDatabase(dbPath);
        }
        else {
            dbHash= DataManager.hashFile(new File(dbPath));
            if (DbManager.checkFileExists(dirpath) && DbManager.isDatabaseValid(dirpath,this.dbHash)) {
                try {
                    connectToDatabase(dbPath);
                } catch (SQLException sqlex) {
                    System.err.println("couldn't connect to database. Check if anything changed in the working environment");
                    System.exit(1);
                }
            }
            else{
                System.err.println("WARNING: check file is not present or contents don't match the current database status.");
                System.err.println("Your options: write the current hash to the checkfile and try to continue as normal, or delete everything/restore from backup");
                System.err.println("Current database hash: " + dbHash);
                InstructionParser.somethingWentWrong("checkfile and db mismatch");
            }
    }


    }

    void createDatabase(String path){
        createDatabase(path,0);
        updateDbHashAndWrite();
    }

    private void createDatabase(String path, int tries){

        try {
            if (this.connection==null) connectToDatabase(path);
            createInitialDatabase();
        }
        catch (SQLException sqlEx){
            if (tries>=3){
                System.out.println( sqlEx.getMessage());

                System.err.println("no se pudo escribir los archivos base necesarios.");
            }
            createDatabase(path,tries+1);
        }
    }

    private void connectToDatabase(String pathToDatabase) throws SQLException {
        //pathToDatabase=Character.toString(pathToDatabase.charAt(pathToDatabase.length()-1)).equals("/")?pathToDatabase:pathToDatabase+"/";
        connection= DriverManager.getConnection("jdbc:sqlite:"+pathToDatabase);
        statement = connection.createStatement();
        statement.setQueryTimeout(30);
        statement.executeUpdate("PRAGMA foreign_keys=ON");
    }

    private void getExistingHashes(){
        try{
            String getHashesQuery="select hash from archivo;";
            ResultSet results=executeQueryStatement(getHashesQuery);
            while (results.next()){
                hashesInDatabase.add(results.getString("hash"));
            }
        }
        catch (SQLException e){
            //escribir al log o algo.
            InstructionParser.somethingWentWrong("could not request hashes already in database");
        }
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

    public boolean insertImageIntoDatabase(IndexedImage image) throws SQLException{
        if (alreadyInDatabase(image)) return false;
        if (isDuplicado(image)){
            image.isDuplicate=true;
            return false;
        }

        String sqlInstruction="insert into archivo(hash,nombre_archivo,ruta_archivo,id_estado_archivo) values('";
        sqlInstruction+=image.getHash()+"','"+image.getName()+"','"+image.getPath()+"',1);";
        executeUpdateStatement(sqlInstruction);
        image.setId(retrieveImageId(image));
        return true;
    }

    private boolean alreadyInDatabase(IndexedImage image) throws SQLException{
        boolean coincideDB=false;
        ResultSet coincidenceInDb=executeQueryStatement("select nombre_archivo,ruta_archivo from archivo where hash='"+image.getHash()+"';");
        if (coincidenceInDb.next()){
            String nombreArchivo=coincidenceInDb.getString("nombre_archivo");
            String rutaArchivo=coincidenceInDb.getString("ruta_archivo");
            coincideDB=(image.getPath().equals(rutaArchivo)&&image.getName().equals(nombreArchivo));
        }
        return !hashesInDatabase.isEmpty() && imageIsAlreadyIn(image.getHash()) && coincideDB;
    }




    private boolean imageIsAlreadyIn(String imageHash){
        return hashesInDatabase.contains(imageHash);
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
        if (dupeAlreadyRegistered(dupe)) return;
        String statement="insert into duplicado(ruta_duplicado,nombre_duplicado,id_archivo) values('"+dupe.getPath()+"','"+dupe.getName()+"','"+originalID+"');";
        executeUpdateStatement(statement);
        //System.out.println("[image "+dupe.getName()+" is a duplicate of "+originalID+"]");

    }

    private boolean dupeAlreadyRegistered(IndexedImage dupe) throws SQLException{
        ResultSet coincidenceInDb=executeQueryStatement("select nombre_duplicado,ruta_duplicado from duplicado where nombre_duplicado='"+dupe.getName()+"' and " +
                "ruta_duplicado='"+dupe.getPath()+"';");
        return coincidenceInDb.next();

    }

    public void rebuildImage(IndexedImage targetImage) throws SQLException{
        int realImageId=retrieveImageId(targetImage);
        targetImage.setId(realImageId);
        restoreSavedtags(targetImage);

    }

    private void restoreSavedtags(IndexedImage targetImage) throws SQLException{
        String sqlRetrieveTags="select etiq.des_etiqueta from etiqueta etiq, etiqueta_clasifica_archivo clas where clas.id_archivo="+targetImage.getId()+" and clas.id_etiqueta=etiq.id_etiqueta;";
        ResultSet result=executeQueryStatement(sqlRetrieveTags);
        while (result.next()){
            targetImage.addTag(result.getString("des_etiqueta"));
        }
    }


    public void associateTagsToImage(IndexedImage image) throws SQLException{
        String[] tagsInDB=getAllTags();
        if (tagsInDB==null) throw new SQLException();
        String baseStatement="insert into etiqueta_clasifica_archivo(id_archivo,id_etiqueta) values(";
        ArrayList<String> imgTags = image.getTags();
        ArrayList<String> newTags=new ArrayList<>(imgTags);
        if (!imgTags.isEmpty()){
            for (int i = 0; i < tagsInDB.length; i++) {
                for (int j = 0; j < imgTags.size(); j++) {
                    if (imgTags.get(j).equals(tagsInDB[i])){
                        String tagStatement=baseStatement+retrieveImageId(image)+","+getTagId(tagsInDB[i].replace("'","''"))+");";
                        addIfNotAlreadyIn(tagStatement);
                        newTags.remove(imgTags.get(j));
                    }
                }
            }
            if (!newTags.isEmpty()) addNewTags(retrieveImageId(image),newTags);
        }
    }

    private void addIfNotAlreadyIn(String tagStatement) throws SQLException{
        try {
            executeUpdateStatement(tagStatement);
        }
        catch (SQLException sqlex){
            if (sqlex.getMessage().toLowerCase().contains("primary")){
                //nada
            }
            else{
                System.out.println(sqlex.getMessage());
                throw new SQLException();
            }
        }
    }

    private void addNewTags(int imgId,ArrayList<String> newTags) throws SQLException{
        String newTagQuery ="insert into etiqueta(des_etiqueta) values('";
        String insertIntoRelation="insert into etiqueta_clasifica_archivo(id_archivo,id_etiqueta) values(";
        for (String tag:
             newTags) {
            executeUpdateStatement(newTagQuery +tag.replace("'","''")+"');");
            int tagId=getTagId(tag);
            executeUpdateStatement(insertIntoRelation+imgId+","+tagId+");");
        }

    }

    private int getTagId(String tag) throws SQLException {
        ResultSet idResult =executeQueryStatement("select id_etiqueta from etiqueta where des_etiqueta='"+tag+"';");
        if (idResult.next()){
            return idResult.getInt("id_etiqueta");
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
            allTags[tagsObtenidos.getInt("id_etiqueta")-1]=tagsObtenidos.getString("des_etiqueta");
        }

        return allTags;

    }

    public void rollbackTransaction(IndexedImage potentialFailureImage){
        System.err.println("ran into problems with image "+potentialFailureImage.getName()+", hash: "+potentialFailureImage.getHash());
        try{
            executeUpdateStatement("ROLLBACK;");
        }
        catch(Exception e){
            InstructionParser.somethingWentWrong("could not rollback a transaction, something must be wrong with the connection?");
        }
    }

    public void updateDbHashAndWrite(){
        updateHash();
        writeHash();
    }

    public void updateHash(){
        String newHash=DataManager.hashFile(new File(dbPath));
        if (!newHash.equals("")) {
            this.dbHash=newHash;
        }
    }


    public void writeHash(){
        if (!DbManager.isDatabaseValid(dirpath,dbHash)) DbManager.writeCheckFile(dirpath,dbHash);
    }

    public void closeConnection(){
        try {

            connection.close();
        }
        catch (Exception e){}
    }

    public String getDbHash() {
        return dbHash;
    }
}
