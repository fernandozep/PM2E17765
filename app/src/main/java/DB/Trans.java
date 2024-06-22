package DB;

public class Trans {
    // Version Database
    public static final int Version = 1;
    // Nombre de la base de datos
    public static final String DBname = "PM012P";

    // Tabla personas
    public static final String TablePersonas = "personas";

    // Propiedades
    public static final String id = "id";
    public static final String pais = "pais";
    public static final String nombre = "nombre";
    public static final String telefono = "telefono";
    public static final String nota = "nota";
    public static final String foto = "foto";

    // DDL Create
    public static final String CreateTablePersonas = "CREATE TABLE " + TablePersonas + " ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, pais TEXT, nombre TEXT, telefono TEXT, nota TEXT, foto TEXT )";

    public static final String SelectAllPerson = "SELECT * FROM " + TablePersonas;

    public static final String DropTablePersonas = "DROP TABLE IF EXISTS " + TablePersonas;

    public static final String SelectUriImagen = "SELECT " + foto + " FROM " + TablePersonas;
}
