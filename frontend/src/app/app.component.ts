import { Component, ViewChild, AfterViewInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { RateLimitConfigComponent } from './components/rate-limit-config/rate-limit-config.component';
import { SystemLimitConfigComponent } from './components/system-limit-config/system-limit-config.component';
import { NotificationTestComponent } from './components/notification-test/notification-test.component';
import { ClientManagementComponent } from './components/client-management/client-management.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    MatTabsModule,
    RateLimitConfigComponent, 
    SystemLimitConfigComponent, 
    NotificationTestComponent, 
    ClientManagementComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements AfterViewInit {
  title = 'rate-limiter-frontend';
  activeTab = 0;
  @ViewChild('notificationTest') notificationTest?: NotificationTestComponent;
  @ViewChild('rateLimitConfig') rateLimitConfig?: RateLimitConfigComponent;

  ngAfterViewInit() {
  }

  onTabChange(index: number) {
    this.activeTab = index;
    // refresh clients when going to test notifications tab
    if (index === 3 && this.notificationTest) {
      this.notificationTest.onTabActivated();
    }
    // refresh clients when going to test rate limit tab
    if (index === 2 && this.rateLimitConfig) {
      this.rateLimitConfig.onTabActivated();
    }
  }
}

