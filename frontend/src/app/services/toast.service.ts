import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  constructor(private snackBar: MatSnackBar) {}

  success(message: string) {
    this.snackBar.open(message, 'close', {
      duration: 3000,
      panelClass: ['success-toast'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  error(message: string) {
    this.snackBar.open(message, 'close', {
      duration: 5000,
      panelClass: ['error-toast'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  info(message: string) {
    this.snackBar.open(message, 'close', {
      duration: 3000,
      panelClass: ['info-toast'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }

  warning(message: string) {
    this.snackBar.open(message, 'close', {
      duration: 3000,
      panelClass: ['warning-toast'],
      horizontalPosition: 'right',
      verticalPosition: 'top',
    });
  }
}

