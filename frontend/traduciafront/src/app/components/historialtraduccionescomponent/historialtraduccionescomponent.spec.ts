import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Historialtraduccionescomponent } from './historialtraduccionescomponent';

describe('Historialtraduccionescomponent', () => {
  let component: Historialtraduccionescomponent;
  let fixture: ComponentFixture<Historialtraduccionescomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Historialtraduccionescomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Historialtraduccionescomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
