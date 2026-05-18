package com.profitrack.dominio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria extends BaseEntity {

    @Column(name = "tipo_usuario", length = 50)
    private String tipoUsuario;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 120)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AccionAuditoria accion;

    @Column(name = "valores_anteriores", columnDefinition = "text")
    private String valoresAnteriores;

    @Column(name = "valores_nuevos", columnDefinition = "text")
    private String valoresNuevos;
}
