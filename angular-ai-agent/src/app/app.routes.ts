import { Routes } from '@angular/router';

import { Login } from './login/login';
import { Chat } from './chat/chat';
import { authGuard } from './auth-guard';

export const routes: Routes = [

  {
    path: 'login',
    component: Login
  },

  {
    path: '',
    component: Chat,
    canActivate: [authGuard]
  },

  {
    path: '**',
    redirectTo: ''
  }

];