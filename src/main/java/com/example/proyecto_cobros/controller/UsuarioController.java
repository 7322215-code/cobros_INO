package com.example.proyecto_cobros.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.proyecto_cobros.entity.Usuario;
import com.example.proyecto_cobros.repository.UsuarioRepository;
import com.example.proyecto_cobros.repository.PagoRepository;
import com.example.proyecto_cobros.security.HashUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PagoRepository pagoRepo;

    // =========================================
    // LOGIN - GET
    // =========================================
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return "redirect:/usuarios/dashboard";
        }
        return "login";
    }

    // =========================================
    // LOGIN - POST
    // =========================================
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        if (username == null || password == null
                || username.trim().isEmpty() || password.trim().isEmpty()) {
            model.addAttribute("error", "Debe ingresar usuario y contraseña");
            return "login";
        }

        String passwordHash = HashUtil.sha256(password);

        Optional<Usuario> usuarioOptional =
                repo.findByUsernameAndPasswordHash(username.trim(), passwordHash);

        if (usuarioOptional.isEmpty()) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }

        Usuario usuario = usuarioOptional.get();

        if (usuario.getActivo() != null && !usuario.getActivo()) {
            model.addAttribute("error", "El usuario está desactivado");
            return "login";
        }

        session.setAttribute("usuario", usuario);
        session.setAttribute("rol", usuario.getRol());

        return "redirect:/usuarios/dashboard";
    }

    // =========================================
    // DASHBOARD con estadísticas
    // =========================================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/usuarios/login";
        }

        long totalUsuarios = repo.count();
        long totalPagos = pagoRepo.count();
        long pagosPagados = pagoRepo.countByEstado("Pagado");
        long pagosPendientes = pagoRepo.countByEstado("Pendiente");

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("pagosPagados", pagosPagados);
        model.addAttribute("pagosPendientes", pagosPendientes);

        return "dashboard";
    }

    // =========================================
    // PANEL ADMIN
    // =========================================
    @GetMapping("/panel-admin")
    public String panelAdmin(HttpSession session, Model model) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios/login";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().equalsIgnoreCase("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        List<Usuario> usuarios = repo.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuarioObj", new Usuario());

        return "admin_usuarios";
    }

    // =========================================
    // CREAR USUARIO
    // =========================================
    @PostMapping("/crear")
    public String crearUsuario(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmarPassword,
                               @RequestParam String rol,
                               HttpSession session,
                               Model model) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios/login";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().equalsIgnoreCase("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        if (username == null || password == null || confirmarPassword == null || rol == null
                || username.trim().isEmpty() || password.trim().isEmpty()
                || confirmarPassword.trim().isEmpty() || rol.trim().isEmpty()) {

            model.addAttribute("error", "Todos los campos son obligatorios");
            model.addAttribute("usuarios", repo.findAll());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        if (password.length() < 4) {
            model.addAttribute("error", "La contraseña debe tener mínimo 4 caracteres");
            model.addAttribute("usuarios", repo.findAll());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("usuarios", repo.findAll());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        Optional<Usuario> existe = repo.findByUsername(username.trim());
        if (existe.isPresent()) {
            model.addAttribute("error", "El usuario ya existe");
            model.addAttribute("usuarios", repo.findAll());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username.trim());
        usuario.setPasswordHash(HashUtil.sha256(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        repo.save(usuario);

        return "redirect:/usuarios/panel-admin?success=created";
    }

    // =========================================
    // VERIFICAR ADMIN
    // =========================================
    @GetMapping("/verificar-admin")
    public String verificarAdmin(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/usuarios/login";
        }

        boolean esAdmin = usuario.getRol() != null
                && usuario.getRol().equalsIgnoreCase("admin");

        model.addAttribute("usuario", usuario);
        model.addAttribute("esAdmin", esAdmin);

        return "verificar_admin";
    }

    // =========================================
    // LOGOUT
    // =========================================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/usuarios/login";
    }
}