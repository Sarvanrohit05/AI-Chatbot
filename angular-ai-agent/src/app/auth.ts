import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  login(email: string, password: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/login`,
      { email, password },
      { responseType: 'text' }
    ).pipe(
      tap(token => {
        localStorage.setItem('token', token);
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }
}