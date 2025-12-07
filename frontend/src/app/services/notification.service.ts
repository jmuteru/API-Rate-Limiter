import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface NotificationRequest {
  recipient: string;
  message: string;
  type?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  sendSMS(request: NotificationRequest, clientId: string): Observable<any> {
    const headers = new HttpHeaders().set('X-Client-Id', clientId);
    return this.http.post(`${this.apiUrl}/notifications/sms`, request, { headers, observe: 'response' });
  }

  sendEmail(request: NotificationRequest, clientId: string): Observable<any> {
    const headers = new HttpHeaders().set('X-Client-Id', clientId);
    return this.http.post(`${this.apiUrl}/notifications/email`, request, { headers, observe: 'response' });
  }
}

