import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Terminocomponent } from './terminocomponent';

describe('Terminocomponent', () => {
  let component: Terminocomponent;
  let fixture: ComponentFixture<Terminocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Terminocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Terminocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
