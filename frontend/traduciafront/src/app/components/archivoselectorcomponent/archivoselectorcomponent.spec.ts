import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Archivoselectorcomponent } from './archivoselectorcomponent';

describe('Archivoselectorcomponent', () => {
  let component: Archivoselectorcomponent;
  let fixture: ComponentFixture<Archivoselectorcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Archivoselectorcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Archivoselectorcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
