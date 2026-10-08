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
| `payroll:view` | yes | yes | yes | yes |
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
| `users:write` | yes | no | no | no |
| `holidays:view` | yes | yes | yes | yes |
| `holidays:write` | yes | yes | no | no |
| `overtime:view` | yes | yes | yes | yes |
| `overtime:write` | yes | yes | yes | yes |
| `overtime:decide` | yes | yes | yes | no |
| `reports:view` | yes | yes | no | no |
| `notifications:view` | yes | yes | yes | yes |

Data scope: **ADMIN / HR** see all employees. **MANAGER** sees self + direct reports. **EMPLOYEE** sees self. List endpoints still return a JSON array (no pagination wrapper).

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
  { key: 'overtime', path: '/overtime', permission: 'overtime:view' },
  { key: 'holidays', path: '/holidays', permission: 'holidays:view' },
  { key: 'announcements', path: '/announcements', permission: 'announcements:view' },
  { key: 'reports', path: '/reports', permission: 'reports:view' },
  { key: 'users', path: '/users', permission: 'users:write' },
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
| `status` (leave) | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED` |
| `status` (attendance / overtime) | attendance: `PRESENT`, `ABSENT`, `LATE`, `ON_LEAVE`; overtime: `PENDING`, `APPROVED`, `REJECTED` |
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
| POST | `/api/v1/auth/password` | logged in | `{ currentPassword, newPassword }` |

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
  "hireDate": "2021-06-01",
  "nationalId": "010101234",
  "dateOfBirth": "1995-04-12",
  "address": "Phnom Penh",
  "salary": 1200
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
  "managerId": "…",
  "nationalId": "010101234",
  "dateOfBirth": "1995-04-12",
  "address": "Phnom Penh",
  "salary": 1200,
  "hasPhoto": false,
  "photoUrl": null
}
```

---

### Leaves

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/leaves` | `leaves:view` | `?employeeId=&status=PENDING\|APPROVED\|REJECTED\|CANCELLED` |
| GET | `/api/v1/leaves/balances` | `leaves:view` | `?employeeId=` (required unless the user has a linked employee) |
| GET | `/api/v1/leaves/{id}` | `leaves:view` | — |
| POST | `/api/v1/leaves` | `leaves:create` | create body |
| PATCH | `/api/v1/leaves/{id}/approve` | `leaves:decide` | — |
| PATCH | `/api/v1/leaves/{id}/reject` | `leaves:decide` | — |
| PATCH | `/api/v1/leaves/{id}/cancel` | `leaves:create` | own pending request only |

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

`payload`: `{ id, employeeId, type, startDate, endDate, reason, status, decidedAt, days }`

Balances `payload`: `[{ employeeId, year, type, entitled, used, pending, remaining }]`

---

### Attendance

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/attendances` | `attendance:view` | `?employeeId=&date=2026-10-07` or `?from=&to=` (`date` wins over range) |
| GET | `/api/v1/attendances/{id}` | `attendance:view` | — |
| POST | `/api/v1/attendances/check-in` | `attendance:check` | `{ "employeeId": "…" }` |
| POST | `/api/v1/attendances/check-out` | `attendance:check` | `{ "employeeId": "…" }` |
| PATCH | `/api/v1/attendances/{id}` | `employees:write` | `{ checkIn, checkOut, status, overtimeHours }` (all optional) |

`payload`: `{ id, employeeId, workDate, checkIn, checkOut, status, overtimeHours }`  
`checkIn` / `checkOut` look like `"08:15:00"`.

---

### Payroll

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/payrolls` | `payroll:view` | `?employeeId=&status=PENDING\|PAID` |
| GET | `/api/v1/payrolls/{id}` | `payroll:view` | — |
| GET | `/api/v1/payrolls/{id}/payslip` | `payroll:view` | PDF; send `Authorization` (same as document files) |
| POST | `/api/v1/payrolls` | `payroll:write` | create body |
| PATCH | `/api/v1/payrolls/{id}/pay` | `payroll:write` | marks as `PAID` |

```json
{
  "employeeId": "…",
  "periodStart": "2026-01-01",
  "periodEnd": "2026-01-31",
  "basicSalary": 1200,
  "allowances": 80,
  "deductions": 20,
  "tax": 50
}
```

`amount` is optional if `basicSalary` is set. Net pay = basic + allowances − deductions − tax.

`payload`: `{ id, employeeId, periodStart, periodEnd, amount, basicSalary, allowances, deductions, tax, netAmount, status }`

---

### Documents

Files are stored on disk. Allowed types: `pdf`, `png`, `jpg`, `jpeg`, `doc`, `docx`, `webp`. Max size: **10MB**.

| Method | Path | Permission | Notes |
|---|---|---|---|
| GET | `/api/v1/documents` | `documents:view` | `?employeeId=` |
| GET | `/api/v1/documents/{id}` | `documents:view` | metadata |
| GET | `/api/v1/documents/{id}/file` | `documents:view` | binary download; send `Authorization` header (do not use a plain `<a href>`) |
| POST | `/api/v1/documents` | `documents:write` | `multipart/form-data` |
| PUT | `/api/v1/documents/{id}` | `documents:write` | `multipart/form-data`; `file` is optional |
| DELETE | `/api/v1/documents/{id}` | `documents:write` | deletes metadata and the stored file |

**Upload (POST)** — `Content-Type: multipart/form-data`

| Field | Type | Required |
|---|---|---|
| `employeeId` | UUID | yes |
| `title` | string | yes |
| `documentType` | `CONTRACT` / `ID_CARD` / `CERTIFICATE` / `OTHER` | yes |
| `file` | file | yes |

**Update (PUT)** — same fields except `employeeId` is not used; send `title`, `documentType`, and/or `file`.

`payload`: `{ id, employeeId, title, fileUrl, originalFileName, contentType, fileSize, hasFile, documentType, uploadedAt }`

`fileUrl` is `/api/v1/documents/{id}/file` when `hasFile` is true. Open it with `fetch` + Bearer token, then `URL.createObjectURL(blob)`.

---

### Performance

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/performance-reviews` | `performance:view` | `?employeeId=` |
| GET | `/api/v1/performance-reviews/{id}` | `performance:view` | — |
| POST | `/api/v1/performance-reviews` | `performance:write` | create body |
| PUT | `/api/v1/performance-reviews/{id}` | `performance:write` | same as create |
| DELETE | `/api/v1/performance-reviews/{id}` | `performance:write` | — |

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
Example keys: `company.name`, `theme.mode`, `theme.primary` (`#0ea5e9`), `leave.annual-days`, `leave.sick-days`.

---

### Holidays

| Method | Path | Permission | Body |
|---|---|---|---|
| GET | `/api/v1/holidays` | `holidays:view` | — |
| POST | `/api/v1/holidays` | `holidays:write` | `{ name, holidayDate, paid }` |
| PUT | `/api/v1/holidays/{id}` | `holidays:write` | same fields (optional) |
| DELETE | `/api/v1/holidays/{id}` | `holidays:write` | — |

`payload`: `{ id, name, holidayDate, paid }`

---

### Overtime

| Method | Path | Permission | Query / body |
|---|---|---|---|
| GET | `/api/v1/overtimes` | `overtime:view` | `?employeeId=` |
| POST | `/api/v1/overtimes` | `overtime:write` | `{ employeeId, workDate, hours, reason }` |
| PATCH | `/api/v1/overtimes/{id}/approve` | `overtime:decide` | — |
| PATCH | `/api/v1/overtimes/{id}/reject` | `overtime:decide` | — |

`payload`: `{ id, employeeId, workDate, hours, reason, status, decidedAt }`

---

### Notifications

| Method | Path | Permission | Notes |
|---|---|---|---|
| GET | `/api/v1/notifications` | `notifications:view` | current user only |
| PATCH | `/api/v1/notifications/{id}/read` | `notifications:view` | — |

`payload`: `{ id, title, message, read, createdAt }`

---

### Users (login accounts)

| Method | Path | Permission | Body |
|---|---|---|---|
| GET | `/api/v1/users` | `users:write` | — |
| POST | `/api/v1/users` | `users:write` | `{ username, password, role, employeeId, enabled }` |
| PUT | `/api/v1/users/{id}` | `users:write` | same fields; password optional |
| DELETE | `/api/v1/users/{id}` | `users:write` | — |

`payload`: `{ id, username, role, employeeId, enabled }` (password is never returned)

---

### Reports

| Method | Path | Permission | `payload` |
|---|---|---|---|
| GET | `/api/v1/reports/summary` | `reports:view` | `{ employees, pendingLeaves, todayAttendance, pendingPayrolls, pendingOvertimes }` |

---

## 7. Page → API map

| Page | Load | Actions |
|---|---|---|
| Dashboard | `GET /dashboard`, `GET /employees`, `GET /leaves?status=PENDING` | — |
| Employees | `GET /employees` or `/employees/search` | POST/PUT/DELETE employee |
| Employee detail | `GET /employees/{id}`, `/history`, `/subordinates` | status, transfer, manager |
| Leaves | `GET /leaves`, `GET /leaves/balances` | POST leave, PATCH approve/reject/cancel |
| Attendance | `GET /attendances?from=&to=` | POST check-in / check-out, PATCH correct |
| Payroll | `GET /payrolls` | POST payroll, PATCH pay, GET payslip PDF |
| Documents | `GET /documents` | POST upload, GET `/{id}/file`, PUT update, DELETE |
| Performance | `GET /performance-reviews` | POST / PUT / DELETE review |
| Organization | `GET /organization/departments` | POST / PUT / DELETE |
| Overtime | `GET /overtimes` | POST overtime, PATCH approve/reject |
| Holidays | `GET /holidays` | POST / PUT / DELETE |
| Announcements | `GET /announcements` | POST / PUT / DELETE |
| Reports | `GET /reports/summary` | — |
| Users | `GET /users` | POST / PUT / DELETE |
| Profile | `GET /notifications` | POST `/auth/password`, PATCH notification read |
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
