import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Landingpagecomponent } from './landingpagecomponent';

describe('Landingpagecomponent', () => {
  let component: Landingpagecomponent;
  let fixture: ComponentFixture<Landingpagecomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Landingpagecomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Landingpagecomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
