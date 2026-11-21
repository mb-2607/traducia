import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Verificarcorreocomponent } from './verificarcorreocomponent';

describe('Verificarcorreocomponent', () => {
  let component: Verificarcorreocomponent;
  let fixture: ComponentFixture<Verificarcorreocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Verificarcorreocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Verificarcorreocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
