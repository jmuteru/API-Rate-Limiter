import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SystemLimitConfigComponent } from './system-limit-config.component';
import { RateLimitService, SystemLimitConfig } from '../../services/rate-limit.service';
import { of, throwError } from 'rxjs';

describe('SystemLimitConfigComponent', () => {
  let component: SystemLimitConfigComponent;
  let fixture: ComponentFixture<SystemLimitConfigComponent>;
  let rateLimitService: jasmine.SpyObj<RateLimitService>;

  const mockSystemConfig: SystemLimitConfig = {
    id: 1,
    globalRequestsPerSecond: 1000
  };

  beforeEach(async () => {
    const rateLimitServiceSpy = jasmine.createSpyObj('RateLimitService', [
      'getSystemLimits',
      'updateSystemLimits'
    ]);

    await TestBed.configureTestingModule({
      imports: [SystemLimitConfigComponent, HttpClientTestingModule],
      providers: [
        { provide: RateLimitService, useValue: rateLimitServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SystemLimitConfigComponent);
    component = fixture.componentInstance;
    rateLimitService = TestBed.inject(RateLimitService) as jasmine.SpyObj<RateLimitService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load system limits on init', () => {
    rateLimitService.getSystemLimits.and.returnValue(of(mockSystemConfig));

    fixture.detectChanges();

    expect(rateLimitService.getSystemLimits).toHaveBeenCalled();
    expect(component.config).toEqual(mockSystemConfig);
  });

  it('should update system limits when form is submitted', () => {
    const updatedConfig: SystemLimitConfig = {
      id: 1,
      globalRequestsPerSecond: 2000
    };
    component.config = updatedConfig;
    rateLimitService.updateSystemLimits.and.returnValue(of(updatedConfig));
    rateLimitService.getSystemLimits.and.returnValue(of(updatedConfig));

    component.onSubmit();

    expect(rateLimitService.updateSystemLimits).toHaveBeenCalledWith(updatedConfig);
    expect(rateLimitService.getSystemLimits).toHaveBeenCalled();
  });

  it('should handle error when loading system limits fails', () => {
    const error = { status: 500, message: 'Server error' };
    rateLimitService.getSystemLimits.and.returnValue(throwError(() => error));

    fixture.detectChanges();

    expect(component.config.globalRequestsPerSecond).toBe(1000); // default value
  });

  it('should handle error when updating system limits fails', () => {
    const error = { status: 400, message: 'Validation error' };
    rateLimitService.updateSystemLimits.and.returnValue(throwError(() => error));

    component.onSubmit();

    expect(rateLimitService.updateSystemLimits).toHaveBeenCalled();
  });
});

