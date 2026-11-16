import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Idiomaselectorcomponent } from './idiomaselectorcomponent';

describe('Idiomaselectorcomponent', () => {
  let component: Idiomaselectorcomponent;
  let fixture: ComponentFixture<Idiomaselectorcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Idiomaselectorcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Idiomaselectorcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
