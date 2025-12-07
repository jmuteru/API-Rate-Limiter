import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { NotificationTestComponent } from './notification-test.component';
import { NotificationService, NotificationRequest } from '../../services/notification.service';
import { ClientService, Client } from '../../services/client.service';
import { ToastService } from '../../services/toast.service';
import { of, throwError } from 'rxjs';

describe('NotificationTestComponent', () => {
  let component: NotificationTestComponent;
  let fixture: ComponentFixture<NotificationTestComponent>;
  let notificationService: jasmine.SpyObj<NotificationService>;
  let clientService: jasmine.SpyObj<ClientService>;
  let toastService: jasmine.SpyObj<ToastService>;

  const mockClients: Client[] = [
    {
      id: 1,
      clientId: 'test-client-001',
      name: 'Test Client'
    }
  ];

  const mockResponse = new HttpResponse({
    status: 200,
    body: {
      status: 'sent',
      type: 'EMAIL',
      recipient: 'test@example.com',
      message: 'Email sent successfully'
    },
    headers: new HttpHeaders({
      'x-ratelimit-timewindow-limit': '100',
      'x-ratelimit-timewindow-remaining': '99'
    })
  });

  beforeEach(async () => {
    const notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['sendEmail', 'sendSMS']);
    const clientServiceSpy = jasmine.createSpyObj('ClientService', ['getAllClients']);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['success', 'error', 'warning', 'info']);

    await TestBed.configureTestingModule({
      imports: [NotificationTestComponent, HttpClientTestingModule],
      providers: [
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: ClientService, useValue: clientServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationTestComponent);
    component = fixture.componentInstance;
    notificationService = TestBed.inject(NotificationService) as jasmine.SpyObj<NotificationService>;
    clientService = TestBed.inject(ClientService) as jasmine.SpyObj<ClientService>;
    toastService = TestBed.inject(ToastService) as jasmine.SpyObj<ToastService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load clients on init', () => {
    clientService.getAllClients.and.returnValue(of(mockClients));

    fixture.detectChanges();

    expect(clientService.getAllClients).toHaveBeenCalled();
    expect(component.clients).toEqual(mockClients);
  });

  it('should send email notification successfully', () => {
    component.clientId = 'test-client-001';
    component.request = {
      recipient: 'test@example.com',
      message: 'Test message'
    };
    notificationService.sendEmail.and.returnValue(of(mockResponse));

    component.sendNotification();

    expect(notificationService.sendEmail).toHaveBeenCalledWith(
      component.request,
      'test-client-001'
    );
    expect(component.results.length).toBe(1);
    expect(toastService.success).toHaveBeenCalled();
  });

  it('should send SMS notification successfully', () => {
    component.clientId = 'test-client-001';
    component.notificationType = 'sms';
    component.request = {
      recipient: '+1234567890',
      message: 'Test message'
    };
    notificationService.sendSMS.and.returnValue(of(mockResponse));

    component.sendNotification();

    expect(notificationService.sendSMS).toHaveBeenCalledWith(
      component.request,
      'test-client-001'
    );
    expect(toastService.success).toHaveBeenCalled();
  });

  it('should not send notification if fields are empty', () => {
    component.clientId = '';
    component.sendNotification();
    expect(notificationService.sendEmail).not.toHaveBeenCalled();
    expect(notificationService.sendSMS).not.toHaveBeenCalled();
    expect(toastService.error).toHaveBeenCalledWith('Please fill in all fields');
  });

  it('should handle 429 rate limit error', () => {
    component.clientId = 'test-client-001';
    component.request = {
      recipient: 'test@example.com',
      message: 'Test message'
    };
    const error = new HttpErrorResponse({
      status: 429,
      statusText: 'Too Many Requests',
      headers: new HttpHeaders({
        'x-ratelimit-limit': '100',
        'x-ratelimit-remaining': '0'
      })
    });
    notificationService.sendEmail.and.returnValue(throwError(() => error));

    component.sendNotification();

    expect(component.results.length).toBe(1);
    expect(component.results[0].status).toBe(429);
    expect(toastService.warning).toHaveBeenCalled();
  });

  it('should handle other errors', () => {
    component.clientId = 'test-client-001';
    component.request = {
      recipient: 'test@example.com',
      message: 'Test message'
    };
    const error = new HttpErrorResponse({
      status: 500,
      statusText: 'Internal Server Error'
    });
    notificationService.sendEmail.and.returnValue(throwError(() => error));

    component.sendNotification();

    expect(component.results.length).toBe(1);
    expect(toastService.error).toHaveBeenCalled();
  });

  it('should send multiple notifications', () => {
    component.clientId = 'test-client-001';
    component.request = {
      recipient: 'test@example.com',
      message: 'Test message'
    };
    notificationService.sendEmail.and.returnValue(of(mockResponse));

    component.sendMultiple();

    expect(notificationService.sendEmail).toHaveBeenCalledTimes(10);
    expect(toastService.info).toHaveBeenCalledWith('All requests completed');
  });

  it('should call onTabActivated and refresh clients', () => {
    clientService.getAllClients.and.returnValue(of(mockClients));

    component.onTabActivated();

    expect(clientService.getAllClients).toHaveBeenCalled();
  });

  it('should check if headers exist', () => {
    expect(component.hasHeaders({})).toBe(false);
    expect(component.hasHeaders({ 'x-header': 'value' })).toBe(true);
    expect(component.hasHeaders(null as any)).toBe(false);
    expect(component.hasHeaders(undefined as any)).toBe(false);
  });
});

