package com.christian.proyecto.services;

import com.christian.proyecto.dto.PersonaRequest;
import com.christian.proyecto.dto.PersonaResponse;

import java.util.List;

public interface PersonaService {

    List<PersonaResponse> listar();

    PersonaResponse obtenerPorId(Long id);

    PersonaResponse registrar(PersonaRequest request);

    PersonaResponse actualizar(PersonaRequest request, Long id);

    void eliminar(Long id);
}
