package ImageInfo.DbConnection;

import Housekeeping.NavLogger;
import ImageInfo.ImageData.IndexedImage;
import InputParse.InstructionParser;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInOut {
    public DbEssential dbEssential;
    public DbInOut(String workPath){
        dbEssential =new DbEssential(workPath);
    }

    public void imageInitialInsert(IndexedImage image){
        try{
            dbEssential.beginTransaction();
            boolean insertedNow= dbEssential.insertImageIntoDatabase(image);

            dbEssential.commitTransaction(image);
            if (insertedNow) {
                dbEssential.updateHash();
            }
            else{
                loadFromDb(image);
            }
        }
        catch(SQLException sqlex){
            dbEssential.rollbackTransaction(image);
            dbEssential.updateHash();
        }
    }

    private void loadFromDb(IndexedImage targetImage){
        try{
            dbEssential.rebuildImage(targetImage);
        }
        catch (SQLException sqlex){
            System.out.println(sqlex.getMessage());

            InstructionParser.somethingWentWrong("could not retrieve already inserted image");
        }
    }

    public void removeTag(IndexedImage targetImage,String removedTag){
        removedTag=removedTag.replace("-","");
        try{
            dbEssential.beginTransaction();
            dbEssential.removeTagFromImage(targetImage.getId(),removedTag);
            dbEssential.commitTransaction(targetImage);
            targetImage.removeTag(removedTag);
            NavLogger.logSuccess("tag removed");
        }
        catch (SQLException e){
            dbEssential.rollbackTransaction(targetImage);
            NavLogger.logError("tag couldn't be removed");
        }

    }

    public void writeTagsToDb(IndexedImage targetImage){
        try{
            dbEssential.beginTransaction();
            dbEssential.associateTagsToImage(targetImage);
            dbEssential.commitTransaction(targetImage);
            NavLogger.logSuccess("all changes committed");
        }
        catch (SQLException sqle){
            dbEssential.rollbackTransaction(targetImage);
            System.out.println(sqle.getMessage());
            sqle.printStackTrace();
            System.out.println("could not write tags to database...");
        }
    }

    public void writeHash(){
        dbEssential.writeHash();
    }
    public void somethingWrong(){
        dbEssential.closeConnection();
    }

    public void tearDown(){
        dbEssential.closeConnection();
        dbEssential.updateDbHashAndWrite();
    }


    public int amountOfDuplicates(){
        try{
            String sqlEncontrarDuplicado="select COUNT(nombre_duplicado) as total from duplicado;";
            ResultSet resultado= dbEssential.executeQueryStatement(sqlEncontrarDuplicado);
            if (resultado.next()){
                return resultado.getInt("total");
            }

        }
        catch (SQLException e){
            return -1;
        }
        return -1;
    }
}
