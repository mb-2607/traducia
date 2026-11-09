import { TestBed } from '@angular/core/testing';
import { Suscripcionservice } from './suscripcionservice';

describe('Suscripcionservice', () => {
  let service: Suscripcionservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Suscripcionservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
