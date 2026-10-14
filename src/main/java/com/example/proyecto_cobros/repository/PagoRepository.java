package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Contar pagos por estado
    long countByEstado(String estado);

    // Buscar por estado
    List<Pago> findByEstado(String estado);

    // Buscar por estudiante (contiene)
    List<Pago> findByEstudianteContainingIgnoreCase(String estudiante);
}