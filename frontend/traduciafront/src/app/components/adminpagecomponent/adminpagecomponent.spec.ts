import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Adminpagecomponent } from './adminpagecomponent';

describe('Adminpagecomponent', () => {
  let component: Adminpagecomponent;
  let fixture: ComponentFixture<Adminpagecomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Adminpagecomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Adminpagecomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
