import { TestBed } from '@angular/core/testing';
import { Documentoservice } from './documentoservice';

describe('Documentoservice', () => {
  let service: Documentoservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Documentoservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
