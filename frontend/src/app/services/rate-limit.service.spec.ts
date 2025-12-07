import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RateLimitService, RateLimitConfig, SystemLimitConfig } from './rate-limit.service';

describe('RateLimitService', () => {
  let service: RateLimitService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RateLimitService]
    });
    service = TestBed.inject(RateLimitService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createConfig', () => {
    it('should create a rate limit configuration', () => {
      const mockConfig: RateLimitConfig = {
        clientId: 'test-client-001',
        timeWindowRequests: 100,
        timeWindowSeconds: 60,
        monthlyRequests: 10000,
        throttlingMode: 'HARD'
      };

      service.createConfig(mockConfig).subscribe(config => {
        expect(config).toEqual(mockConfig);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/rate-limits');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockConfig);
      req.flush(mockConfig);
    });
  });

  describe('getConfig', () => {
    it('should get a rate limit configuration by client id', () => {
      const mockConfig: RateLimitConfig = {
        id: 1,
        clientId: 'test-client-001',
        timeWindowRequests: 100,
        timeWindowSeconds: 60,
        monthlyRequests: 10000,
        throttlingMode: 'HARD'
      };

      service.getConfig('test-client-001').subscribe(config => {
        expect(config).toEqual(mockConfig);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/rate-limits/test-client-001');
      expect(req.request.method).toBe('GET');
      req.flush(mockConfig);
    });
  });

  describe('getAllConfigs', () => {
    it('should get all rate limit configurations', () => {
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

      service.getAllConfigs().subscribe(configs => {
        expect(configs).toEqual(mockConfigs);
        expect(configs.length).toBe(1);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/rate-limits');
      expect(req.request.method).toBe('GET');
      req.flush(mockConfigs);
    });
  });

  describe('updateConfig', () => {
    it('should update a rate limit configuration', () => {
      const mockConfig: RateLimitConfig = {
        id: 1,
        clientId: 'test-client-001',
        timeWindowRequests: 200,
        timeWindowSeconds: 60,
        monthlyRequests: 20000,
        throttlingMode: 'SOFT'
      };

      service.updateConfig('test-client-001', mockConfig).subscribe(config => {
        expect(config).toEqual(mockConfig);
        expect(config.throttlingMode).toBe('SOFT');
      });

      const req = httpMock.expectOne('http://localhost:8080/api/rate-limits/test-client-001');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockConfig);
      req.flush(mockConfig);
    });
  });

  describe('deleteConfig', () => {
    it('should delete a rate limit configuration', () => {
      service.deleteConfig('test-client-001').subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne('http://localhost:8080/api/rate-limits/test-client-001');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('getSystemLimits', () => {
    it('should get system limits', () => {
      const mockSystemConfig: SystemLimitConfig = {
        id: 1,
        globalRequestsPerSecond: 1000
      };

      service.getSystemLimits().subscribe(config => {
        expect(config).toEqual(mockSystemConfig);
        expect(config.globalRequestsPerSecond).toBe(1000);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/system/limits');
      expect(req.request.method).toBe('GET');
      req.flush(mockSystemConfig);
    });
  });

  describe('updateSystemLimits', () => {
    it('should update system limits', () => {
      const mockSystemConfig: SystemLimitConfig = {
        id: 1,
        globalRequestsPerSecond: 2000
      };

      service.updateSystemLimits(mockSystemConfig).subscribe(config => {
        expect(config).toEqual(mockSystemConfig);
        expect(config.globalRequestsPerSecond).toBe(2000);
      });

      const req = httpMock.expectOne('http://localhost:8080/api/system/limits');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(mockSystemConfig);
      req.flush(mockSystemConfig);
    });
  });
});

