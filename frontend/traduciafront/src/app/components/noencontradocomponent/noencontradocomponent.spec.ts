import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Noencontradocomponent } from './noencontradocomponent';

describe('Noencontradocomponent', () => {
  let component: Noencontradocomponent;
  let fixture: ComponentFixture<Noencontradocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Noencontradocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Noencontradocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
