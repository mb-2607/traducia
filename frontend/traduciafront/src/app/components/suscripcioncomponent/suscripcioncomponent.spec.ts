import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Suscripcioncomponent } from './suscripcioncomponent';

describe('Suscripcioncomponent', () => {
  let component: Suscripcioncomponent;
  let fixture: ComponentFixture<Suscripcioncomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Suscripcioncomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Suscripcioncomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
