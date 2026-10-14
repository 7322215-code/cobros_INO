package com.example.proyecto_cobros.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @Column(name = "id_mensualidad")
    private Integer idMensualidad;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "metodo_pago", length = 25)
    private String metodoPago;

    @Column(nullable = false)
    private Boolean activo;

    @Column(length = 20)
    private String estado;

    @Column(length = 100)
    private String estudiante;

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }

    public Integer getIdMensualidad() { return idMensualidad; }
    public void setIdMensualidad(Integer idMensualidad) { this.idMensualidad = idMensualidad; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String estudiante) { this.estudiante = estudiante; }
}