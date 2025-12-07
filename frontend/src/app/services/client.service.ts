import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Client {
  id?: number;
  clientId: string;
  name: string;
  description?: string;
  contactEmail?: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createClient(client: Client): Observable<Client> {
    return this.http.post<Client>(`${this.apiUrl}/clients`, client);
  }

  getClient(clientId: string): Observable<Client> {
    return this.http.get<Client>(`${this.apiUrl}/clients/${clientId}`);
  }

  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.apiUrl}/clients`);
  }

  updateClient(clientId: string, client: Client): Observable<Client> {
    return this.http.put<Client>(`${this.apiUrl}/clients/${clientId}`, client);
  }

  deleteClient(clientId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clients/${clientId}`);
  }
}

