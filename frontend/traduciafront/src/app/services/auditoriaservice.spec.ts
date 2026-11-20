import { TestBed } from '@angular/core/testing';
import { Auditoriaservice } from './auditoriaservice';

describe('Auditoriaservice', () => {
  let service: Auditoriaservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Auditoriaservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
