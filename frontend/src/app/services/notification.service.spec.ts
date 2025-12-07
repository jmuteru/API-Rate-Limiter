import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpHeaders } from '@angular/common/http';
import { NotificationService, NotificationRequest } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [NotificationService]
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('sendSMS', () => {
    it('should send SMS notification with X-Client-Id header', () => {
      const mockRequest: NotificationRequest = {
        recipient: '+1234567890',
        message: 'Test SMS message'
      };
      const clientId = 'test-client-001';
      const mockResponse = {
        status: 200,
        body: {
          status: 'sent',
          type: 'SMS',
          recipient: '+1234567890',
          message: 'SMS sent successfully'
        },
        headers: new HttpHeaders({
          'X-RateLimit-TimeWindow-Limit': '100',
          'X-RateLimit-TimeWindow-Remaining': '99'
        })
      };

      service.sendSMS(mockRequest, clientId).subscribe(response => {
        expect(response.status).toBe(200);
        expect(response.body.type).toBe('SMS');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/notifications/sms');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      expect(req.request.headers.get('X-Client-Id')).toBe(clientId);
      req.flush(mockResponse.body, { headers: mockResponse.headers, status: 200, statusText: 'OK' });
    });
  });

  describe('sendEmail', () => {
    it('should send email notification with X-Client-Id header', () => {
      const mockRequest: NotificationRequest = {
        recipient: 'test@example.com',
        message: 'Test email message'
      };
      const clientId = 'test-client-001';
      const mockResponse = {
        status: 200,
        body: {
          status: 'sent',
          type: 'EMAIL',
          recipient: 'test@example.com',
          message: 'Email sent successfully'
        },
        headers: new HttpHeaders({
          'X-RateLimit-TimeWindow-Limit': '100',
          'X-RateLimit-TimeWindow-Remaining': '99'
        })
      };

      service.sendEmail(mockRequest, clientId).subscribe(response => {
        expect(response.status).toBe(200);
        expect(response.body.type).toBe('EMAIL');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/notifications/email');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      expect(req.request.headers.get('X-Client-Id')).toBe(clientId);
      req.flush(mockResponse.body, { headers: mockResponse.headers, status: 200, statusText: 'OK' });
    });
  });
});

