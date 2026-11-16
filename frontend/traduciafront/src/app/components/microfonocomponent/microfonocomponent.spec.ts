import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Microfonocomponent } from './microfonocomponent';

describe('Microfonocomponent', () => {
  let component: Microfonocomponent;
  let fixture: ComponentFixture<Microfonocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Microfonocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Microfonocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
