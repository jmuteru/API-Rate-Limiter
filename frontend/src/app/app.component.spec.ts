import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RateLimitConfigComponent } from './components/rate-limit-config/rate-limit-config.component';
import { SystemLimitConfigComponent } from './components/system-limit-config/system-limit-config.component';
import { NotificationTestComponent } from './components/notification-test/notification-test.component';
import { ClientManagementComponent } from './components/client-management/client-management.component';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AppComponent,
        MatTabsModule,
        NoopAnimationsModule,
        HttpClientTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should have title rate-limiter-frontend', () => {
    expect(component.title).toBe('rate-limiter-frontend');
  });

  it('should initialize with activeTab as 0', () => {
    expect(component.activeTab).toBe(0);
  });

  it('should update activeTab when onTabChange is called', () => {
    component.onTabChange(2);
    expect(component.activeTab).toBe(2);
  });

  it('should call onTabActivated on notificationTest when switching to tab 3', () => {
    const notificationTestComponent = jasmine.createSpyObj('NotificationTestComponent', ['onTabActivated']);
    component.notificationTest = notificationTestComponent;

    component.onTabChange(3);

    expect(notificationTestComponent.onTabActivated).toHaveBeenCalled();
  });

  it('should call onTabActivated on rateLimitConfig when switching to tab 2', () => {
    const rateLimitConfigComponent = jasmine.createSpyObj('RateLimitConfigComponent', ['onTabActivated']);
    component.rateLimitConfig = rateLimitConfigComponent;

    component.onTabChange(2);

    expect(rateLimitConfigComponent.onTabActivated).toHaveBeenCalled();
  });

  it('should not call onTabActivated when switching to other tabs', () => {
    const notificationTestComponent = jasmine.createSpyObj('NotificationTestComponent', ['onTabActivated']);
    const rateLimitConfigComponent = jasmine.createSpyObj('RateLimitConfigComponent', ['onTabActivated']);
    component.notificationTest = notificationTestComponent;
    component.rateLimitConfig = rateLimitConfigComponent;

    component.onTabChange(0);
    component.onTabChange(1);

    expect(notificationTestComponent.onTabActivated).not.toHaveBeenCalled();
    expect(rateLimitConfigComponent.onTabActivated).not.toHaveBeenCalled();
  });
});

