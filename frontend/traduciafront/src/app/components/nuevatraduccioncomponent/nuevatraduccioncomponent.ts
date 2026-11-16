import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {IdiomaService} from '../../services/idiomaservice';
import {Idioma} from '../../models/idiomamodel';
import {ResultadoTraduccion} from '../../models/traduccionmodel';
import {finalize} from 'rxjs';

const sinSoloEspacios: ValidatorFn = (control) =>
  (control.value ?? '').trim().length === 0 ? { vacio: true } : null;

const idiomasDistintos: ValidatorFn = (grupo) => {
  const origen = grupo.get('origen')?.value;
  const destino = grupo.get('destino')?.value;
  return origen && destino && origen === destino ? { mismosIdiomas: true } : null;
};

type Estado = 'reposo' | 'extrayendo' | 'traduciendo';

@Component({
  selector: 'app-nuevatraduccioncomponent',
  standalone: false,
  styleUrl: './nuevatraduccioncomponent.css',
  templateUrl: './nuevatraduccioncomponent.html',
})
export class Nuevatraduccioncomponent implements OnInit {
  private fb = inject(FormBuilder);
  private idiomaService = inject(IdiomaService);
  estado: Estado = 'reposo';
  escuchando = false;
  ocupado = false;
  resultadoLista: ResultadoTraduccion[] = [];
  error = '';

  readonly form = this.fb.nonNullable.group(
    {
      texto: ['', [sinSoloEspacios]],
      origen: ['', Validators.required],
      destino: ['', Validators.required],
    },
    { validators: idiomasDistintos },
  );

  listaIdioma: Idioma[] = [];
  cargandoListaIdioma : boolean = true;

  ngOnInit() {
    this.idiomaService.getListaIdioma().subscribe(
      listaIdioma => {
        this.listaIdioma = listaIdioma;
        this.cargandoListaIdioma = false;
      }
    )
  }

  traducir(){
    console.log('Traducir');
    //TODO: Traduccion Service y toda esa wea
  }

  alSeleccionarArchivo(archivo: File): void {
    this.iniciar('extrayendo');
    //TODO: Archivo service y toda esa wea
    this.agregarTexto("Archivo metido")
  }

  agregarTexto(nuevo: string): void {
    const control = this.form.controls.texto;
    control.setValue([control.value.trimEnd(), nuevo.trim()].filter(Boolean).join(' '));
  }

  private iniciar(estado: Estado): void {
    this.estado = estado;
    this.error = '';
    this.form.disable();
  }

  private terminar(): void {
    this.estado = 'reposo';
    this.form.enable();
  }

  private elegirIdiomasIniciales(): void {
    const ids = this.listaIdioma.map((i) => i.id);
    const { origen, destino } = this.form.controls;
    if (!origen.value) origen.setValue(ids.includes('es') ? 'es' : (ids[0] ?? ''));
    if (!destino.value) destino.setValue(ids.includes('en') ? 'en' : (ids[1] ?? ''));
  }
}
