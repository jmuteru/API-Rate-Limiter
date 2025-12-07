import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface RateLimitConfig {
  id?: number;
  clientId: string;
  timeWindowRequests: number;
  timeWindowSeconds: number;
  monthlyRequests: number;
  throttlingMode: 'SOFT' | 'HARD';
  createdAt?: string;
  updatedAt?: string;
}

export interface SystemLimitConfig {
  id?: number;
  globalRequestsPerSecond: number;
}

@Injectable({
  providedIn: 'root'
})
export class RateLimitService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createConfig(config: RateLimitConfig): Observable<RateLimitConfig> {
    return this.http.post<RateLimitConfig>(`${this.apiUrl}/rate-limits`, config);
  }

  getConfig(clientId: string): Observable<RateLimitConfig> {
    return this.http.get<RateLimitConfig>(`${this.apiUrl}/rate-limits/${clientId}`);
  }

  getAllConfigs(): Observable<RateLimitConfig[]> {
    return this.http.get<RateLimitConfig[]>(`${this.apiUrl}/rate-limits`);
  }

  updateConfig(clientId: string, config: RateLimitConfig): Observable<RateLimitConfig> {
    return this.http.put<RateLimitConfig>(`${this.apiUrl}/rate-limits/${clientId}`, config);
  }

  deleteConfig(clientId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rate-limits/${clientId}`);
  }

  getSystemLimits(): Observable<SystemLimitConfig> {
    return this.http.get<SystemLimitConfig>(`${this.apiUrl}/system/limits`);
  }

  updateSystemLimits(config: SystemLimitConfig): Observable<SystemLimitConfig> {
    return this.http.put<SystemLimitConfig>(`${this.apiUrl}/system/limits`, config);
  }
}

