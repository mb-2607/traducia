import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbarcomponent',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbarcomponent.html',
  styleUrls: ['./navbarcomponent.css'],

  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Navbarcomponent {
  @Input() logoSrc = 'assets/logoempresa.png';
  @Input() planRoute = '/planes';
  @Input() profileRoute = '/perfil';
}
