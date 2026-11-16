import {ChangeDetectorRef, Component, DestroyRef, inject, OnInit} from '@angular/core';
import {DOCUMENT} from '@angular/common';
import {TemaService} from '../../services/temaservice';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

const PANTALLA_MOVIL = '(max-width: 44rem)';

@Component({
  selector: 'app-asideusercomponent',
  standalone: false,
  styleUrl: './asideusercomponent.css',
  templateUrl: './asideusercomponent.html',
  host: { '[class.plegado]': '!panelAbierto' },
})
export class AsideUserComponent implements OnInit {

  private cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  private destroyRef = inject(DestroyRef);
  temaService: TemaService = inject(TemaService);

  private readonly documento: Document = inject(DOCUMENT);
  panelAbierto: boolean = !(this.documento.defaultView?.matchMedia?.('(max-width: 44rem)').matches ?? false);
  temaOscuro = false;

  ngOnInit() {
    this.temaService.getTemaOscuroAsObservable().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(
      (data: boolean) => {
        this.temaOscuro = data;
      }
    )
  }

  alternarTema(): void {
    this.temaService.setTemaOscuro(!this.temaOscuro);
    this.cdr.markForCheck();
  }
}
