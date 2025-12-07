import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RateLimitService, RateLimitConfig } from '../../services/rate-limit.service';
import { ClientService, Client } from '../../services/client.service';

@Component({
  selector: 'app-rate-limit-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rate-limit-config.component.html',
  styleUrls: ['./rate-limit-config.component.css']
})
export class RateLimitConfigComponent implements OnInit, OnDestroy {
  configs: RateLimitConfig[] = [];
  config: RateLimitConfig = this.getDefaultConfig();
  clients: Client[] = [];
  showForm = false;
  isEditMode = false;
  allowManualEntry = false;
  message = '';
  messageType = 'info';
  private refreshInterval?: any;

  constructor(
    private rateLimitService: RateLimitService,
    private clientService: ClientService
  ) {}

  ngOnInit() {
    this.loadConfigs();
    this.loadClients();
    // Auto-refresh every 5 seconds
    this.refreshInterval = setInterval(() => {
      if (!this.showForm) {
        this.loadConfigs();
        this.loadClients();
      }
    }, 5000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  loadClients() {
    this.clientService.getAllClients().subscribe({
      next: (data) => {
        this.clients = data;
      },
      error: (err) => {
        // Don't show error - clients might not be set up yet
      }
    });
  }

  // method to be called when tab becomes active
  onTabActivated() {
    this.loadClients();
    this.loadConfigs();
  }

  onClientSelected() {
    // When a client is selected, we can optionally load their existing config
    if (this.config.clientId && !this.isEditMode) {
      this.rateLimitService.getConfig(this.config.clientId).subscribe({
        next: (existingConfig) => {
          if (confirm('A configuration already exists for this client. Do you want to edit it instead?')) {
            this.editConfig(existingConfig);
          } else {
            this.config.clientId = '';
          }
        },
        error: () => {
          // No existing config, which is fine for new configs
        }
      });
    }
  }

  getDefaultConfig(): RateLimitConfig {
    return {
      clientId: '',
      timeWindowRequests: 100,
      timeWindowSeconds: 60,
      monthlyRequests: 10000,
      throttlingMode: 'HARD'
    };
  }

  loadConfigs() {
    this.rateLimitService.getAllConfigs().subscribe({
      next: (data) => {
        this.configs = data;
      },
      error: (err) => {
        const errorMsg = this.getErrorMessage(err);
        this.showMessage('Error loading configurations: ' + errorMsg, 'error');
      }
    });
  }

  onSubmit() {
    // Validate form
    if (!this.config.clientId || !this.config.clientId.trim()) {
      this.showMessage('Client ID is required', 'error');
      return;
    }
    if (!this.config.timeWindowRequests || this.config.timeWindowRequests < 1) {
      this.showMessage('Time Window Requests must be at least 1', 'error');
      return;
    }
    if (!this.config.timeWindowSeconds || this.config.timeWindowSeconds < 1) {
      this.showMessage('Time Window Seconds must be at least 1', 'error');
      return;
    }
    if (!this.config.monthlyRequests || this.config.monthlyRequests < 1) {
      this.showMessage('Monthly Requests must be at least 1', 'error');
      return;
    }

    if (this.isEditMode) {
      this.rateLimitService.updateConfig(this.config.clientId, this.config).subscribe({
        next: () => {
          this.showMessage('Configuration updated successfully', 'success');
          this.loadConfigs();
          this.resetForm();
        },
        error: (err) => {
          const errorMsg = this.getErrorMessage(err);
          this.showMessage('Error updating configuration: ' + errorMsg, 'error');
        }
      });
    } else {
      this.rateLimitService.createConfig(this.config).subscribe({
        next: () => {
          this.showMessage('Configuration created successfully', 'success');
          this.loadConfigs();
          this.resetForm();
        },
        error: (err) => {
          const errorMsg = this.getErrorMessage(err);
          this.showMessage('Error creating configuration: ' + errorMsg, 'error');
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
      return 'A configuration for this client ID already exists.';
    }
    return `HTTP ${err.status || 'Unknown'} error occurred`;
  }

  editConfig(config: RateLimitConfig) {
    this.config = { ...config };
    this.isEditMode = true;
    this.showForm = true;
  }

  deleteConfig(clientId: string) {
    if (confirm(`Are you sure you want to delete configuration for client ${clientId}?`)) {
      this.rateLimitService.deleteConfig(clientId).subscribe({
        next: () => {
          this.showMessage('Configuration deleted successfully', 'success');
          this.loadConfigs();
        },
        error: (err) => {
          const errorMsg = this.getErrorMessage(err);
          this.showMessage('Error deleting configuration: ' + errorMsg, 'error');
        }
      });
    }
  }

  cancelEdit() {
    this.resetForm();
  }

  showNewForm() {
    this.config = this.getDefaultConfig();
    this.isEditMode = false;
    this.allowManualEntry = false;
    this.showForm = true;
    this.loadClients(); // Refresh clients list
  }

  resetForm() {
    this.config = this.getDefaultConfig();
    this.isEditMode = false;
    this.allowManualEntry = false;
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

