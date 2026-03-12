package com.christian.proyecto.repositories;

import com.christian.proyecto.entities.Persona;
import com.christian.proyecto.enums.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    List<Persona> findByNombreContainingIgnoreCase(String nombre);

    List<Persona> findByEmailContaining(String email);

    List<Persona> findByEdadBetween(Short min, Short max);

    List<Persona> findByTelefono(String telefono);

    List<Persona> findByGenero(Genero genero);

    boolean existsByTelefono(String telefono);

    boolean existsByTelefonoAndIdNot(String telefono, Long id);
}
