import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RateLimitConfigComponent } from './rate-limit-config.component';
import { RateLimitService, RateLimitConfig } from '../../services/rate-limit.service';
import { ClientService, Client } from '../../services/client.service';
import { of, throwError } from 'rxjs';

describe('RateLimitConfigComponent', () => {
  let component: RateLimitConfigComponent;
  let fixture: ComponentFixture<RateLimitConfigComponent>;
  let rateLimitService: jasmine.SpyObj<RateLimitService>;
  let clientService: jasmine.SpyObj<ClientService>;

  const mockConfigs: RateLimitConfig[] = [
    {
      id: 1,
      clientId: 'test-client-001',
      timeWindowRequests: 100,
      timeWindowSeconds: 60,
      monthlyRequests: 10000,
      throttlingMode: 'HARD'
    }
  ];

  const mockClients: Client[] = [
    {
      id: 1,
      clientId: 'test-client-001',
      name: 'Test Client'
    }
  ];

  beforeEach(async () => {
    const rateLimitServiceSpy = jasmine.createSpyObj('RateLimitService', [
      'getAllConfigs',
      'getConfig',
      'createConfig',
      'updateConfig',
      'deleteConfig'
    ]);
    const clientServiceSpy = jasmine.createSpyObj('ClientService', ['getAllClients']);

    await TestBed.configureTestingModule({
      imports: [RateLimitConfigComponent, HttpClientTestingModule],
      providers: [
        { provide: RateLimitService, useValue: rateLimitServiceSpy },
        { provide: ClientService, useValue: clientServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RateLimitConfigComponent);
    component = fixture.componentInstance;
    rateLimitService = TestBed.inject(RateLimitService) as jasmine.SpyObj<RateLimitService>;
    clientService = TestBed.inject(ClientService) as jasmine.SpyObj<ClientService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load configs and clients on init', () => {
    rateLimitService.getAllConfigs.and.returnValue(of(mockConfigs));
    clientService.getAllClients.and.returnValue(of(mockClients));

    fixture.detectChanges();

    expect(rateLimitService.getAllConfigs).toHaveBeenCalled();
    expect(clientService.getAllClients).toHaveBeenCalled();
    expect(component.configs).toEqual(mockConfigs);
    expect(component.clients).toEqual(mockClients);
  });

  it('should show form when showNewForm is called', () => {
    clientService.getAllClients.and.returnValue(of(mockClients));
    component.showNewForm();
    expect(component.showForm).toBe(true);
    expect(component.isEditMode).toBe(false);
    expect(clientService.getAllClients).toHaveBeenCalled();
  });

  it('should create config when form is submitted in create mode', () => {
    const newConfig: RateLimitConfig = {
      clientId: 'test-client-001',
      timeWindowRequests: 100,
      timeWindowSeconds: 60,
      monthlyRequests: 10000,
      throttlingMode: 'HARD'
    };
    component.config = newConfig;
    component.isEditMode = false;
    rateLimitService.createConfig.and.returnValue(of(newConfig));
    rateLimitService.getAllConfigs.and.returnValue(of(mockConfigs));

    component.onSubmit();

    expect(rateLimitService.createConfig).toHaveBeenCalledWith(newConfig);
    expect(rateLimitService.getAllConfigs).toHaveBeenCalled();
  });

  it('should update config when form is submitted in edit mode', () => {
    const updatedConfig: RateLimitConfig = {
      id: 1,
      clientId: 'test-client-001',
      timeWindowRequests: 200,
      timeWindowSeconds: 60,
      monthlyRequests: 20000,
      throttlingMode: 'SOFT'
    };
    component.config = updatedConfig;
    component.isEditMode = true;
    rateLimitService.updateConfig.and.returnValue(of(updatedConfig));
    rateLimitService.getAllConfigs.and.returnValue(of(mockConfigs));

    component.onSubmit();

    expect(rateLimitService.updateConfig).toHaveBeenCalledWith('test-client-001', updatedConfig);
    expect(rateLimitService.getAllConfigs).toHaveBeenCalled();
  });

  it('should not submit if clientId is empty', () => {
    component.config.clientId = '';
    component.onSubmit();
    expect(rateLimitService.createConfig).not.toHaveBeenCalled();
    expect(rateLimitService.updateConfig).not.toHaveBeenCalled();
  });

  it('should not submit if timeWindowRequests is invalid', () => {
    component.config.clientId = 'test-client-001';
    component.config.timeWindowRequests = 0;
    component.onSubmit();
    expect(rateLimitService.createConfig).not.toHaveBeenCalled();
  });

  it('should delete config when deleteConfig is called', () => {
    rateLimitService.deleteConfig.and.returnValue(of(undefined));
    rateLimitService.getAllConfigs.and.returnValue(of([]));
    spyOn(window, 'confirm').and.returnValue(true);

    component.deleteConfig('test-client-001');

    expect(rateLimitService.deleteConfig).toHaveBeenCalledWith('test-client-001');
    expect(rateLimitService.getAllConfigs).toHaveBeenCalled();
  });

  it('should edit config when editConfig is called', () => {
    const configToEdit = mockConfigs[0];
    component.editConfig(configToEdit);
    expect(component.config).toEqual(configToEdit);
    expect(component.isEditMode).toBe(true);
    expect(component.showForm).toBe(true);
  });

  it('should call onTabActivated and refresh clients', () => {
    clientService.getAllClients.and.returnValue(of(mockClients));
    rateLimitService.getAllConfigs.and.returnValue(of(mockConfigs));

    component.onTabActivated();

    expect(clientService.getAllClients).toHaveBeenCalled();
    expect(rateLimitService.getAllConfigs).toHaveBeenCalled();
  });
});

