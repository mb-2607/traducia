import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Userpagecomponent } from './userpagecomponent';

describe('Userpagecomponent', () => {
  let component: Userpagecomponent;
  let fixture: ComponentFixture<Userpagecomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Userpagecomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Userpagecomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
