import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Recuperarcontrasenacomponent } from './recuperarcontrasenacomponent';

describe('Recuperarcontrasenacomponent', () => {
  let component: Recuperarcontrasenacomponent;
  let fixture: ComponentFixture<Recuperarcontrasenacomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Recuperarcontrasenacomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Recuperarcontrasenacomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
