import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Loggincomponent } from './loggincomponent';

describe('Loggincomponent', () => {
  let component: Loggincomponent;
  let fixture: ComponentFixture<Loggincomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Loggincomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Loggincomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
