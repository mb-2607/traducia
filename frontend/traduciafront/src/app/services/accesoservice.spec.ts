import { TestBed } from '@angular/core/testing';
import { AccesoService } from './accesoservice';

describe('Accesoservice', () => {
  let service: AccesoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AccesoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
