package ImageInfo.DbConnection;

public enum DbCreationStatements {

    CreateArchivoTable("CREATE TABLE archivo(id_archivo integer primary key,hash text not null unique, nombre_archivo text not null,ruta_archivo text not null, id_estado_archivo integer not null,foreign key(id_estado_archivo) references estado_archivo(id_estado_archivo));"),
    CreateEstadoArchivoTable("CREATE TABLE estado_archivo(id_estado_archivo integer primary key autoincrement,des_estado_archivo text not null);"),
    CreateEtiquetaTable("CREATE TABLE etiqueta(id_etiqueta integer primary key,des_etiqueta text not null unique);"),
    CreateDuplicadoTable("CREATE TABLE duplicado(id_duplicado integer primary key,ruta_duplicado text not null,nombre_duplicado text not null,id_archivo integer not null,foreign key(id_archivo) references archivo(id_archivo));"),
    CreateEtiquetaClasificaTable("CREATE TABLE etiqueta_clasifica_archivo(id_archivo integer not null,id_etiqueta integer not null,primary key(id_archivo,id_etiqueta),foreign key(id_archivo) references archivo(id_archivo),foreign key(id_etiqueta) references etiqueta(id_etiqueta));");

    String sqlStatement;

    DbCreationStatements(String sqlStatement) {
        this.sqlStatement = sqlStatement;
    }
}
