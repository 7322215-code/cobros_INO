package com.example.proyecto_cobros.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mensualidades")
public class Mensualidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensualidad")
    private Integer idMensualidad;

    @Column(name = "id_estudiante")
    private Integer idEstudiante;

    private Integer anio;
    private Integer mes;
    private BigDecimal monto;
    private Boolean pagado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estudiante", insertable = false, updatable = false)
    private Estudiante estudiante;

    public Integer getIdMensualidad() { return idMensualidad; }
    public void setIdMensualidad(Integer idMensualidad) { this.idMensualidad = idMensualidad; }

    public Integer getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Integer idEstudiante) { this.idEstudiante = idEstudiante; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Boolean getPagado() { return pagado; }
    public void setPagado(Boolean pagado) { this.pagado = pagado; }

    public Estudiante getEstudiante() { return estudiante; }
}