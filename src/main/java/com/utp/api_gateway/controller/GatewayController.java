package com.utp.api_gateway.controller;

import com.utp.api_gateway.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;


import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "API Gateway", description = "Rutas expuestas por el gateway que reenvían peticiones a microservicios")
public class GatewayController {

    private final GatewayService gatewayService;

    // ==== AUTH ====

    @PostMapping("/auth/register")
    @Operation(
            summary = "Registro de usuario",
            description = "Registra un nuevo usuario en el sistema.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            }
    )
    public ResponseEntity<?> handleRegistro(@RequestBody Map<String, Object> body) {
        return gatewayService.forwardPost("http://localhost:8081/auth/register", body);
    }

    @PostMapping("/auth/login")
    @Operation(
            summary = "Login de usuario",
            description = "Autentica un usuario y devuelve un token JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login correcto"),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
            }

    )
    public ResponseEntity<?> handleLogin(@RequestBody Map<String, Object> body) {
        return gatewayService.forwardPost("http://localhost:8081/auth/login", body);
    }

    @GetMapping("/auth/validate")
    @Operation(
            summary = "Validar token JWT",
            description = "Valida un token JWT y devuelve los datos del usuario.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token válido"),
                    @ApiResponse(responseCode = "400", description = "Token no proporcionado"),
                    @ApiResponse(responseCode = "401", description = "Token inválido")
            }
    )
    public ResponseEntity<?> handleValidate(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Falta el token de autorización"));
        }
        return gatewayService.forwardGet("http://localhost:8081/auth/validate", token);
    }

    // ==== CURSOS ====

    @PostMapping("/cursos")
    @Operation(
            summary = "Crear curso",
            description = "Crea un curso nuevo asociado al docente autenticado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Curso creado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Token no proporcionado"),
                    @ApiResponse(responseCode = "401", description = "Token inválido"),
                    @ApiResponse(responseCode = "403", description = "Solo docentes pueden crear cursos")
            }
    )
    public ResponseEntity<?> handleCrearCurso(@RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token no proporcionado"));
        }

        Map<String, Object> validation = gatewayService.validateToken(token);
        validation.forEach((a,b)-> System.out.println(a+"->"+b));
        if (!(Boolean) validation.getOrDefault("valid", false)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido"));
        }

        if (!"ROLE_DOCENTE".equals(validation.get("rol"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Solo docentes pueden crear cursos"));
        }

        body.put("docenteEmail", validation.get("correo"));

        return gatewayService.forwardPostWithToken("http://localhost:8082/cursos", body, token);
    }

    @GetMapping("/cursos/docente/{email}")
    @Operation(
            summary = "Listar cursos de un docente",
            description = "Lista los cursos creados por el docente autenticado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cursos encontrados"),
                    @ApiResponse(responseCode = "400", description = "Token no proporcionado"),
                    @ApiResponse(responseCode = "401", description = "Token inválido"),
                    @ApiResponse(responseCode = "403", description = "Solo docentes pueden consultar sus cursos")
            }
    )
    public ResponseEntity<?> handleListarCursosDocente(
            @Parameter(description = "Correo electrónico del docente", example = "docente@utp.edu.pe")
            @PathVariable String email,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token no proporcionado"));
        }

        Map<String, Object> validation = gatewayService.validateToken(token);
        if (!(Boolean) validation.getOrDefault("valid", false)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido"));
        }

        if (!"ROLE_DOCENTE".equals(validation.get("rol"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Solo docentes pueden consultar sus cursos"));
        }

        String url = "http://localhost:8082/cursos/docente/" + email;
        return gatewayService.forwardGet(url, token);
    }

}
