import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = () => {

  const token = localStorage.getItem('token');
  const router = inject(Router);

  if (token) {
    return true;
  }

  return router.createUrlTree(['/login']);
};