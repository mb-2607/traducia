import {inject, Injectable} from '@angular/core';
import {StorageService} from './storageservice';
import {BehaviorSubject} from 'rxjs';
import {DOCUMENT} from '@angular/common';

const TEMA_KEY = 'tema';

@Injectable({
  providedIn: 'root'
})
export class TemaService {
  private readonly documento: Document = inject(DOCUMENT);
  temaOscuro :BehaviorSubject<boolean>;
  constructor(private storageService: StorageService) {
    this.temaOscuro = new BehaviorSubject<boolean>(storageService.get(TEMA_KEY) ?? true);
    this.aplicarTema(this.temaOscuro.value);
  }

  getTemaOscuroAsObservable(){
    return this.temaOscuro.asObservable();
  }

  setTemaOscuro(valor: boolean) {
    this.temaOscuro.next(valor);
    this.storageService.set(TEMA_KEY, valor);
    this.aplicarTema(this.temaOscuro.value);
  }

  aplicarTema(oscuro :boolean) : void {
    this.documento.documentElement.dataset['tema'] = oscuro ? 'oscuro' : 'claro';
  }
}
