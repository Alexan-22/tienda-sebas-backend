package com.tiendasebas.tiendasebas.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompraRequest {

    private List<CompraItemDto> items;

    private String medioPago;
}