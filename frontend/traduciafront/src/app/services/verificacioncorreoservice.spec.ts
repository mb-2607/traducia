import { TestBed } from '@angular/core/testing';
import { Verificacioncorreoservice } from './verificacioncorreoservice';

describe('Verificacioncorreoservice', () => {
  let service: Verificacioncorreoservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Verificacioncorreoservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
