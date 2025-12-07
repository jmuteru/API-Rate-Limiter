import { TestBed } from '@angular/core/testing';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;
  let snackBar: MatSnackBar;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MatSnackBarModule, NoopAnimationsModule],
      providers: [ToastService]
    });
    service = TestBed.inject(ToastService);
    snackBar = TestBed.inject(MatSnackBar);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should show success toast', () => {
    spyOn(snackBar, 'open');
    service.success('Test success message');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Test success message',
      'close',
      jasmine.objectContaining({
        duration: 3000,
        panelClass: ['success-toast'],
        horizontalPosition: 'right',
        verticalPosition: 'top'
      })
    );
  });

  it('should show error toast', () => {
    spyOn(snackBar, 'open');
    service.error('Test error message');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Test error message',
      'close',
      jasmine.objectContaining({
        duration: 5000,
        panelClass: ['error-toast'],
        horizontalPosition: 'right',
        verticalPosition: 'top'
      })
    );
  });

  it('should show info toast', () => {
    spyOn(snackBar, 'open');
    service.info('Test info message');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Test info message',
      'close',
      jasmine.objectContaining({
        duration: 3000,
        panelClass: ['info-toast'],
        horizontalPosition: 'right',
        verticalPosition: 'top'
      })
    );
  });

  it('should show warning toast', () => {
    spyOn(snackBar, 'open');
    service.warning('Test warning message');
    expect(snackBar.open).toHaveBeenCalledWith(
      'Test warning message',
      'close',
      jasmine.objectContaining({
        duration: 3000,
        panelClass: ['warning-toast'],
        horizontalPosition: 'right',
        verticalPosition: 'top'
      })
    );
  });
});

