package Modelo;

import java.util.UUID;

public abstract class Usuario {

    private String id;
    private String nombreUsuario;
    private String contrasena;
    private Rol rol;

    public Usuario(String nombreUsuario, String contrasena, Rol rol) {
        this.id = UUID.randomUUID().toString();
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public boolean autenticar(String user, String pass) {
        return nombreUsuario.equals(user) && contrasena.equals(pass);
    }

    public String getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
