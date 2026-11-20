import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { administradorguardGuard } from './administradorguard-guard';

describe('administradorguardGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => administradorguardGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
