import { TestBed } from '@angular/core/testing';
import { Temaservice } from './temaservice';

describe('Temaservice', () => {
  let service: Temaservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Temaservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
