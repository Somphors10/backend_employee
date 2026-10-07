# Employee Manage API — frontend guide

Base URL: `http://localhost:8080`  
Swagger: http://localhost:8080/swagger-ui.html  
CORS: `http://localhost:5173`

All JSON field names are **camelCase**. IDs are UUID strings. Dates are `YYYY-MM-DD`. Timestamps are ISO-8601 (`2026-10-07T08:00:00Z`).

---

## 1. Response wrapper

Every endpoint returns this shape:

```json
{
  "status": 200,
  "message": "Employees retrieved successfully",
  "payload": {},
  "timestamp": "2026-10-07T08:00:00Z"
}
```

| Field | Meaning |
|---|---|
| `status` | HTTP status code |
| `message` | Human text for toasts |
| `payload` | Data (object, array, or `null`) |
| `timestamp` | Server time |

Use `response.payload` in the UI. Use `response.message` for success/error toasts.

---

## 2. Auth (do this first)

### Login (no token)

`POST /api/v1/auth/login`

```json
{ "username": "admin", "password": "admin123" }
```

`payload`:

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "permissions": ["dashboard:view", "employees:view", "employees:write"]
}
```

Save `token` and send it on every other request:

```http
Authorization: Bearer <token>
Content-Type: application/json
```

### Current user

`GET /api/v1/auth/me`  
Permission: logged in  
Same `payload` as login.

### Logout

No backend call. Delete the token in the browser and send the user to `/login`.

### Demo accounts

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `hr` | `hr123` | HR |
| `manager` | `manager123` | MANAGER |
| `employee` | `employee123` | EMPLOYEE |

### Fetch helper

```js
const API = 'http://localhost:8080/api/v1';

async function api(path, { method = 'GET', body, token } = {}) {
  const res = await fetch(`${API}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json();
  if (!res.ok) {
    const error = new Error(data.message || 'Request failed');
    error.status = res.status;
    throw error;
  }
  return data.payload;
}
```

Example:

```js
const auth = await api('/auth/login', {
  method: 'POST',
  body: { username: 'admin', password: 'admin123' },
});
const employees = await api('/employees', { token: auth.token });
```

---

## 3. Status codes

| Code | When | Frontend |
|---|---|---|
| 200 / 201 | OK | Use `payload` |
| 400 | Bad body / invalid operation | Show `message` |
| 401 | No token or bad login | Clear session, go to login |
| 403 | Logged in, wrong permission | Hide the button / show Access denied |
| 404 | ID not found | Show `message` |
| 409 | Duplicate email | Show `message` |

---

## 4. Roles and permissions

After login, check **`payload.permissions`**, not only the role.

```js
const can = (user, permission) => user?.permissions?.includes(permission);
if (can(user, 'employees:write')) { /* show Add employee */ }
```

| Permission | ADMIN | HR | MANAGER | EMPLOYEE |
|---|---|---|---|---|
| `dashboard:view` | yes | yes | yes | yes |
| `employees:view` | yes | yes | yes | yes |
| `employees:write` | yes | yes | no | no |
| `leaves:view` | yes | yes | yes | yes |
| `leaves:create` | yes | yes | yes | yes |
| `leaves:decide` | yes | yes | yes | no |
| `attendance:view` | yes | yes | yes | yes |
| `attendance:check` | yes | yes | yes | yes |
| `payroll:view` | yes | yes | no | no |
| `payroll:write` | yes | yes | no | no |
| `documents:view` | yes | yes | yes | yes |
| `documents:write` | yes | yes | no | no |
| `performance:view` | yes | yes | yes | yes |
| `performance:write` | yes | yes | yes | no |
| `organization:view` | yes | yes | yes | yes |
| `organization:write` | yes | yes | no | no |
| `announcements:view` | yes | yes | yes | yes |
| `announcements:write` | yes | yes | no | no |
| `settings:view` | yes | yes | no | no |
| `settings:write` | yes | no | no | no |

Suggested sidebar:

```js
const NAV = [
  { key: 'dashboard', path: '/', permission: 'dashboard:view' },
  { key: 'employees', path: '/employees', permission: 'employees:view' },
  { key: 'leaves', path: '/leaves', permission: 'leaves:view' },
  { key: 'attendance', path: '/attendance', permission: 'attendance:view' },
  { key: 'payroll', path: '/payroll', permission: 'payroll:view' },
  { key: 'documents', path: '/documents', permission: 'documents:view' },
  { key: 'performance', path: '/performance', permission: 'performance:view' },
  { key: 'organization', path: '/organization', permission: 'organization:view' },
  { key: 'announcements', path: '/announcements', permission: 'announcements:view' },
  { key: 'settings', path: '/settings', permission: 'settings:view' },
];
```

---

## 5. Enums (send as strings)

| Enum | Values |
|---|---|
| `role` | `ADMIN`, `HR`, `MANAGER`, `EMPLOYEE` |
| `status` (employee) | `ACTIVE`, `INACTIVE` |
| `type` (leave) | `ANNUAL`, `SICK`, `UNPAID` |
| `status` (leave) | `PENDING`, `APPROVED`, `REJECTED` |
| `status` (attendance) | `PRESENT`, `ABSENT`, `LATE`, `ON_LEAVE` |
| `status` (payroll) | `PENDING`, `PAID` |
| `documentType` | `CONTRACT`, `ID_CARD`, `CERTIFICATE`, `OTHER` |
| `eventType` (history) | `CREATED`, `UPDATED`, `STATUS_CHANGED`, `TRANSFERRED`, `MANAGER_ASSIGNED`, `MANAGER_CLEARED`, `DELETED` |

---

## 6. Endpoints

`{id}` is always a UUID.

### Auth

| Method | Path | Permission | Body |
|---|---|---|---|
| POST | `/api/v1/auth/login` | public | `{ username, password }` |
| GET | `/api/v1/auth/me` | logged in | — |

---

### Dashboard

| Method | Path | Permission | `payload` |
|---|---|---|---|
| GET | `/api/v1/dashboard` | `dashboard:view` | `{ totalEmployees, activeEmployees, pendingLeaves, todayAttendance, pendingPayrolls }` |
| GET | `/api/v1/navigation` | `dashboard:view` | `[{ key, label, path, status }]` |

`status` on navigation is `LIVE` or `COMING_NEXT`. Map `/dashboard` to `/` in the app if needed.

---

### Employees

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/employees` | `employees:view` | — |
| GET | `/api/v1/employees/search` | `employees:view` | `?department=&q=&status=ACTIVE\|INACTIVE` |
| GET | `/api/v1/employees/summary` | `employees:view` | `{ totalEmployees, activeEmployees, inactiveEmployees, byDepartment: [{ department, employeeCount }] }` |
| GET | `/api/v1/employees/departments` | `employees:view` | `string[]` |
| GET | `/api/v1/employees/positions` | `employees:view` | `string[]` |
| GET | `/api/v1/employees/{id}` | `employees:view` | employee object |
| GET | `/api/v1/employees/{id}/history` | `employees:view` | `[{ id, employeeId, eventType, description, occurredAt }]` |
| GET | `/api/v1/employees/{id}/subordinates` | `employees:view` | employee array |
| POST | `/api/v1/employees` | `employees:write` | create body |
| PUT | `/api/v1/employees/{id}` | `employees:write` | same as create |
| PATCH | `/api/v1/employees/{id}/status` | `employees:write` | `{ "status": "INACTIVE" }` |
| PATCH | `/api/v1/employees/{id}/transfer` | `employees:write` | `{ "department": "IT", "position": "QA Engineer" }` |
| PATCH | `/api/v1/employees/{id}/manager` | `employees:write` | `{ "managerId": "<uuid>" }` |
| DELETE | `/api/v1/employees/{id}/manager` | `employees:write` | — |
| DELETE | `/api/v1/employees/{id}` | `employees:write` | — |

Create / update body:

```json
{
  "firstName": "Dara",
  "lastName": "Kim",
  "email": "dara.kim@company.com",
  "phoneNumber": "012111112",
  "position": "Software Engineer",
  "department": "IT",
  "hireDate": "2021-06-01"
}
```

Employee `payload`:

```json
{
  "id": "…",
  "firstName": "Dara",
  "lastName": "Kim",
  "email": "dara.kim@company.com",
  "phoneNumber": "012111112",
  "position": "Software Engineer",
  "department": "IT",
  "hireDate": "2021-06-01",
  "status": "ACTIVE",
  "managerId": "…"
}
```

---

### Leaves

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/leaves` | `leaves:view` | `?employeeId=&status=PENDING\|APPROVED\|REJECTED` |
| GET | `/api/v1/leaves/{id}` | `leaves:view` | — |
| POST | `/api/v1/leaves` | `leaves:create` | create body |
| PATCH | `/api/v1/leaves/{id}/approve` | `leaves:decide` | — |
| PATCH | `/api/v1/leaves/{id}/reject` | `leaves:decide` | — |

Create body:

```json
{
  "employeeId": "…",
  "type": "ANNUAL",
  "startDate": "2026-03-01",
  "endDate": "2026-03-03",
  "reason": "Family trip"
}
```

`payload`: `{ id, employeeId, type, startDate, endDate, reason, status, decidedAt }`

---

### Attendance

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/attendances` | `attendance:view` | `?employeeId=&date=2026-10-07` |
| GET | `/api/v1/attendances/{id}` | `attendance:view` | — |
| POST | `/api/v1/attendances/check-in` | `attendance:check` | `{ "employeeId": "…" }` |
| POST | `/api/v1/attendances/check-out` | `attendance:check` | `{ "employeeId": "…" }` |

`payload`: `{ id, employeeId, workDate, checkIn, checkOut, status }`  
`checkIn` / `checkOut` look like `"08:15:00"`.

---

### Payroll

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/payrolls` | `payroll:view` | `?employeeId=&status=PENDING\|PAID` |
| GET | `/api/v1/payrolls/{id}` | `payroll:view` | — |
| POST | `/api/v1/payrolls` | `payroll:write` | create body |
| PATCH | `/api/v1/payrolls/{id}/pay` | `payroll:write` | marks as `PAID` |

```json
{
  "employeeId": "…",
  "periodStart": "2026-01-01",
  "periodEnd": "2026-01-31",
  "amount": 1200
}
```

`payload`: `{ id, employeeId, periodStart, periodEnd, amount, status }`

---

### Documents

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/documents` | `documents:view` | `?employeeId=` |
| GET | `/api/v1/documents/{id}` | `documents:view` | — |
| POST | `/api/v1/documents` | `documents:write` | create body |
| DELETE | `/api/v1/documents/{id}` | `documents:write` | — |

```json
{
  "employeeId": "…",
  "title": "Employment Contract",
  "fileUrl": "https://files.company.local/docs/1.pdf",
  "documentType": "CONTRACT"
}
```

`payload`: `{ id, employeeId, title, fileUrl, documentType, uploadedAt }`  
This stores a URL, not a file upload.

---

### Performance

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/performance-reviews` | `performance:view` | `?employeeId=` |
| GET | `/api/v1/performance-reviews/{id}` | `performance:view` | — |
| POST | `/api/v1/performance-reviews` | `performance:write` | create body |

```json
{
  "employeeId": "…",
  "reviewer": "Sokha Chan",
  "rating": 4,
  "comments": "Strong delivery this quarter.",
  "reviewDate": "2026-06-01"
}
```

`rating` is `1`–`5`.  
`payload`: `{ id, employeeId, reviewer, rating, comments, reviewDate }`

---

### Organization

| Method | Path | Permission | Body |
|---|---|---|---|
| GET | `/api/v1/organization/departments` | `organization:view` | — |
| GET | `/api/v1/organization/departments/{id}` | `organization:view` | — |
| POST | `/api/v1/organization/departments` | `organization:write` | create body |
| PUT | `/api/v1/organization/departments/{id}` | `organization:write` | same as create |
| DELETE | `/api/v1/organization/departments/{id}` | `organization:write` | — |

```json
{
  "name": "IT",
  "description": "Software and infrastructure",
  "managerId": "…"
}
```

`payload`: `{ id, name, description, managerId }`

---

### Announcements

| Method | Path | Permission | Body |
|---|---|---|---|
| GET | `/api/v1/announcements` | `announcements:view` | — |
| GET | `/api/v1/announcements/{id}` | `announcements:view` | — |
| POST | `/api/v1/announcements` | `announcements:write` | create body |
| PUT | `/api/v1/announcements/{id}` | `announcements:write` | same as create |
| DELETE | `/api/v1/announcements/{id}` | `announcements:write` | — |

```json
{
  "title": "Office closed Friday",
  "content": "The office is closed this Friday.",
  "published": true
}
```

`payload`: `{ id, title, content, published, createdAt }`

---

### Settings

| Method | Path | Permission | Body |
|---|---|---|---|
| GET | `/api/v1/settings` | `settings:view` | — |
| GET | `/api/v1/settings/{key}` | `settings:view` | — |
| PUT | `/api/v1/settings/{key}` | `settings:write` | `{ "value": "light" }` |

`payload`: `{ key, value }`  
Example keys: `company.name`, `theme.mode`, `theme.primary` (`#0ea5e9`).

---

## 7. Page → API map

| Page | Load | Actions |
|---|---|---|
| Dashboard | `GET /dashboard`, `GET /employees`, `GET /leaves?status=PENDING` | — |
| Employees | `GET /employees` or `/employees/search` | POST/PUT/DELETE employee |
| Employee detail | `GET /employees/{id}`, `/history`, `/subordinates` | status, transfer, manager |
| Leaves | `GET /leaves` | POST leave, PATCH approve/reject |
| Attendance | `GET /attendances` | POST check-in / check-out |
| Payroll | `GET /payrolls` | POST payroll, PATCH pay |
| Documents | `GET /documents` | POST / DELETE |
| Performance | `GET /performance-reviews` | POST review |
| Organization | `GET /organization/departments` | POST / PUT / DELETE |
| Announcements | `GET /announcements` | POST / PUT / DELETE |
| Settings | `GET /settings` | PUT `/settings/{key}` |

---

## 8. Try it

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Copy `payload.token`, then:

```bash
curl -s http://localhost:8080/api/v1/employees ^
  -H "Authorization: Bearer PASTE_TOKEN_HERE"
```
