import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { API_BASE_URL } from '../../../../../../../../../../Downloads/Traducia_Front_Documentos_Audio/Traducia_Front_Documentos_Audio/src/app/api-config';
import { AuthService } from '../../services/authservice';
import { IdiomaService } from '../../services/idiomaservice';
import { Archivoselectorcomponent } from '../../../../../../../../../../Downloads/Traducia_Front_Documentos_Audio/Traducia_Front_Documentos_Audio/src/app/components/archivoselectorcomponent/archivoselectorcomponent';
import { Idiomaselectorcomponent } from '../idiomaselectorcomponent/idiomaselectorcomponent';
import { Nuevatraduccioncomponent } from './nuevatraduccioncomponent';

/** Comprueba la integración del editor con HttpClient sin acceder a la red. */
describe('Nuevatraduccioncomponent: integración HTTP', () => {
  let componente: Nuevatraduccioncomponent;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [Nuevatraduccioncomponent, Idiomaselectorcomponent, Archivoselectorcomponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: 'https://backend.example.test' },
        { provide: AuthService, useValue: {
          getJwt: () => 'jwt-de-prueba',
          getUserAsObservable: () => of({ plan: 'infinitia' }),
        } },
        { provide: IdiomaService, useValue: { getListaIdioma: () => of([
          { id: 'es', nombre: 'Español', voz: 'es-CO' },
          { id: 'en', nombre: 'Inglés', voz: 'en-US' },
        ]) } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(Nuevatraduccioncomponent);
    componente = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    TestBed.resetTestingModule();
  });

  it('conserva todo el texto extraído y permite editarlo cuando supera el plan', () => {
    componente.form.controls.texto.setValue('Previo');
    componente.alSeleccionarArchivo(new File(['%PDF-1.7'], 'documento.pdf'));
    expect(componente.ocupado()).toBe(true);
    const peticion = http.expectOne((req) => req.url.endsWith('/extraer-documento'));
    expect(peticion.request.headers.has('Content-Type')).toBe(false);
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer jwt-de-prueba');
    peticion.flush({ texto: 'Texto completo', caracteres: 14, limiteCaracteres: 10, excedeLimite: true });
    expect(componente.form.controls.texto.value).toBe('Previo\n\nTexto completo');
    expect(componente.form.controls.texto.hasError('maxlength')).toBe(true);
    expect(componente.form.enabled).toBe(true);
    componente.form.controls.texto.setValue('Corto');
    expect(componente.form.valid).toBe(true);
  });

  it('libera el formulario cuando el backend rechaza una extracción', () => {
    componente.form.controls.texto.setValue('Conservar');
    componente.alSeleccionarArchivo(new File(['%PDF-1.7'], 'documento.pdf'));
    http.expectOne((req) => req.url.endsWith('/extraer-documento')).flush(
      { detail: 'Documento rechazado' }, { status: 422, statusText: 'Unprocessable Content' },
    );
    expect(componente.error()).toBe('Documento rechazado');
    expect(componente.form.controls.texto.value).toBe('Conservar');
    expect(componente.ocupado()).toBe(false);
    expect(componente.form.enabled).toBe(true);
  });
});
