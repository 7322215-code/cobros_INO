package com.example.proyecto_cobros.controller;

import com.example.proyecto_cobros.entity.Mensualidad;
import com.example.proyecto_cobros.entity.Pago;
import com.example.proyecto_cobros.repository.MensualidadRepository;
import com.example.proyecto_cobros.repository.PagoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoRepository repo;

    @Autowired
    private MensualidadRepository mensualidadRepo;

    @Autowired
    private JdbcTemplate jdbc;

    @ModelAttribute("usuarios")
    public List<Map<String, Object>> usuarios() {
        return jdbc.queryForList("SELECT id_usuario, username FROM usuarios ORDER BY username");
    }

    // ---------- LISTAR ----------
    @GetMapping
    public String listarPagos(Model model) {
        model.addAttribute("listaPagos", repo.findAll());
        model.addAttribute("pago", new Pago());
        model.addAttribute("mensualidades", mensualidadRepo.findByPagadoFalseOrderByAnioAscMesAsc());
        return "pagos";
    }

    @GetMapping({"/guardar", "/actualizar", "/generar"})
    public String volver() {
        return "redirect:/pagos";
    }

    // ---------- GUARDAR ----------
    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute Pago pago, RedirectAttributes ra, Principal principal) {
        return procesar(pago, ra, principal, false);
    }

    // ---------- ACTUALIZAR ----------
    @PostMapping("/actualizar")
    public String actualizarPago(@ModelAttribute Pago pago, RedirectAttributes ra, Principal principal) {
        return procesar(pago, ra, principal, true);
    }

    private String procesar(Pago pago, RedirectAttributes ra, Principal principal, boolean actualizando) {
        try {
            if (pago.getIdMensualidad() == null) {
                ra.addFlashAttribute("error", "⚠️ Selecciona una mensualidad.");
                return "redirect:/pagos";
            }

            Mensualidad m = mensualidadRepo.findById(pago.getIdMensualidad()).orElse(null);
            if (m == null) {
                ra.addFlashAttribute("error", "⚠️ La mensualidad seleccionada no existe.");
                return "redirect:/pagos";
            }

            boolean esEdicion = actualizando && pago.getIdPago() != null;

            if (!esEdicion && Boolean.TRUE.equals(m.getPagado())) {
                ra.addFlashAttribute("error", "⚠️ Esa mensualidad ya está pagada.");
                return "redirect:/pagos";
            }

            Integer idUsuario = resolverUsuario(principal, pago.getIdUsuario());
            if (idUsuario == null) {
                ra.addFlashAttribute("error", "⚠️ No se pudo identificar al usuario. Selecciónalo en la lista.");
                return "redirect:/pagos";
            }
            pago.setIdUsuario(idUsuario);

            if (pago.getActivo() == null) pago.setActivo(true);
            if (pago.getEstado() == null || pago.getEstado().isBlank()) pago.setEstado("Pagado");

            if (esEdicion) {
                Pago anterior = repo.findById(pago.getIdPago()).orElse(null);
                if (anterior != null && anterior.getIdMensualidad() != null
                        && !anterior.getIdMensualidad().equals(pago.getIdMensualidad())) {
                    mensualidadRepo.findById(anterior.getIdMensualidad()).ifPresent(old -> {
                        old.setPagado(false);
                        mensualidadRepo.save(old);
                    });
                }
            }

            if (m.getEstudiante() != null) {
                pago.setEstudiante(m.getEstudiante().getNombreCompleto());
            }

            repo.save(pago);

            m.setPagado("Pagado".equals(pago.getEstado()));
            mensualidadRepo.save(m);

            ra.addFlashAttribute("mensaje", esEdicion
                    ? "✅ Pago actualizado correctamente."
                    : "✅ Pago guardado correctamente.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "⚠️ No se pudo guardar el pago. Revisa los datos e inténtalo de nuevo.");
        }
        return "redirect:/pagos";
    }

    private Integer resolverUsuario(Principal principal, Integer idFormulario) {
        if (principal != null) {
            List<Integer> r = jdbc.queryForList(
                    "SELECT id_usuario FROM usuarios WHERE username = ?",
                    Integer.class, principal.getName());
            if (!r.isEmpty()) return r.get(0);
        }
        if (idFormulario != null) {
            Integer c = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM usuarios WHERE id_usuario = ?",
                    Integer.class, idFormulario);
            if (c != null && c > 0) return idFormulario;
        }
        return null;
    }

    // ---------- BUSCAR ----------
    @GetMapping("/buscar")
    public String buscarPago(@RequestParam("estudiante") String estudiante, Model model) {
        List<Pago> filtrados = repo.findAll().stream()
                .filter(p -> p.getEstudiante() != null
                        && p.getEstudiante().toLowerCase().contains(estudiante.toLowerCase()))
                .toList();

        if (filtrados.isEmpty()) {
            model.addAttribute("error", "⚠️ No se encontraron pagos para: " + estudiante);
        } else {
            model.addAttribute("mensaje", "✅ Se encontraron " + filtrados.size() + " pagos.");
        }

        model.addAttribute("listaPagos", filtrados);
        model.addAttribute("pago", new Pago());
        model.addAttribute("mensualidades", mensualidadRepo.findByPagadoFalseOrderByAnioAscMesAsc());
        return "pagos";
    }

    // ---------- FILTRAR POR ESTADO ----------
    @GetMapping("/filtrar")
    public String filtrar(@RequestParam("estado") String estado, Model model) {
        List<Pago> filtrados = repo.findAll().stream()
                .filter(p -> estado.equalsIgnoreCase(p.getEstado()))
                .toList();
        model.addAttribute("listaPagos", filtrados);
        model.addAttribute("pago", new Pago());
        model.addAttribute("mensualidades", mensualidadRepo.findByPagadoFalseOrderByAnioAscMesAsc());
        model.addAttribute("mensaje", "Mostrando pagos: " + estado + " (" + filtrados.size() + ")");
        return "pagos";
    }

    // ---------- GENERAR MENSUALIDADES ----------
    @PostMapping("/generar")
    public String generar(@RequestParam Integer anio,
                          @RequestParam Integer mesDesde,
                          @RequestParam Integer mesHasta,
                          @RequestParam(required = false) BigDecimal monto,
                          RedirectAttributes ra) {
        try {
            int creadas = jdbc.update(
                "INSERT INTO mensualidades (id_estudiante, anio, mes, monto, pagado) " +
                "SELECT e.id_estudiante, CAST(? AS integer), m, " +
                "COALESCE(CAST(? AS numeric), " +
                "  (SELECT x.monto FROM mensualidades x WHERE x.id_estudiante = e.id_estudiante " +
                "   ORDER BY x.anio DESC, x.mes DESC LIMIT 1), 25.00), false " +
                "FROM estudiantes e " +
                "CROSS JOIN generate_series(CAST(? AS integer), CAST(? AS integer)) AS m " +
                "WHERE e.activo = true " +
                "AND NOT EXISTS (SELECT 1 FROM mensualidades y " +
                "  WHERE y.id_estudiante = e.id_estudiante AND y.anio = CAST(? AS integer) AND y.mes = m)",
                anio, monto, mesDesde, mesHasta, anio);
            ra.addFlashAttribute("mensaje", "✅ Se crearon " + creadas + " mensualidades.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "⚠️ No se pudieron generar las mensualidades.");
        }
        return "redirect:/pagos";
    }

    // ---------- EDITAR ----------
    @GetMapping("/editar/{id}")
    public String editarPago(@PathVariable Integer id, Model model) {
        Pago pago = repo.findById(id).orElse(new Pago());
        model.addAttribute("pago", pago);
        model.addAttribute("listaPagos", repo.findAll());
        model.addAttribute("mensualidades", mensualidadRepo.findAll());
        return "pagos";
    }

    // ---------- ELIMINAR ----------
    @GetMapping("/eliminar/{id}")
    public String eliminarPago(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Pago pago = repo.findById(id).orElse(null);
            if (pago != null) {
                if (pago.getIdMensualidad() != null) {
                    mensualidadRepo.findById(pago.getIdMensualidad()).ifPresent(m -> {
                        m.setPagado(false);
                        mensualidadRepo.save(m);
                    });
                }
                repo.deleteById(id);
            }
            ra.addFlashAttribute("mensaje", "🗑️ Pago eliminado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "⚠️ No se pudo eliminar el pago.");
        }
        return "redirect:/pagos";
    }

    // ---------- FACTURA ----------
    @GetMapping("/factura/{id}")
    public String factura(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Pago pago = repo.findById(id).orElse(null);
        if (pago == null) {
            ra.addFlashAttribute("error", "⚠️ El pago no existe.");
            return "redirect:/pagos";
        }
        Mensualidad m = pago.getIdMensualidad() == null ? null
                : mensualidadRepo.findById(pago.getIdMensualidad()).orElse(null);
        if (m == null) {
            ra.addFlashAttribute("error", "⚠️ Este pago no tiene mensualidad asociada.");
            return "redirect:/pagos";
        }
        String usuario = jdbc.queryForList(
                "SELECT username FROM usuarios WHERE id_usuario = ?",
                String.class, pago.getIdUsuario()).stream().findFirst().orElse("-");

        model.addAttribute("pago", pago);
        model.addAttribute("mensualidad", m);
        model.addAttribute("usuario", usuario);
        return "factura_pago";
    }
}