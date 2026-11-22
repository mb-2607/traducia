import { TestBed } from '@angular/core/testing';
import { Transcripcionservice } from './transcripcionservice';

describe('Transcripcionservice', () => {
  let service: Transcripcionservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Transcripcionservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
