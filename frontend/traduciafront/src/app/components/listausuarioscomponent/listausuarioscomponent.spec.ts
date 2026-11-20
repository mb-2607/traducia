import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Listausuarioscomponent } from './listausuarioscomponent';

describe('Listausuarioscomponent', () => {
  let component: Listausuarioscomponent;
  let fixture: ComponentFixture<Listausuarioscomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Listausuarioscomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Listausuarioscomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
