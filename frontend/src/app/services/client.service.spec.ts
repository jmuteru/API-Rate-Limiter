import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClientService, Client } from './client.service';

describe('ClientService', () => {
  let service: ClientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClientService]
    });
    service = TestBed.inject(ClientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createClient', () => {
    it('should create a client', () => {
      const mockClient: Client = {
        clientId: 'test-client-001',
        name: 'Test Client',
        description: 'Test description',
        contactEmail: 'test@example.com'
      };

      service.createClient(mockClient).subscribe(client => {
        expect(client).toEqual(mockClient);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/clients');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockClient);
      req.flush(mockClient);
    });
  });

  describe('getClient', () => {
    it('should get a client by id', () => {
      const mockClient: Client = {
        id: 1,
        clientId: 'test-client-001',
        name: 'Test Client',
        description: 'Test description',
        contactEmail: 'test@example.com'
      };

      service.getClient('test-client-001').subscribe(client => {
        expect(client).toEqual(mockClient);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/clients/test-client-001');
      expect(req.request.method).toBe('GET');
      req.flush(mockClient);
    });
  });

  describe('getAllClients', () => {
    it('should get all clients', () => {
      const mockClients: Client[] = [
        {
          id: 1,
          clientId: 'test-client-001',
          name: 'Test Client 1',
          contactEmail: 'test1@example.com'
        },
        {
          id: 2,
          clientId: 'test-client-002',
          name: 'Test Client 2',
          contactEmail: 'test2@example.com'
        }
      ];

      service.getAllClients().subscribe(clients => {
        expect(clients).toEqual(mockClients);
        expect(clients.length).toBe(2);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/clients');
      expect(req.request.method).toBe('GET');
      req.flush(mockClients);
    });
  });

  describe('updateClient', () => {
    it('should update a client', () => {
      const mockClient: Client = {
        id: 1,
        clientId: 'test-client-001',
        name: 'Updated Client',
        description: 'Updated description',
        contactEmail: 'updated@example.com'
      };

      service.updateClient('test-client-001', mockClient).subscribe(client => {
        expect(client).toEqual(mockClient);
        expect(client.name).toBe('Updated Client');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/clients/test-client-001');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockClient);
      req.flush(mockClient);
    });
  });

  describe('deleteClient', () => {
    it('should delete a client', () => {
      service.deleteClient('test-client-001').subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne('http://localhost:8080/api/clients/test-client-001');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});

