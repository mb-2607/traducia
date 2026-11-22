import { TestBed } from '@angular/core/testing';
import { Usuariosadminservice } from './usuariosadminservice';

describe('Usuariosadminservice', () => {
  let service: Usuariosadminservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Usuariosadminservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
