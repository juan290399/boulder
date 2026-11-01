package com.boulder.boulder.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.boulder.boulder.config.CuentasActivas;
import com.boulder.boulder.dto.RegistroDTO;
import com.boulder.boulder.dto.UsuarioDTO;
import com.boulder.boulder.entities.Rol;
import com.boulder.boulder.entities.Usuario;
import com.boulder.boulder.entities.UsuarioRol;
import com.boulder.boulder.repositories.EmpleadoRepository;
import com.boulder.boulder.repositories.RolRepository;
import com.boulder.boulder.repositories.UsuarioRepository;
import com.boulder.boulder.repositories.UsuarioRolRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarios;
    @Mock private UsuarioRolRepository usuarioRoles;
    @Mock private RolRepository roles;
    @Mock private EmpleadoRepository empleados;
    @Mock private PasswordEncoder codificador;
    @Mock private CuentasActivas cuentasActivas;

    @InjectMocks
    private UsuarioServiceImpl servicio;

    private Usuario carlos;

    @BeforeEach
    void preparar() {
        carlos = new Usuario();
        carlos.setId(UUID.randomUUID());
        carlos.setUsuario("cmendoza");
        carlos.setEstado(true);
        when(usuarios.findById(carlos.getId())).thenReturn(Optional.of(carlos));
        when(usuarios.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(codificador.encode(any())).thenAnswer(i -> "{bcrypt}" + i.getArgument(0));
        when(usuarioRoles.contarUsuariosActivosConRol("ADMIN")).thenReturn(1L);
        when(roles.findByCodigo(any())).thenAnswer(i -> Optional.of(rol(i.getArgument(0))));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private static Rol rol(String codigo) {
        Rol r = new Rol();
        r.setCodigo(codigo);
        return r;
    }

    private static UsuarioRol asignacion(String codigo) {
        UsuarioRol ur = new UsuarioRol();
        ur.setRol(rol(codigo));
        return ur;
    }

    private static void sesionDe(String usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private static UsuarioDTO alta(String usuario, String correo) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setUsuario(usuario);
        dto.setCorreo(correo);
        dto.setContrasenia("Clave2026");
        return dto;
    }

    @Test
    @DisplayName("El alta rechaza un usuario o un correo ya registrados")
    void altaDuplicada() {
        when(usuarios.existsByUsuario("ana")).thenReturn(true);
        when(usuarios.existsByCorreo("ya@boulder.pe")).thenReturn(true);

        assertThatThrownBy(() -> servicio.guardar(alta("ana", "otro@boulder.pe")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ana");
        assertThatThrownBy(() -> servicio.guardar(alta("luis", "ya@boulder.pe")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ya@boulder.pe");
        verify(usuarios, never()).save(any());
    }

    @Test
    @DisplayName("El alta cifra la contraseña, nace activa y con los roles pedidos sin repetir")
    void alta() {
        UsuarioDTO dto = alta("ana", "ana@boulder.pe");
        dto.setRoles(List.of("GEOLOGO", "GEOLOGO", "VISITANTE"));

        servicio.guardar(dto);

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(guardado.capture());
        assertThat(guardado.getValue().getContrasenia()).isEqualTo("{bcrypt}Clave2026");
        assertThat(guardado.getValue().getEstado()).isTrue();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UsuarioRol>> asignadas = ArgumentCaptor.forClass(List.class);
        verify(usuarioRoles).saveAllAndFlush(asignadas.capture());
        assertThat(asignadas.getValue()).extracting(ur -> ur.getRol().getCodigo())
                .containsExactly("GEOLOGO", "VISITANTE");
    }

    @Test
    @DisplayName("Un rol que no está en el catálogo se rechaza")
    void rolInexistente() {
        when(roles.findByCodigo("JEFE")).thenReturn(Optional.empty());
        UsuarioDTO dto = alta("ana", "ana@boulder.pe");
        dto.setRoles(List.of("JEFE"));

        assertThatThrownBy(() -> servicio.guardar(dto)).hasMessageContaining("JEFE");
    }

    @Test
    @DisplayName("El auto-registro nace inactivo y sin roles")
    void autoRegistro() {
        RegistroDTO dto = new RegistroDTO();
        dto.setUsuario("nuevo");
        dto.setCorreo("nuevo@boulder.pe");
        dto.setContrasenia("Clave2026");

        UsuarioDTO r = servicio.registrar(dto);

        assertThat(r.getEstado()).isFalse();
        verify(usuarioRoles, never()).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("Nadie puede desactivar su propia cuenta")
    void desactivarseASiMismo() {
        sesionDe("cmendoza");
        assertThatThrownBy(() -> servicio.cambiarEstado(carlos.getId(), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Desactivar a otro revoca su sesión en el acto")
    void desactivarRevoca() {
        sesionDe("admin");
        servicio.cambiarEstado(carlos.getId(), false);

        assertThat(carlos.getEstado()).isFalse();
        verify(cuentasActivas).olvidar("cmendoza");
    }

    @Test
    @DisplayName("No se puede dejar el sistema sin administradores activos")
    void ultimoAdmin() {
        sesionDe("admin");
        when(usuarioRoles.contarUsuariosActivosConRol("ADMIN")).thenReturn(0L);

        assertThatThrownBy(() -> servicio.cambiarEstado(carlos.getId(), false))
                .hasMessageContaining("administrador");
    }

    @Test
    @DisplayName("Un administrador no puede quitarse a sí mismo el rol ADMIN")
    void quitarsePropioAdmin() {
        sesionDe("cmendoza");
        when(usuarioRoles.findByUsuarioId(carlos.getId())).thenReturn(List.of(asignacion("ADMIN")));

        assertThatThrownBy(() -> servicio.asignarRoles(carlos.getId(), List.of("GEOLOGO")))
                .isInstanceOf(IllegalStateException.class);
        verify(usuarioRoles, never()).deleteByUsuarioId(any());
    }

    @Test
    @DisplayName("Actualizar sin contraseña conserva la anterior")
    void actualizarSinClave() {
        carlos.setContrasenia("{bcrypt}vieja");
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombres("Carlos");
        dto.setContrasenia("   ");

        servicio.actualizar(carlos.getId(), dto);

        assertThat(carlos.getContrasenia()).isEqualTo("{bcrypt}vieja");
        assertThat(carlos.getNombres()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("El DTO trae los roles del usuario y nunca la contraseña")
    void dto() {
        carlos.setContrasenia("{bcrypt}x");
        when(usuarioRoles.findByUsuarioId(carlos.getId())).thenReturn(List.of(asignacion("GEOLOGO")));

        UsuarioDTO r = servicio.obtenerPorId(carlos.getId());

        assertThat(r.getRoles()).containsExactly("GEOLOGO");
        assertThat(r.getContrasenia()).isNull();
    }
}
