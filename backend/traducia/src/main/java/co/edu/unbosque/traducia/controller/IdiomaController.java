package co.edu.unbosque.traducia.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.traducia.dto.IdiomaResponseDTO;
import co.edu.unbosque.traducia.dto.ProveedorResponseDTO;
import co.edu.unbosque.traducia.entity.ProveedorTraduccion;
import co.edu.unbosque.traducia.entity.User;
import co.edu.unbosque.traducia.service.IdiomaService;
import co.edu.unbosque.traducia.service.UserService;

/**
 * Controlador REST encargado de gestionar las solicitudes relacionadas
 * con los idiomas disponibles en la aplicación.
 * <p>
 * Expone endpoints para consultar la lista de idiomas registrados y los
 * proveedores de traducción disponibles según el plan del usuario autenticado.
 * Permite el acceso desde los orígenes configurados mediante CORS.
 */
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "${front.url}",
"http://localhost:4200" })
@RestController
@RequestMapping("/idiomas")
public class IdiomaController {

	/**
	 * Servicio encargado de gestionar las operaciones relacionadas
	 * con los idiomas.
	 */
	private final IdiomaService idiomaServ;

	/**
	 * Servicio utilizado para consultar el plan vigente del usuario autenticado.
	 */
	private final UserService userService;

	/**
	 * Construye una instancia del controlador de idiomas.
	 *
	 * @param idiomaServ  servicio utilizado para realizar las operaciones
	 *                    relacionadas con los idiomas
	 * @param userService servicio utilizado para consultar el plan del usuario
	 */
	public IdiomaController(IdiomaService idiomaServ, UserService userService) {
		this.idiomaServ = idiomaServ;
		this.userService = userService;
	}

	/**
	 * Obtiene la lista de idiomas registrados en el sistema.
	 *
	 * @return respuesta HTTP 200 (OK) que contiene una lista de objetos
	 *         {@link IdiomaResponseDTO} con la información de los idiomas
	 */
	@GetMapping()
	public ResponseEntity<List<IdiomaResponseDTO>> listarIdioma() {
		List<IdiomaResponseDTO> listaIdioma = idiomaServ.listarIdioma();
		return ResponseEntity.ok(listaIdioma);
	}

	/**
	 * Obtiene el catálogo de proveedores de traducción disponibles en el sistema,
	 * indicando cuáles están habilitados para el plan del usuario autenticado.
	 *
	 * @param authentication autenticación del usuario actual; puede ser {@code null}
	 *                       en accesos públicos
	 * @return respuesta HTTP 200 (OK) con la lista de {@link ProveedorResponseDTO}
	 */
	@GetMapping(path = "/proveedores")
	public ResponseEntity<List<ProveedorResponseDTO>> listarProveedores(Authentication authentication) {
		Set<ProveedorTraduccion> permitidos = Set.of();
		if (authentication != null) {
			User usuario = userService.obtenerEntidadPorNombre(authentication.getName());
			if (usuario.getPlanType() != null && usuario.getPlanType().getListaProveedor() != null) {
				permitidos = usuario.getPlanType().getListaProveedor();
			}
		}

		final Set<ProveedorTraduccion> proveedoresPlan = permitidos;
		List<ProveedorResponseDTO> proveedores = Arrays.stream(ProveedorTraduccion.values())
				.map(proveedor -> new ProveedorResponseDTO(
						proveedor.name(),
						String.valueOf(proveedor.getTipoProveedor()),
						proveedoresPlan.contains(proveedor)))
				.toList();
		return ResponseEntity.ok(proveedores);
	}
}