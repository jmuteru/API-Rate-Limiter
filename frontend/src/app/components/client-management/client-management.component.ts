import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientService, Client } from '../../services/client.service';

@Component({
  selector: 'app-client-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-management.component.html',
  styleUrls: ['./client-management.component.css']
})
export class ClientManagementComponent implements OnInit, OnDestroy {
  clients: Client[] = [];
  client: Client = this.getDefaultClient();
  showForm = false;
  isEditMode = false;
  message = '';
  messageType = 'info';
  private refreshInterval?: any;

  constructor(private clientService: ClientService) {}

  ngOnInit() {
    this.loadClients();
    // Auto-refresh every 5 seconds
    this.refreshInterval = setInterval(() => {
      if (!this.showForm) {
        this.loadClients();
      }
    }, 5000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  getDefaultClient(): Client {
    return {
      clientId: '',
      name: '',
      description: '',
      contactEmail: ''
    };
  }

  loadClients() {
    this.clientService.getAllClients().subscribe({
      next: (data) => {
        this.clients = data;
      },
      error: (err) => {
        const errorMsg = this.getErrorMessage(err);
        this.showMessage('Error loading clients: ' + errorMsg, 'error');
      }
    });
  }

  onSubmit() {
    if (!this.client.clientId || !this.client.clientId.trim()) {
      this.showMessage('Client ID is required', 'error');
      return;
    }
    if (!this.client.name || !this.client.name.trim()) {
      this.showMessage('Client name is required', 'error');
      return;
    }

    if (this.isEditMode) {
      this.clientService.updateClient(this.client.clientId, this.client).subscribe({
        next: () => {
          this.showMessage('Client updated successfully', 'success');
          this.loadClients();
          this.resetForm();
        },
        error: (err) => {
          const errorMsg = this.getErrorMessage(err);
          this.showMessage('Error updating client: ' + errorMsg, 'error');
        }
      });
    } else {
      this.clientService.createClient(this.client).subscribe({
        next: () => {
          this.showMessage('Client created successfully', 'success');
          this.loadClients();
          this.resetForm();
        },
        error: (err) => {
          const errorMsg = this.getErrorMessage(err);
          this.showMessage('Error creating client: ' + errorMsg, 'error');
        }
      });
    }
  }

  getErrorMessage(err: any): string {
    if (err.error) {
      if (err.error.message) {
        return err.error.message;
      }
      if (typeof err.error === 'string') {
        return err.error;
      }
      if (err.error.errors && Array.isArray(err.error.errors)) {
        return err.error.errors.map((e: any) => e.defaultMessage || e.message).join(', ');
      }
    }
    if (err.message) {
      return err.message;
    }
    if (err.status === 0) {
      return 'Cannot connect to server. Please ensure the backend is running on http://localhost:8080';
    }
    if (err.status === 400) {
      return 'Invalid request. Please check all fields are filled correctly.';
    }
    if (err.status === 409) {
      return 'A client with this ID already exists.';
    }
    return `HTTP ${err.status || 'Unknown'} error occurred`;
  }

  editClient(client: Client) {
    this.client = { ...client };
    this.isEditMode = true;
    this.showForm = true;
  }

  deleteClient(clientId: string) {
    if (confirm(`Are you sure you want to delete client ${clientId}? This will not delete the rate limit configuration.`)) {
      this.clientService.deleteClient(clientId).subscribe({
        next: () => {
          this.showMessage('Client deleted successfully', 'success');
          this.loadClients();
        },
        error: (err) => {
          const errorMsg = this.getErrorMessage(err);
          this.showMessage('Error deleting client: ' + errorMsg, 'error');
        }
      });
    }
  }

  cancelEdit() {
    this.resetForm();
  }

  showNewForm() {
    this.client = this.getDefaultClient();
    this.isEditMode = false;
    this.showForm = true;
  }

  resetForm() {
    this.client = this.getDefaultClient();
    this.isEditMode = false;
    this.showForm = false;
  }

  showMessage(msg: string, type: string) {
    this.message = msg;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }
}

