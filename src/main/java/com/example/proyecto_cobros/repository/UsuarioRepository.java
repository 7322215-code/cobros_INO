package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository  // ← Agrega esta anotación (opcional pero recomendada)
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Método existente
    List<Usuario> findAllByOrderByUsernameAsc();

    // 🔹 NUEVO: Para el LOGIN (buscar por username y password)
    Optional<Usuario> findByUsernameAndPasswordHash(String username, String passwordHash);

    // 🔹 NUEVO: Para verificar si el usuario ya existe
    Optional<Usuario> findByUsername(String username);
}