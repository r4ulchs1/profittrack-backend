package com.profitrack.infraestructura.seguridad;

/**
 * Constantes centralizadas de nombres de rol.
 * Usadas en validarRol() para evitar strings hardcodeados en los controladores.
 */
public final class RolConstantes {

    private RolConstantes() {
        // No instanciable
    }

    public static final String OWNER = "Owner";
    public static final String GERENTE = "Gerente";
    public static final String PM = "PM";
    public static final String ADMINISTRADOR = "Administrador";
    public static final String DESARROLLADOR = "Desarrollador";
}
