package com.christian.proyecto.services;

import com.christian.proyecto.dto.PersonaRequest;
import com.christian.proyecto.dto.PersonaResponse;
import com.christian.proyecto.entities.Persona;
import com.christian.proyecto.enums.Genero;
import com.christian.proyecto.exceptions.RecursoNoEncontradoException;
import com.christian.proyecto.mappers.PersonaMapper;
import com.christian.proyecto.repositories.PersonaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;

    private final PersonaMapper personaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> listar() {
        log.info("Listado de todas las personas solicitado");
        return personaRepository.findAll().stream()
                .map(personaMapper::entityToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> obtenerPorNombre(String nombre) {
        log.info("Buscando personas que contengan el nombre: {}", nombre);
        return personaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(personaMapper::entityToResponse).toList();
    }

    @Override
    public List<PersonaResponse> obtenerPorEmail(String email) {
        log.info("Buscando personas que contengan el email: {}", email);
        return personaRepository.findByEmailContaining(email.toLowerCase()).stream()
                .map(personaMapper::entityToResponse).toList();
    }

    @Override
    public List<PersonaResponse> obtenerPorRangoEdad(Short edadMin, Short edadMax) {
        log.info("Buscando personas que estén en el rango de edad entre los {} y los {} años",
                edadMin, edadMax);
        return personaRepository.findByEdadBetween(edadMin, edadMax).stream()
                .map(personaMapper::entityToResponse).toList();
    }

    @Override
    public List<PersonaResponse> obtenerPorTelefono(String telefono) {
        log.info("Buscando personas que tengan el teléfono: {}", telefono);
        return personaRepository.findByTelefono(telefono).stream()
                .map(personaMapper::entityToResponse).toList();
    }

    @Override
    public List<PersonaResponse> obtenerPorGenero(Character genero) {
        Genero generoEnum = Genero.obtenerPorAbreviacion(genero);
        log.info("Buscando personas que pertenezcan al género: {}", generoEnum.getDescripcion());
        return personaRepository.findByGenero(generoEnum).stream()
                .map(personaMapper::entityToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtenerPorId(Long id) {
        return personaMapper.entityToResponse(obtenerPersonaOException(id));
    }

    @Override
    public PersonaResponse registrar(PersonaRequest request) {
        log.info("Registrando nueva Persona: {}", request.nombre());

        Genero genero = Genero.obtenerPorAbreviacion(request.genero());

        validarTelefonoUnico(request.telefono());

        String email = generarEmail(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno());

        Persona persona = personaRepository.save(personaMapper.requestToEntity(request, genero, email));
        log.info("Nueva Persona registrada: {}", persona.getNombre());

        return personaMapper.entityToResponse(persona);
    }

    @Override
    public PersonaResponse actualizar(PersonaRequest request, Long id) {
        Persona persona = obtenerPersonaOException(id);

        log.info("Actualizando Persona con id: {}", id);

        validarTelefonoUnicoActualizado(request.telefono(), id);

        persona.setNombre(request.nombre());
        persona.setApellidoPaterno(request.apellidoPaterno());
        persona.setApellidoMaterno(request.apellidoMaterno());
        persona.setEdad(request.edad());
        persona.setTelefono(request.telefono());

        Genero genero = Genero.obtenerPorAbreviacion(request.genero());
        persona.setGenero(genero);

        String email = generarEmail(
                request.nombre(),
                request.apellidoPaterno(),
                request.apellidoMaterno());
        persona.setEmail(email);

        log.info("Persona actualizada con id: {}", id);
        return personaMapper.entityToResponse(persona);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando Persona con id: {}", id);
        personaRepository.delete(obtenerPersonaOException(id));
        log.info("Persona con id {} eliminada", id);
    }

    public Persona obtenerPersonaOException(Long id) {
        log.info("Buscando Persona con id: {}", id);
        return personaRepository.findById(id).orElseThrow( () ->
                new RecursoNoEncontradoException("Persona no encontrada con el id: " + id));
    }

    private String obtenerPrimerosCaracteres(String texto, int cantidad) {
        if (texto == null) return "";

        return texto.length() <= cantidad ? texto : texto.substring(0, cantidad);
    }

    private String generarEmail(String nombre, String apellidoPaterno, String apellidoMaterno) {
        log.info("Generando email...");
        return (
                obtenerPrimerosCaracteres(nombre, 5) +
                        obtenerPrimerosCaracteres(apellidoPaterno, 5) +
                        obtenerPrimerosCaracteres(apellidoMaterno, 5) + "@ejemplo.com"
                ).toLowerCase();
    }

    private void validarTelefonoUnico(String telefono) {
        log.info("Validando teléfono único...");
        if (personaRepository.existsByTelefono(telefono)) {
            throw new IllegalArgumentException("Ya existe una persona registrada con el teléfono " + telefono);
        }
    }

    private void validarTelefonoUnicoActualizado(String telefono, Long id) {
        log.info("Validando teléfono único al actualizar...");
        if (personaRepository.existsByTelefonoAndIdNot(telefono, id)) {
            throw new IllegalArgumentException("Ya existe una persona registrada con el teléfono " + telefono);
        }
    }
}
