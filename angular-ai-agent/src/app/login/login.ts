import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  errorMessage = '';

  login() {

    if (!this.email || !this.password) {
      this.errorMessage = 'Email and password are required';
      return;
    }

    this.authService.login(
      this.email,
      this.password
    ).subscribe({

      next: () => {
        this.router.navigate(['/']);
      },

      error: (error) => {
        console.error('Login failed:', error);
        this.errorMessage = 'Invalid email or password';
      }

    });
  }
}