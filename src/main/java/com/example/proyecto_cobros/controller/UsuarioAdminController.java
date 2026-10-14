package com.example.proyecto_cobros.controller;

import com.example.proyecto_cobros.entity.Usuario;
import com.example.proyecto_cobros.repository.UsuarioRepository;
import com.example.proyecto_cobros.security.HashUtil;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios/admin")
public class UsuarioAdminController {

    @Autowired
    private UsuarioRepository repo;

    // =========================================
    // VALIDAR ADMIN
    // =========================================

    private boolean esAdmin(HttpSession session){

        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        return usuario != null
                && usuario.getRol() != null
                && usuario.getRol().equalsIgnoreCase("admin");
    }

    // =========================================
    // VALIDAR CONTRASEÑA ADMIN
    // =========================================

    private boolean validarPasswordAdmin(
            HttpSession session,
            String passwordAdmin
    ){

        Usuario admin =
                (Usuario) session.getAttribute("usuario");

        if(admin == null){
            return false;
        }

        if(passwordAdmin == null
                || passwordAdmin.trim().isEmpty()){

            return false;
        }

        String hash =
                HashUtil.sha256(passwordAdmin);

        return hash.equals(admin.getPasswordHash());
    }

    // =========================================
    // PANEL ADMIN
    // =========================================

    @GetMapping
    public String panelAdmin(
            HttpSession session,
            Model model
    ){

        if(!esAdmin(session)){
            return "redirect:/usuarios";
        }

        model.addAttribute(
                "usuarios",
                repo.findAll()
        );

        if(!model.containsAttribute("usuarioObj")){

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );
        }

        return "admin_usuarios";
    }

    // =========================================
    // CREAR USUARIO
    // =========================================

    @PostMapping("/crear")
    public String crearUsuario(

            @RequestParam String username,

            @RequestParam String password,

            @RequestParam String confirmarPassword,

            @RequestParam String rol,

            @RequestParam String passwordAdmin,

            HttpSession session,

            Model model

    ){

        if(!esAdmin(session)){
            return "redirect:/usuarios";
        }

        // VALIDAR PASSWORD ADMIN

        if(!validarPasswordAdmin(
                session,
                passwordAdmin
        )){

            model.addAttribute(
                    "error",
                    "Contraseña de administrador incorrecta"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );

            return "admin_usuarios";
        }

        // VALIDAR USERNAME

        if(username == null
                || username.trim().isEmpty()){

            model.addAttribute(
                    "error",
                    "El usuario es obligatorio"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );

            return "admin_usuarios";
        }

        // VALIDAR PASSWORD

        if(password == null
                || password.trim().isEmpty()){

            model.addAttribute(
                    "error",
                    "La contraseña es obligatoria"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );

            return "admin_usuarios";
        }

        if(password.length() < 4){

            model.addAttribute(
                    "error",
                    "La contraseña debe tener mínimo 4 caracteres"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );

            return "admin_usuarios";
        }

        // VALIDAR CONFIRMACION

        if(!password.equals(confirmarPassword)){

            model.addAttribute(
                    "error",
                    "Las contraseñas no coinciden"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );

            return "admin_usuarios";
        }

        Usuario usuario = new Usuario();

        usuario.setUsername(
                username.trim()
        );

        usuario.setPasswordHash(
                HashUtil.sha256(password)
        );

        usuario.setRol(rol);

        usuario.setActivo(true);

        usuario.setFechaCreacion(
                LocalDateTime.now()
        );

        repo.save(usuario);

        return "redirect:/usuarios/admin?success=created";
    }

    // =========================================
    // EDITAR
    // =========================================

    @GetMapping("/editar/{id}")
    public String editarUsuario(

            @PathVariable Integer id,

            HttpSession session,

            Model model

    ){

        if(!esAdmin(session)){
            return "redirect:/usuarios";
        }

        Usuario usuario =
                repo.findById(id).orElse(null);

        model.addAttribute(
                "usuarioObj",
                usuario
        );

        model.addAttribute(
                "usuarios",
                repo.findAll()
        );

        return "admin_usuarios";
    }

    // =========================================
    // ACTUALIZAR
    // =========================================

    @PostMapping("/actualizar")
    public String actualizarUsuario(

            @RequestParam Integer id,

            @RequestParam String username,

            @RequestParam String password,

            @RequestParam String confirmarPassword,

            @RequestParam String rol,

            @RequestParam Boolean activo,

            @RequestParam String passwordAdmin,

            HttpSession session,

            Model model

    ){

        if(!esAdmin(session)){
            return "redirect:/usuarios";
        }

        // VALIDAR PASSWORD ADMIN

        if(!validarPasswordAdmin(
                session,
                passwordAdmin
        )){

            model.addAttribute(
                    "error",
                    "Contraseña de administrador incorrecta"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            return "admin_usuarios";
        }

        Optional<Usuario> optional =
                repo.findById(id);

        if(optional.isEmpty()){
            return "redirect:/usuarios/admin";
        }

        Usuario usuario = optional.get();

        // VALIDAR USERNAME

        if(username == null
                || username.trim().isEmpty()){

            model.addAttribute(
                    "error",
                    "El usuario es obligatorio"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    usuario
            );

            return "admin_usuarios";
        }

        // PASSWORD OBLIGATORIA AL MODIFICAR

        if(password == null
                || password.trim().isEmpty()){

            model.addAttribute(
                    "error",
                    "Debe escribir una nueva contraseña"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    usuario
            );

            return "admin_usuarios";
        }

        if(password.length() < 4){

            model.addAttribute(
                    "error",
                    "La contraseña debe tener mínimo 4 caracteres"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    usuario
            );

            return "admin_usuarios";
        }

        if(!password.equals(confirmarPassword)){

            model.addAttribute(
                    "error",
                    "Las contraseñas no coinciden"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    usuario
            );

            return "admin_usuarios";
        }

        usuario.setUsername(
                username.trim()
        );

        usuario.setRol(rol);

        usuario.setActivo(activo);

        usuario.setPasswordHash(
                HashUtil.sha256(password)
        );

        repo.save(usuario);

        return "redirect:/usuarios/admin?success=updated";
    }

    // =========================================
    // ELIMINAR
    // =========================================

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(

            @PathVariable Integer id,

            @RequestParam String passwordAdmin,

            HttpSession session,

            Model model

    ){

        if(!esAdmin(session)){
            return "redirect:/usuarios";
        }

        // VALIDAR PASSWORD ADMIN

        if(!validarPasswordAdmin(
                session,
                passwordAdmin
        )){

            model.addAttribute(
                    "error",
                    "Contraseña de administrador incorrecta"
            );

            model.addAttribute(
                    "usuarios",
                    repo.findAll()
            );

            model.addAttribute(
                    "usuarioObj",
                    new Usuario()
            );

            return "admin_usuarios";
        }

        repo.deleteById(id);

        return "redirect:/usuarios/admin?success=deleted";
    }
}