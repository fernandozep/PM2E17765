package DB;

public class Consulta {
    private Integer id;
    private String pais;
    private String nombre;
    private String telefono;
    private String nota;
    private String foto;

    public Consulta(Integer id, String pais, String nombre, String telefono, String nota, String foto) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.foto = foto;
    }

    public Consulta() {
    }
//id
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
//Pais
    public String getPais() {
        return pais;
    }
    public void setPais(String pais) {
        this.pais = pais;
    }
//Nombre
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
//Telefono
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
//Nota
    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
//Foto
    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
