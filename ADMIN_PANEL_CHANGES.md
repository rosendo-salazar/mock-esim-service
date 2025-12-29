# Admin Panel Changes for Maya Mock Integration

This document outlines the changes needed in `fly-roamy-admin` to integrate with the mock Maya service and enable plan synchronization.

## Overview

The admin panel needs the following enhancements:
1. Environment configuration for mock vs production Maya
2. "Sync from Maya" button on Plans page
3. Plan source indicator (Maya vs Manual)
4. Sync status display

---

## 1. Environment Configuration

### Update Environment Files

**File: `src/environments/environment.ts` (Development)**
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  agentApiUrl: 'http://localhost:8081/api',

  // Add Maya Mock configuration
  mayaApiUrl: 'http://localhost:8082/v1',
  mayaApiKey: 'maya_test_key',
  mayaApiSecret: 'maya_test_secret',
  useMockMaya: true
};
```

**File: `src/environments/environment.stage.ts` (Staging)**
```typescript
export const environment = {
  production: false,
  apiUrl: 'https://api-stage.roamyhub.com/api',
  agentApiUrl: 'https://stage-agent-cbgje0e8guhzbxb3.eastus-01.azurewebsites.net/api',

  // Maya Mock on Azure
  mayaApiUrl: 'https://mock-maya-stage.azurewebsites.net/v1',
  mayaApiKey: process.env['MAYA_MOCK_API_KEY'] || '',
  mayaApiSecret: process.env['MAYA_MOCK_API_SECRET'] || '',
  useMockMaya: true
};
```

**File: `src/environments/environment.prod.ts` (Production)**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.roamyhub.com/api',

  // Real Maya API (when available)
  mayaApiUrl: 'https://api.maya.net/v1',
  useMockMaya: false
};
```

---

## 2. Plan Model Updates

### Update Plan Interface

**File: `src/app/core/models/plan.model.ts`**

Add these fields to track sync status:

```typescript
export interface Plan {
  // Existing fields...
  id: string;
  productId?: number;
  name: string;
  data: number;
  days: number;
  price: number;
  currencyPrices?: { [currency: string]: number };
  packageType: 'country' | 'region' | 'global';
  countries: string[];
  badge?: string;
  isActive: boolean;

  // NEW: Maya sync fields
  provider?: string;           // 'maya' | 'manual'
  providerId?: string;         // Maya bundle ID
  lastSyncedAt?: string;       // Last sync timestamp
  syncStatus?: 'synced' | 'modified' | 'new' | 'removed';
}

// New interface for sync results
export interface SyncResult {
  success: boolean;
  created: number;
  updated: number;
  removed: number;
  errors: string[];
  timestamp: string;
}
```

---

## 3. API Service Updates

### Add Sync Endpoint to Plan Service

**File: `src/app/core/api/admin-plan-api.service.ts`**

Add these methods:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Plan, SyncResult } from '../models/plan.model';

@Injectable({
  providedIn: 'root'
})
export class AdminPlanApiService {
  private apiUrl = `${environment.apiUrl}/admin/plans`;

  constructor(private http: HttpClient) {}

  // Existing methods...

  /**
   * Sync plans from Maya API
   * Calls fly-roamy-api which then fetches from Maya mock
   */
  syncFromMaya(): Observable<SyncResult> {
    return this.http.post<SyncResult>(`${this.apiUrl}/sync`, {});
  }

  /**
   * Get last sync status
   */
  getSyncStatus(): Observable<{
    lastSyncAt: string;
    totalMayaPlans: number;
    totalManualPlans: number;
  }> {
    return this.http.get<any>(`${this.apiUrl}/sync/status`);
  }

  /**
   * Mark plan as manual (exclude from sync)
   */
  markAsManual(planId: string): Observable<Plan> {
    return this.http.put<Plan>(`${this.apiUrl}/${planId}/mark-manual`, {});
  }
}
```

---

## 4. Plans Component Updates

### Add Sync Button and Status

**File: `src/app/features/plans/plans.component.ts`**

Add to the component class:

```typescript
import { AdminPlanApiService } from '../../core/api/admin-plan-api.service';
import { SyncResult } from '../../core/models/plan.model';

export class PlansComponent implements OnInit {
  // Existing properties...

  // NEW: Sync-related properties
  isSyncing = false;
  lastSyncResult: SyncResult | null = null;
  syncError: string | null = null;

  constructor(
    private planApiService: AdminPlanApiService,
    // ... other dependencies
  ) {}

  /**
   * Sync plans from Maya API
   */
  syncFromMaya(): void {
    if (this.isSyncing) return;

    this.isSyncing = true;
    this.syncError = null;

    this.planApiService.syncFromMaya().subscribe({
      next: (result) => {
        this.lastSyncResult = result;
        this.isSyncing = false;

        // Refresh the plan list
        this.refreshCache();

        // Show success toast
        this.showToast(
          `Sync complete: ${result.created} created, ${result.updated} updated`,
          'success'
        );
      },
      error: (error) => {
        this.syncError = error.message || 'Sync failed';
        this.isSyncing = false;
        this.showToast('Sync failed: ' + this.syncError, 'error');
      }
    });
  }

  /**
   * Get badge class for plan source
   */
  getPlanSourceBadge(plan: Plan): { text: string; class: string } {
    if (plan.provider === 'maya') {
      return { text: 'Maya', class: 'bg-blue-100 text-blue-800' };
    }
    return { text: 'Manual', class: 'bg-gray-100 text-gray-800' };
  }

  /**
   * Get sync status badge
   */
  getSyncStatusBadge(plan: Plan): { text: string; class: string } | null {
    if (!plan.syncStatus) return null;

    switch (plan.syncStatus) {
      case 'synced':
        return { text: 'Synced', class: 'bg-green-100 text-green-800' };
      case 'modified':
        return { text: 'Modified', class: 'bg-yellow-100 text-yellow-800' };
      case 'new':
        return { text: 'New', class: 'bg-purple-100 text-purple-800' };
      case 'removed':
        return { text: 'Removed', class: 'bg-red-100 text-red-800' };
      default:
        return null;
    }
  }
}
```

### Update Template

**File: `src/app/features/plans/plans.component.html`**

Add sync button in the header section:

```html
<!-- Add after existing action buttons -->
<div class="flex items-center gap-2">
  <!-- Existing buttons... -->

  <!-- Sync from Maya button -->
  <button
    (click)="syncFromMaya()"
    [disabled]="isSyncing"
    class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
  >
    <svg
      *ngIf="isSyncing"
      class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle
        class="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        stroke-width="4"
      ></circle>
      <path
        class="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
      ></path>
    </svg>
    <svg
      *ngIf="!isSyncing"
      class="-ml-1 mr-2 h-4 w-4"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
      ></path>
    </svg>
    {{ isSyncing ? 'Syncing...' : 'Sync from Maya' }}
  </button>
</div>

<!-- Sync result notification -->
<div
  *ngIf="lastSyncResult"
  class="mt-4 p-4 bg-green-50 border border-green-200 rounded-md"
>
  <div class="flex items-center">
    <svg class="h-5 w-5 text-green-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
      <path
        fill-rule="evenodd"
        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
        clip-rule="evenodd"
      ></path>
    </svg>
    <span class="text-green-800">
      Last sync: {{ lastSyncResult.created }} created,
      {{ lastSyncResult.updated }} updated
    </span>
  </div>
</div>

<!-- In the plan table, add source column -->
<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
  Source
</th>

<!-- In the table body -->
<td class="px-6 py-4 whitespace-nowrap">
  <span
    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
    [ngClass]="getPlanSourceBadge(plan).class"
  >
    {{ getPlanSourceBadge(plan).text }}
  </span>
  <span
    *ngIf="getSyncStatusBadge(plan)"
    class="ml-1 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
    [ngClass]="getSyncStatusBadge(plan)?.class"
  >
    {{ getSyncStatusBadge(plan)?.text }}
  </span>
</td>
```

---

## 5. Backend Changes (fly-roamy-api)

### New Sync Endpoint

**File: `src/main/java/com/flyroamy/api/fly_api/controller/AdminPlanController.java`**

Add these endpoints:

```java
@PostMapping("/sync")
@RequireAdmin
public ResponseEntity<?> syncPlansFromMaya() {
    logger.info("Starting plan sync from Maya API");

    try {
        SyncResult result = planSyncService.syncFromMaya();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "created", result.getCreated(),
            "updated", result.getUpdated(),
            "removed", result.getRemoved(),
            "errors", result.getErrors(),
            "timestamp", LocalDateTime.now()
        ));
    } catch (Exception e) {
        logger.error("Plan sync failed: {}", e.getMessage());
        return ResponseEntity.status(500).body(Map.of(
            "success", false,
            "error", e.getMessage()
        ));
    }
}

@GetMapping("/sync/status")
@RequireAdmin
public ResponseEntity<?> getSyncStatus() {
    return ResponseEntity.ok(Map.of(
        "lastSyncAt", planSyncService.getLastSyncTime(),
        "totalMayaPlans", planRepository.countByProvider("maya"),
        "totalManualPlans", planRepository.countByProviderNot("maya")
    ));
}

@PutMapping("/{planId}/mark-manual")
@RequireAdmin
public ResponseEntity<?> markAsManual(@PathVariable String planId) {
    Plan plan = planService.getPlanById(planId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    plan.setProvider("manual");
    plan.setProviderId(null);
    planRepository.save(plan);

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Plan marked as manual"
    ));
}
```

### New PlanSyncService

**File: `src/main/java/com/flyroamy/api/fly_api/service/PlanSyncService.java`**

```java
@Service
public class PlanSyncService {

    private final MayaMobileService mayaMobileService;
    private final PlanRepository planRepository;

    public SyncResult syncFromMaya() {
        // 1. Fetch all bundles from Maya API
        List<MayaBundle> bundles = mayaMobileService.getAllBundles();

        int created = 0, updated = 0;
        List<String> errors = new ArrayList<>();

        // 2. Transform and upsert each bundle
        for (MayaBundle bundle : bundles) {
            try {
                Plan plan = transformToLocalPlan(bundle);

                Optional<Plan> existing = planRepository.findByProviderId(bundle.getBundleId());
                if (existing.isPresent()) {
                    // Update existing
                    Plan existingPlan = existing.get();
                    updatePlanFromBundle(existingPlan, bundle);
                    planRepository.save(existingPlan);
                    updated++;
                } else {
                    // Create new
                    plan.setProvider("maya");
                    plan.setProviderId(bundle.getBundleId());
                    planRepository.save(plan);
                    created++;
                }
            } catch (Exception e) {
                errors.add("Failed to sync bundle " + bundle.getBundleId() + ": " + e.getMessage());
            }
        }

        return new SyncResult(created, updated, 0, errors);
    }

    private Plan transformToLocalPlan(MayaBundle bundle) {
        Plan plan = new Plan();
        plan.setProductId(bundle.getProductId());
        plan.setName(bundle.getName());
        plan.setData(bundle.getDataGB());
        plan.setDays(bundle.getValidityDays());
        plan.setPrice(bundle.getPrice());
        plan.setCurrencyPrices(bundle.getPrices());
        plan.setPackageType(bundle.getPackageType());
        plan.setCountries(bundle.getCountries());
        plan.setBadge(bundle.getBadge());
        plan.setIsActive(bundle.isActive());
        plan.setWholesaleCost(bundle.getWholesaleCost());
        plan.setProvider("maya");
        plan.setProviderId(bundle.getBundleId());
        return plan;
    }
}
```

### Update MayaMobileService

Add method to fetch bundles:

```java
public List<MayaBundle> getAllBundles() {
    // Call the mock/real Maya API
    String url = baseUrl + "/connectivity/bundles?size=1000";

    // HTTP call with Basic Auth
    HttpHeaders headers = new HttpHeaders();
    String auth = apiKey + ":" + apiSecret;
    headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));

    ResponseEntity<BundleListResponse> response = restTemplate.exchange(
        url, HttpMethod.GET, new HttpEntity<>(headers), BundleListResponse.class
    );

    return response.getBody().getBundles();
}
```

---

## 6. Testing Checklist

After implementing these changes:

- [ ] Verify sync button appears on Plans page
- [ ] Test sync with mock service running
- [ ] Verify plans are created/updated correctly
- [ ] Check source badges display correctly
- [ ] Test manual plan creation (should show "Manual" badge)
- [ ] Verify modified plans show correct sync status
- [ ] Test error handling when Maya service is down

---

## 7. Future Enhancements

1. **Auto-Sync Schedule**: Add cron job to sync plans daily
2. **Sync History**: Store sync history in database
3. **Conflict Resolution**: Handle cases where local changes conflict with Maya
4. **Webhook Integration**: Receive real-time updates when Maya plans change
5. **Bulk Operations**: Select multiple plans to re-sync

---

**Document Version**: 1.0
**Created**: December 28, 2025
**Author**: Claude Code
