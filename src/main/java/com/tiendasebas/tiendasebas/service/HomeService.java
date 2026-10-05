package com.tiendasebas.tiendasebas.service;

import com.tiendasebas.tiendasebas.entity.HomeBanner;
import com.tiendasebas.tiendasebas.entity.HomeColeccion;
import com.tiendasebas.tiendasebas.repository.HomeBannerRepository;
import com.tiendasebas.tiendasebas.repository.HomeColeccionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HomeService {

    private final HomeBannerRepository homeBannerRepository;
    private final HomeColeccionRepository homeColeccionRepository;

    public HomeService(HomeBannerRepository homeBannerRepository, HomeColeccionRepository homeColeccionRepository) {
        this.homeBannerRepository = homeBannerRepository;
        this.homeColeccionRepository = homeColeccionRepository;
    }

    // =========================================================
    // BANNERS PÚBLICOS
    // =========================================================
    public List<HomeBanner> listarBannersPublicos() {
        return homeBannerRepository.findByEstadoTrueOrderByOrdenAsc();
    }

    // =========================================================
    // BANNER PÚBLICO ANTIGUO
    // Compatibilidad temporal
    // =========================================================
    public HomeBanner obtenerBannerPublico() {
        return homeBannerRepository.findFirstByEstadoTrueOrderByOrdenAsc().orElse(null);
    }

    // =========================================================
    // BANNERS ADMIN
    // =========================================================
    public List<HomeBanner> listarBannersAdmin() {
        return homeBannerRepository.findAllByOrderByOrdenAsc();
    }

    // =========================================================
    // BANNER ADMIN ANTIGUO
    // Compatibilidad temporal
    // =========================================================
    public HomeBanner obtenerBannerAdmin() {
        return homeBannerRepository.findAllByOrderByOrdenAsc().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No existe ningún banner"));
    }

    // =========================================================
    // BUSCAR BANNER
    // =========================================================
    public HomeBanner buscarBanner(Integer idBanner) {
        return homeBannerRepository.findById(idBanner)
                .orElseThrow(() -> new RuntimeException("Banner no encontrado"));
    }

    // =========================================================
    // CREAR BANNER
    // =========================================================
    public HomeBanner crearBanner(HomeBanner datos) {
        HomeBanner banner = new HomeBanner();
        banner.setEtiqueta(datos.getEtiqueta());
        banner.setTitulo(datos.getTitulo());
        banner.setDescripcion(datos.getDescripcion());
        banner.setTextoBoton(datos.getTextoBoton());
        banner.setEnlaceBoton(datos.getEnlaceBoton());
        banner.setOrden(normalizarOrden(datos.getOrden()));
        banner.setEstado(normalizarEstado(datos.getEstado()));

        /*
         * La imagen se carga posteriormente
         * mediante el endpoint multipart.
         */
        banner.setImagen(null);
        return homeBannerRepository.save(banner);
    }

    // =========================================================
    // ACTUALIZAR BANNER
    // =========================================================
    public HomeBanner actualizarBanner(Integer idBanner, HomeBanner datos) {
        HomeBanner banner = buscarBanner(idBanner);
        banner.setEtiqueta(datos.getEtiqueta());
        banner.setTitulo(datos.getTitulo());
        banner.setDescripcion(datos.getDescripcion());
        banner.setTextoBoton(datos.getTextoBoton());
        banner.setEnlaceBoton(datos.getEnlaceBoton());
        banner.setOrden(normalizarOrden(datos.getOrden()));
        banner.setEstado(normalizarEstado(datos.getEstado()));

        /*
         * No modificamos imagen aquí.
         */
        return homeBannerRepository.save(banner);
    }

    // =========================================================
    // ACTUALIZAR IMAGEN BANNER
    // =========================================================
    public HomeBanner actualizarImagenBanner(Integer idBanner, String nombreImagen) {
        HomeBanner banner = buscarBanner(idBanner);
        banner.setImagen(nombreImagen);
        return homeBannerRepository.save(banner);
    }

    // =========================================================
    // ELIMINAR BANNER
    // =========================================================
    public HomeBanner eliminarBanner(Integer idBanner) {
        HomeBanner banner = buscarBanner(idBanner);
        homeBannerRepository.delete(banner);
        return banner;
    }

    // =========================================================
    // COLECCIONES PÚBLICAS
    // =========================================================
    public List<HomeColeccion> listarColeccionesPublicas() {
        return homeColeccionRepository.findByEstadoTrueOrderByOrdenAsc();
    }

    // =========================================================
    // COLECCIONES ADMIN
    // =========================================================
    public List<HomeColeccion> listarColeccionesAdmin() {
        return homeColeccionRepository.findAllByOrderByOrdenAsc();
    }

    // =========================================================
    // BUSCAR COLECCIÓN
    // =========================================================
    public HomeColeccion buscarColeccion(Integer idColeccion) {
        return homeColeccionRepository.findById(idColeccion)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada"));
    }

    // =========================================================
    // CREAR COLECCIÓN
    // =========================================================
    public HomeColeccion crearColeccion(HomeColeccion datos) {
        HomeColeccion coleccion = new HomeColeccion();
        coleccion.setTitulo(datos.getTitulo());
        coleccion.setEtiqueta(datos.getEtiqueta());
        coleccion.setTextoBoton(datos.getTextoBoton());
        coleccion.setEnlace(datos.getEnlace());
        coleccion.setOrden(normalizarOrden(datos.getOrden()));
        coleccion.setEstado(normalizarEstado(datos.getEstado()));

        /*
         * La imagen se sube después.
         */
        coleccion.setImagen(null);
        return homeColeccionRepository.save(coleccion);
    }

    // =========================================================
    // ACTUALIZAR COLECCIÓN
    // =========================================================
    public HomeColeccion actualizarColeccion(Integer idColeccion, HomeColeccion datos) {
        HomeColeccion coleccion = buscarColeccion(idColeccion);
        coleccion.setTitulo(datos.getTitulo());
        coleccion.setEtiqueta(datos.getEtiqueta());
        coleccion.setTextoBoton(datos.getTextoBoton());
        coleccion.setEnlace(datos.getEnlace());
        coleccion.setOrden(normalizarOrden(datos.getOrden()));
        coleccion.setEstado(normalizarEstado(datos.getEstado()));

        /*
         * Conservamos la imagen.
         */
        return homeColeccionRepository.save(coleccion);
    }

    // =========================================================
    // ACTUALIZAR IMAGEN COLECCIÓN
    // =========================================================
    public HomeColeccion actualizarImagenColeccion(Integer idColeccion, String nombreImagen) {
        HomeColeccion coleccion = buscarColeccion(idColeccion);
        coleccion.setImagen(nombreImagen);
        return homeColeccionRepository.save(coleccion);
    }

    // =========================================================
    // ELIMINAR COLECCIÓN
    // =========================================================
    public HomeColeccion eliminarColeccion(Integer idColeccion) {
        HomeColeccion coleccion = buscarColeccion(idColeccion);
        homeColeccionRepository.delete(coleccion);
        return coleccion;
    }

    // =========================================================
    // NORMALIZAR ORDEN
    // =========================================================
    private Integer normalizarOrden(Integer orden) {
        if (orden == null || orden < 1) {
            return 1;
        }
        return orden;
    }

    // =========================================================
    // NORMALIZAR ESTADO
    // =========================================================
    private Boolean normalizarEstado(Boolean estado) {
        return estado != null ? estado : true;
    }
}