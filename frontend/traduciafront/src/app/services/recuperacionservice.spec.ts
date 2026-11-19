import { TestBed } from '@angular/core/testing';
import { Recuperacionservice } from './recuperacionservice';

describe('Recuperacionservice', () => {
  let service: Recuperacionservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Recuperacionservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
