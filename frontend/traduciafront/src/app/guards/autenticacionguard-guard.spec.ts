import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { autenticacionguardGuard } from './autenticacionguard-guard';

describe('autenticacionguardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => autenticacionguardGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
