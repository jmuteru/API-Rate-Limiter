import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RateLimitService, SystemLimitConfig } from '../../services/rate-limit.service';

@Component({
  selector: 'app-system-limit-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './system-limit-config.component.html',
  styleUrls: ['./system-limit-config.component.css']
})
export class SystemLimitConfigComponent implements OnInit, OnDestroy {
  config: SystemLimitConfig = { globalRequestsPerSecond: 1000 };
  message = '';
  messageType = 'info';
  private refreshInterval?: any;

  constructor(private rateLimitService: RateLimitService) {}

  ngOnInit() {
    this.loadConfig();
    // Auto-refresh every 5 seconds
    this.refreshInterval = setInterval(() => {
      this.loadConfig();
    }, 5000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  loadConfig() {
    this.rateLimitService.getSystemLimits().subscribe({
      next: (data) => {
        this.config = data;
      },
      error: (err) => {
        this.showMessage('Error loading system limits: ' + err.message, 'error');
      }
    });
  }

  onSubmit() {
    this.rateLimitService.updateSystemLimits(this.config).subscribe({
      next: () => {
        this.showMessage('System limits updated successfully', 'success');
        this.loadConfig();
      },
      error: (err) => {
        this.showMessage('Error updating system limits: ' + err.message, 'error');
      }
    });
  }

  showMessage(msg: string, type: string) {
    this.message = msg;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }
}

