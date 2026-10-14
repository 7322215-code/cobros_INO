package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Mensualidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensualidadRepository extends JpaRepository<Mensualidad, Integer> {
    List<Mensualidad> findByPagadoFalseOrderByAnioAscMesAsc();
}