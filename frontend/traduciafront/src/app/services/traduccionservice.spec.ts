import { TestBed } from '@angular/core/testing';
import { Traduccionservice } from './traduccionservice';

describe('Traduccionservice', () => {
  let service: Traduccionservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Traduccionservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
