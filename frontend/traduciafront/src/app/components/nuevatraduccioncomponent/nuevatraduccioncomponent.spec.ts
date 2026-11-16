import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Nuevatraduccioncomponent } from './nuevatraduccioncomponent';

describe('Nuevatraduccioncomponent', () => {
  let component: Nuevatraduccioncomponent;
  let fixture: ComponentFixture<Nuevatraduccioncomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Nuevatraduccioncomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Nuevatraduccioncomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
