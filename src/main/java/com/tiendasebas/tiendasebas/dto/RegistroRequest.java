package com.tiendasebas.tiendasebas.dto;

import lombok.Data;

@Data
public class RegistroRequest {

    private String nombre;
    private String apellido;
    private String correo;
    private String password;
}