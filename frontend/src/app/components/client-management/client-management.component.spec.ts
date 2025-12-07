import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ClientManagementComponent } from './client-management.component';
import { ClientService, Client } from '../../services/client.service';
import { of, throwError } from 'rxjs';

describe('ClientManagementComponent', () => {
  let component: ClientManagementComponent;
  let fixture: ComponentFixture<ClientManagementComponent>;
  let clientService: jasmine.SpyObj<ClientService>;

  const mockClients: Client[] = [
    {
      id: 1,
      clientId: 'test-client-001',
      name: 'Test Client',
      description: 'Test description',
      contactEmail: 'test@example.com'
    }
  ];

  beforeEach(async () => {
    const clientServiceSpy = jasmine.createSpyObj('ClientService', [
      'getAllClients',
      'createClient',
      'updateClient',
      'deleteClient'
    ]);

    await TestBed.configureTestingModule({
      imports: [ClientManagementComponent, HttpClientTestingModule],
      providers: [
        { provide: ClientService, useValue: clientServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientManagementComponent);
    component = fixture.componentInstance;
    clientService = TestBed.inject(ClientService) as jasmine.SpyObj<ClientService>;
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

  it('should show form when showNewForm is called', () => {
    component.showNewForm();
    expect(component.showForm).toBe(true);
    expect(component.isEditMode).toBe(false);
  });

  it('should reset form when resetForm is called', () => {
    component.showForm = true;
    component.isEditMode = true;
    component.resetForm();
    expect(component.showForm).toBe(false);
    expect(component.isEditMode).toBe(false);
  });

  it('should create client when form is submitted in create mode', () => {
    const newClient: Client = {
      clientId: 'new-client',
      name: 'New Client',
      description: 'New description',
      contactEmail: 'new@example.com'
    };
    component.client = newClient;
    component.isEditMode = false;
    clientService.createClient.and.returnValue(of(newClient));
    clientService.getAllClients.and.returnValue(of(mockClients));

    component.onSubmit();

    expect(clientService.createClient).toHaveBeenCalledWith(newClient);
    expect(clientService.getAllClients).toHaveBeenCalled();
  });

  it('should update client when form is submitted in edit mode', () => {
    const updatedClient: Client = {
      id: 1,
      clientId: 'test-client-001',
      name: 'Updated Client',
      description: 'Updated description',
      contactEmail: 'updated@example.com'
    };
    component.client = updatedClient;
    component.isEditMode = true;
    clientService.updateClient.and.returnValue(of(updatedClient));
    clientService.getAllClients.and.returnValue(of(mockClients));

    component.onSubmit();

    expect(clientService.updateClient).toHaveBeenCalledWith('test-client-001', updatedClient);
    expect(clientService.getAllClients).toHaveBeenCalled();
  });

  it('should not submit if clientId is empty', () => {
    component.client.clientId = '';
    component.onSubmit();
    expect(clientService.createClient).not.toHaveBeenCalled();
    expect(clientService.updateClient).not.toHaveBeenCalled();
  });

  it('should not submit if name is empty', () => {
    component.client.clientId = 'test-client';
    component.client.name = '';
    component.onSubmit();
    expect(clientService.createClient).not.toHaveBeenCalled();
    expect(clientService.updateClient).not.toHaveBeenCalled();
  });

  it('should delete client when deleteClient is called', () => {
    clientService.deleteClient.and.returnValue(of(undefined));
    clientService.getAllClients.and.returnValue(of([]));
    spyOn(window, 'confirm').and.returnValue(true);

    component.deleteClient('test-client-001');

    expect(clientService.deleteClient).toHaveBeenCalledWith('test-client-001');
    expect(clientService.getAllClients).toHaveBeenCalled();
  });

  it('should not delete client if confirm is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteClient('test-client-001');

    expect(clientService.deleteClient).not.toHaveBeenCalled();
  });

  it('should handle error when loading clients fails', () => {
    const error = { status: 500, message: 'Server error' };
    clientService.getAllClients.and.returnValue(throwError(() => error));

    fixture.detectChanges();

    expect(component.clients).toEqual([]);
  });

  it('should edit client when editClient is called', () => {
    const clientToEdit = mockClients[0];
    component.editClient(clientToEdit);
    expect(component.client).toEqual(clientToEdit);
    expect(component.isEditMode).toBe(true);
    expect(component.showForm).toBe(true);
  });
});

