import { TestBed } from '@angular/core/testing';
import { IdiomaService } from './idiomaservice';

describe('Idiomaservice', () => {
  let service: IdiomaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IdiomaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
