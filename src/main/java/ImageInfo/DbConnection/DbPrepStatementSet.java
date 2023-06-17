package ImageInfo.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbPrepStatementSet {
    PreparedStatement insertNewImageStatement,insertNewTagStatement,insertImageTagRelationStatement,removeTagFromImageStatement;

    PreparedStatement retrieveImageDataStatement,retrieveTagIdStatement,retrieveImageTagsStatement,retrieveImageIdStatement;

    public DbPrepStatementSet(Connection connection){
        startPreparedStatements(connection);
    }

    private void startPreparedStatements(Connection connection) {
        try {
            insertNewImageStatement = connection.prepareStatement("insert into archivo(hash,nombre_archivo,ruta_archivo,id_estado_archivo) values(?,?,?,1);");
            insertNewTagStatement=connection.prepareStatement("insert into etiqueta(des_etiqueta) values(?);");
            insertImageTagRelationStatement=connection.prepareStatement("insert into etiqueta_clasifica_archivo(id_archivo,id_etiqueta) values(?,?);");
            removeTagFromImageStatement=connection.prepareStatement("delete from etiqueta_clasifica_archivo where id_etiqueta=? and id_archivo=?;");
            retrieveImageDataStatement= connection.prepareStatement("select nombre_archivo,ruta_archivo from archivo where hash=?;");
            retrieveImageTagsStatement=connection.prepareStatement("select etiq.des_etiqueta from etiqueta etiq, etiqueta_clasifica_archivo clas where clas.id_archivo=? and clas.id_etiqueta=etiq.id_etiqueta;");
            retrieveImageIdStatement=connection.prepareStatement("select id_archivo from archivo where hash=?");
            retrieveTagIdStatement=connection.prepareStatement("select id_etiqueta from etiqueta where des_etiqueta=?;");
        }
        catch (SQLException sqlex){

        }

    }
}
