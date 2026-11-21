import {DOCUMENT} from '@angular/common';
import {Injectable, inject, signal} from '@angular/core';
const CLAVE_ACEPTACION = 'traduciaTerminosAceptados';
const VERSION_TERMINOS = '1.0';

/**
 * Recuerda la aceptación de términos en este navegador.
 *
 * No sustituye el registro de aceptación asociado a una cuenta
 * que posteriormente implemente el backend.
 */
@Injectable({providedIn: 'root'})
export class TerminosService {
    private readonly documento = inject(DOCUMENT);
    private readonly estadoAceptacion = signal(false);

    readonly aceptados = this.estadoAceptacion.asReadonly();
    readonly persistenciaDisponible = signal(true);

    constructor() {
        this.estadoAceptacion.set(this.leerAceptacion());
    }

    /**
     * Registra una aceptación explícita.
     *
     * Si el navegador bloquea el almacenamiento, la aceptación
     * permanece disponible durante la ejecución actual.
     */
    aceptar(): void {
        this.guardarAceptacion(new Date().toISOString());
        this.estadoAceptacion.set(true);
    }

    /**
     * Una fecha null permite migrar el formato anterior
     * sin inventar cuándo ocurrió aquella aceptación.
     */
    private guardarAceptacion(aceptadoEn: string | null): void {
        try {
            const almacenamiento =
                this.documento.defaultView?.localStorage;

            if (!almacenamiento) {
                this.persistenciaDisponible.set(false);
                return;
            }

            almacenamiento.setItem(
                CLAVE_ACEPTACION,
                JSON.stringify({
                    version: VERSION_TERMINOS,
                    aceptadoEn,
                }),
            );

            this.persistenciaDisponible.set(true);
        } catch {
            this.persistenciaDisponible.set(false);
        }
    }

    /** Comprueba el formato y la versión de la aceptación guardada. */
    private leerAceptacion(): boolean {
        try {
            const almacenamiento =
                this.documento.defaultView?.localStorage;

            if (!almacenamiento) {
                this.persistenciaDisponible.set(false);
                return false;
            }

            const guardado =
                almacenamiento.getItem(CLAVE_ACEPTACION);

            if (guardado === null) {
                return false;
            }

            // Conserva las aceptaciones del componente anterior.
            if (guardado === 'true') {
                this.guardarAceptacion(null);
                return true;
            }

            const registro: unknown = JSON.parse(guardado);

            return (
                typeof registro === 'object' &&
                registro !== null &&
                !Array.isArray(registro) &&
                'version' in registro &&
                registro.version === VERSION_TERMINOS &&
                'aceptadoEn' in registro &&
                (
                    registro.aceptadoEn === null ||
                    (
                        typeof registro.aceptadoEn === 'string' &&
                        Number.isFinite(Date.parse(registro.aceptadoEn))
                    )
                )
            );
        } catch {
            this.persistenciaDisponible.set(false);
            return false;
        }
    }
}