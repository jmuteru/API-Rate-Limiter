import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationService, NotificationRequest } from '../../services/notification.service';
import { ClientService, Client } from '../../services/client.service';
import { ToastService } from '../../services/toast.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-notification-test',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notification-test.component.html',
  styleUrls: ['./notification-test.component.css']
})
export class NotificationTestComponent implements OnInit, OnDestroy {
  clientId = '';
  clients: Client[] = [];
  notificationType = 'email';
  request: NotificationRequest = {
    recipient: '',
    message: ''
  };
  loading = false;
  results: any[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    private notificationService: NotificationService,
    private clientService: ClientService,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  private refreshInterval?: any;

  ngOnInit() {
    this.loadClients();
    // Auto-refresh clients every 5 seconds
    this.refreshInterval = setInterval(() => {
      this.loadClients();
    }, 5000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  // call when tab is active
  onTabActivated() {
    this.loadClients();
  }

  loadClients() {
    this.clientService.getAllClients().subscribe({
      next: (data) => {
        this.clients = data || [];
      },
      error: (err) => {
        const errorMsg = this.getErrorMessage(err);
        // only show error if it's not a connection issue (might be temporary)
        if (err.status !== 0) {
          this.toastService.error('error loading clients: ' + errorMsg.toLowerCase());
        }
        this.clients = []; // make sure array is empty on error
      }
    });
  }

  getErrorMessage(err: any): string {
    if (err.error) {
      if (err.error.message) {
        return err.error.message;
      }
      if (typeof err.error === 'string') {
        return err.error;
      }
    }
    if (err.message) {
      return err.message;
    }
    if (err.status === 0) {
      return 'Cannot connect to server. Please ensure the backend is running on http://localhost:8080';
    }
    if (err.status === 404) {
      return 'Clients endpoint not found. Please restart the backend server.';
    }
    return `HTTP ${err.status || 'Unknown'} error occurred`;
  }

  sendNotification() {
    if (!this.clientId || !this.request.recipient || !this.request.message) {
      this.toastService.error('Please fill in all fields');
      return;
    }

    this.loading = true;
    this.results = []; // clear previous results
    const service = this.notificationType === 'email' 
      ? this.notificationService.sendEmail(this.request, this.clientId)
      : this.notificationService.sendSMS(this.request, this.clientId);

    service.subscribe({
      next: (response: any) => {
        const headers: any = {};
        if (response.headers) {
          // get all header names
          const headerKeys = response.headers.keys();
          headerKeys.forEach((key: string) => {
            const value = response.headers.get(key);
            headers[key.toLowerCase()] = value;
          });
        }
        
        const status = response.status || 200;
        this.results = [{
          status: status,
          message: response.body?.message || 'Success',
          headers: headers
        }];
        
        this.cdr.detectChanges(); // Force change detection
        this.toastService.success('Notification sent successfully');
        this.loading = false;
      },
      error: (err: HttpErrorResponse | any) => {
        const headers: any = {};
        
        // try to get headers from error response 
        if (err instanceof HttpErrorResponse && err.headers) {
          try {
            err.headers.keys().forEach((key: string) => {
              headers[key.toLowerCase()] = err.headers.get(key);
            });
          } catch (e) {
            // Silently handle header extraction errors
          }
        } else if (err.headers) {
          // fallback for other error types
          try {
            if (typeof err.headers.keys === 'function') {
              err.headers.keys().forEach((key: string) => {
                headers[key.toLowerCase()] = err.headers.get(key);
              });
            }
          } catch (e) {
            // Silently handle header extraction errors
          }
        }
        
        // figure out the message based on status code
        let message = 'Request failed';
        if (err.status === 429) {
          message = 'Rate limit exceeded (429) .This is expected when testing rate limiting';
        } else if (err.error?.message) {
          message = err.error.message;
        } else if (err.message) {
          message = err.message;
        }
        
        const result = {
          status: err.status || 'Error',
          message: message,
          headers: headers
        };
        this.results = [result];
        this.cdr.detectChanges(); // force change detection
        
        // show different toast based on status
        if (err.status === 429) {
          this.toastService.warning('Rate limit exceeded - Check results table for details');
        } else {
          this.toastService.error('Error: ' + result.message);
        }
        this.loading = false;
      }
    });
  }

  sendMultiple() {
    if (!this.clientId || !this.request.recipient || !this.request.message) {
      this.toastService.error('Please fill in all fields');
      return;
    }

    this.loading = true;
    this.results = [];
    
    const requests = [];
    for (let i = 0; i < 10; i++) {
      const service = this.notificationType === 'email'
        ? this.notificationService.sendEmail(this.request, this.clientId)
        : this.notificationService.sendSMS(this.request, this.clientId);
      requests.push(service);
    }

    let completed = 0;
    requests.forEach((req, index) => {
      req.subscribe({
        next: (response: any) => {
          const headers: any = {};
          if (response.headers) {
            const headerKeys = response.headers.keys();
            headerKeys.forEach((key: string) => {
              const value = response.headers.get(key);
              headers[key.toLowerCase()] = value;
            });
          }
          
          const status = response.status || 200;
          this.results[index] = {
            status: status,
            message: response.body?.message || 'Success',
            headers: headers
          };
          
          completed++;
          if (completed === 10) {
            this.loading = false;
            this.toastService.info('All requests completed');
          }
        },
        error: (err: any) => {
          const headers: any = {};
          if (err.headers) {
            try {
              err.headers.keys().forEach((key: string) => {
                headers[key.toLowerCase()] = err.headers.get(key);
              });
            } catch (e) {
              // Silently handle header extraction errors
            }
          }
          
          let message = 'Rate limit exceeded';
          if (err.status === 429) {
            message = 'Rate limit exceeded. Status Code:429';
          } else if (err.error?.message) {
            message = err.error.message;
          } else if (err.message) {
            message = err.message;
          }
          
          this.results[index] = {
            status: err.status || 429,
            message: message,
            headers: headers
          };
          
          completed++;
          if (completed === 10) {
            this.loading = false;
            this.cdr.detectChanges(); // Force change detection
            this.toastService.info('All requests completed');
          }
        }
      });
    });
  }

  hasHeaders(headers: any): boolean {
    return headers && typeof headers === 'object' && Object.keys(headers).length > 0;
  }
}

