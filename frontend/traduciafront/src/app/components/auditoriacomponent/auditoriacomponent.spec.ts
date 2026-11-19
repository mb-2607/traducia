import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Auditoriacomponent } from './auditoriacomponent';

describe('Auditoriacomponent', () => {
  let component: Auditoriacomponent;
  let fixture: ComponentFixture<Auditoriacomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Auditoriacomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Auditoriacomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
