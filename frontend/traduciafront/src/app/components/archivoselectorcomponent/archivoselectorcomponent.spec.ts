import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAX_ARCHIVO_BYTES } from '../../api-config';
import { Archivoselectorcomponent } from './archivoselectorcomponent';

/** Crea un archivo con el tamaño indicado en bytes; por defecto, un PDF. */
function archivoDe(bytes: number, nombre = 'contrato.pdf'): File {
  return new File([new Uint8Array(bytes)], nombre, { type: 'application/pdf' });
}

/** Simula el input file con el archivo que eligió el usuario. */
function inputCon(archivo: File | null): HTMLInputElement {
  return {
    files: { item: () => archivo },
    value: 'C:\\fakepath\\contrato.pdf',
  } as unknown as HTMLInputElement;
}

/**
 * Pruebas unitarias de {@link Archivoselectorcomponent}.
 *
 * Validan las reglas de selección de archivos (formato admitido, archivo vacío, límite de
 * 10 MB, cancelación y selector deshabilitado), los eventos que se emiten hacia el componente
 * padre y la interacción del botón con el input oculto.
 */
describe('Archivoselectorcomponent', () => {
  let fixture: ComponentFixture<Archivoselectorcomponent>;
  let component: Archivoselectorcomponent;
  let html: HTMLElement;
  let aceptados: File[];
  let rechazos: string[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Archivoselectorcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Archivoselectorcomponent);
    component = fixture.componentInstance;
    html = fixture.nativeElement;

    aceptados = [];
    rechazos = [];
    component.archivoSeleccionado.subscribe((archivo) => aceptados.push(archivo));
    component.archivoRechazado.subscribe((mensaje) => rechazos.push(mensaje));

    await fixture.whenStable();
  });

  /**
   * Verifica que un documento admitido, con contenido y dentro del límite, se emita por
   * `archivoSeleccionado` sin generar rechazos.
   */
  it('emite el archivo cuando es válido', () => {
    const archivo = archivoDe(2048);

    component.seleccionar(inputCon(archivo));

    expect(aceptados).toEqual([archivo]);
    expect(rechazos).toEqual([]);
  });

  /** Verifica que la extensión se reconozca aunque venga en mayúsculas. */
  it('acepta la extensión sin importar mayúsculas', () => {
    component.seleccionar(inputCon(archivoDe(2048, 'CONTRATO.PDF')));

    expect(aceptados).toHaveLength(1);
  });

  /**
   * Verifica que el valor del input se borre después de leerlo, para que el navegador vuelva a
   * disparar `change` si se elige el mismo archivo.
   */
  it('limpia el input para poder elegir otra vez el mismo archivo', () => {
    const input = inputCon(archivoDe(2048));

    component.seleccionar(input);

    expect(input.value).toBe('');
  });

  /** Verifica que un archivo con un formato no admitido se rechace con su mensaje. */
  it('rechaza formatos no admitidos', () => {
    component.seleccionar(inputCon(archivoDe(2048, 'instalador.exe')));

    expect(rechazos).toEqual(['Selecciona TXT, PDF, Word o una imagen admitida.']);
    expect(aceptados).toEqual([]);
  });

  /** Verifica que un archivo sin extensión se trate como un formato no admitido. */
  it('rechaza archivos sin extensión', () => {
    component.seleccionar(inputCon(archivoDe(2048, 'documento')));

    expect(rechazos).toEqual(['Selecciona TXT, PDF, Word o una imagen admitida.']);
  });

  /** Verifica que un archivo de 0 bytes se rechace con el mensaje correspondiente. */
  it('rechaza un archivo vacío', () => {
    component.seleccionar(inputCon(archivoDe(0)));

    expect(rechazos).toEqual(['El archivo está vacío.']);
    expect(aceptados).toEqual([]);
  });

  /** Verifica el caso límite: un archivo de exactamente 10 MB todavía se acepta. */
  it('acepta un archivo de exactamente 10 MB', () => {
    component.seleccionar(inputCon(archivoDe(MAX_ARCHIVO_BYTES)));

    expect(aceptados).toHaveLength(1);
  });

  /** Verifica que un byte por encima de los 10 MB sea suficiente para rechazar el archivo. */
  it('rechaza un archivo de más de 10 MB', () => {
    component.seleccionar(inputCon(archivoDe(MAX_ARCHIVO_BYTES + 1)));

    expect(rechazos).toEqual(['El archivo supera el máximo de 10 MB.']);
    expect(aceptados).toEqual([]);
  });

  /**
   * Verifica que no se emita ningún evento cuando el usuario cierra el explorador sin elegir un
   * archivo.
   */
  it('no hace nada si el usuario cancela la selección', () => {
    component.seleccionar(inputCon(null));

    expect(aceptados).toEqual([]);
    expect(rechazos).toEqual([]);
  });

  /** Verifica que con el selector deshabilitado se ignore el archivo, aunque sea válido. */
  it('ignora la selección cuando está deshabilitado', () => {
    const input = inputCon(archivoDe(2048));
    fixture.componentRef.setInput('deshabilitado', true);

    component.seleccionar(input);

    expect(aceptados).toEqual([]);
    expect(rechazos).toEqual([]);
    expect(input.value).toBe('');
  });

  /** Verifica que el botón visible delegue el clic en el input de tipo file, que está oculto. */
  it('abre el explorador de archivos al pulsar el botón', () => {
    const input = html.querySelector('input[type="file"]') as HTMLInputElement;
    const abrir = vi.spyOn(input, 'click');

    (html.querySelector('button') as HTMLButtonElement).click();

    expect(abrir).toHaveBeenCalled();
  });

  /**
   * Verifica, desde la plantilla, que el evento `change` del input llame a `seleccionar` y emita
   * el archivo.
   */
  it('procesa el archivo cuando cambia el input', () => {
    const input = html.querySelector('input[type="file"]') as HTMLInputElement;
    const archivo = archivoDe(1024, 'carta.pdf');
    Object.defineProperty(input, 'files', { value: { item: () => archivo } });

    input.dispatchEvent(new Event('change'));

    expect(aceptados).toEqual([archivo]);
  });

  /** Verifica que la entrada `deshabilitado` desactive el botón de adjuntar. */
  it('deshabilita el botón cuando el padre lo indica', async () => {
    fixture.componentRef.setInput('deshabilitado', true);
    await fixture.whenStable();

    expect((html.querySelector('button') as HTMLButtonElement).disabled).toBe(true);
  });

  /** Verifica que el atributo `accept` liste las extensiones admitidas con su punto. */
  it('limita los tipos de archivo del selector', () => {
    const accept = html.querySelector('input')?.getAttribute('accept');

    expect(accept).toBe(component.tiposPermitidos);
    expect(accept).toContain('.pdf');
  });
});
