package com.tiendasebas.tiendasebas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes_presenciales", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_presencial_documento", columnNames = {"tipo_documento", "numero_documento"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientePresencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idClientePresencial;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean estado;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = true;
    }
}