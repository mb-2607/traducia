import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Bibliotecatraduccionescomponent } from './bibliotecatraduccionescomponent';

describe('Bibliotecatraduccionescomponent', () => {
  let component: Bibliotecatraduccionescomponent;
  let fixture: ComponentFixture<Bibliotecatraduccionescomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Bibliotecatraduccionescomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Bibliotecatraduccionescomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
