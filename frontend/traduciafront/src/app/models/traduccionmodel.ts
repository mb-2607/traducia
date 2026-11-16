export interface SolicitudTraduccion {
  texto: string;
  origen: string;
  destino: string;
}

export interface ResultadoTraduccion {
  proveedor: string;
  texto: string;
}

export interface TraduccionGuardada {
  id: string;
  titulo: string;
  tipo: 'texto' | 'archivo';
}
