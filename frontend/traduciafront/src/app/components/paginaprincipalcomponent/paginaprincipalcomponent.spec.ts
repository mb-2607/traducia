import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Paginaprincipalcomponent } from './paginaprincipalcomponent';

describe('Paginaprincipalcomponent', () => {
  let component: Paginaprincipalcomponent;
  let fixture: ComponentFixture<Paginaprincipalcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Paginaprincipalcomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Paginaprincipalcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
