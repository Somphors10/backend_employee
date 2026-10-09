# Employee Manage API — easy frontend guide

| | |
|---|---|
| Local API | `http://localhost:8080` |
| Prefix | `/api/v1` |
| Swagger | http://localhost:8080/swagger-ui.html |
| Vite (Employee Hub) | call `/api/...` — proxy to `:8080` |
| Production | set `VITE_API_BASE` to the API origin (no trailing slash) |

JSON is **camelCase**. IDs are UUID. Dates `YYYY-MM-DD`. Times `HH:mm:ss`. Lists are a **JSON array** in `payload` (no pagination).

---

## 1. Copy this helper

```js
const API = (import.meta.env.VITE_API_BASE || '') + '/api/v1';

async function api(path, { method = 'GET', body, token, formData } = {}) {
  const res = await fetch(`${API}${path}`, {
    method,
    headers: {
      ...(formData ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: formData || (body ? JSON.stringify(body) : undefined),
  });
  const data = await res.json().catch(() => null);
  if (res.status === 401) {
    /* clear session → /login */
  }
  if (!res.ok) {
    throw Object.assign(new Error(data?.message || 'Request failed'), { status: res.status });
  }
  return data.payload; // always render this
}

async function openFile(path, token, downloadName) {
  const res = await fetch(`${API}${path}`, { headers: { Authorization: `Bearer ${token}` } });
  if (!res.ok) throw new Error('Download failed');
  const url = URL.createObjectURL(await res.blob());
  if (downloadName) {
    const a = document.createElement('a');
    a.href = url;
    a.download = downloadName;
    a.click();
  } else {
    window.open(url, '_blank', 'noopener');
  }
}
```

**Every JSON response looks like this.** Use `payload`. Show `message` in toasts.

```json
{
  "status": 200,
  "message": "Employees retrieved successfully",
  "payload": {},
  "timestamp": "2026-10-09T08:00:00Z"
}
```

PDF / CSV endpoints return the **file**, not this wrapper. Do **not** use `<a href>` — they need the Bearer token.

| HTTP | Meaning | UI |
|---|---|---|
| 200 / 201 | OK | use `payload` |
| 400 | bad body / invalid action | toast `message` |
| 401 | no / bad token | logout → login |
| 403 | missing permission | hide button / Access denied |
| 404 | id not found | toast `message` |
| 409 | duplicate email / username | toast `message` |

---

## 2. Login first

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `hr` | `hr123` | HR |
| `manager` | `manager123` | MANAGER |
| `employee` | `employee123` | EMPLOYEE |

```js
const user = await api('/auth/login', {
  method: 'POST',
  body: { username: 'admin', password: 'admin123' },
});
// user.token, user.role, user.employeeId, user.permissions
localStorage.setItem('token', user.token);
```

```http
Authorization: Bearer <token>
Content-Type: application/json
```

For file upload (`FormData`) **do not** set `Content-Type`. Still send `Authorization`.

| Method | Path | Auth | Body |
|---|---|---|---|
| POST | `/auth/login` | none | `{ username, password }` |
| GET | `/auth/me` | token | same payload as login (refreshes token) |
| POST | `/auth/password` | token | `{ currentPassword, newPassword }` (min 6 chars) |

Logout = delete token in the frontend.

Login `payload`:

```json
{
  "token": "eyJ…",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "permissions": ["dashboard:view", "employees:view", "employees:write"]
}
```

Hide buttons with **permissions**, not only `role`:

```js
const can = (user, p) => user?.permissions?.includes(p);
if (can(user, 'employees:write')) { /* show Add employee */ }
```

---

## 3. Who can do what

| Permission | ADMIN | HR | MANAGER | EMPLOYEE | Use for |
|---|---|---|---|---|---|
| `dashboard:view` | yes | yes | yes | yes | Dashboard |
| `employees:view` | yes | yes | yes | yes | Own / scoped employee record |
| `employees:directory` | yes | yes | yes | no | Employees list page |
| `employees:write` | yes | yes | no | no | Create / update people, correct attendance |
| `leaves:view` | yes | yes | yes | yes | Leave list |
| `leaves:create` | yes | yes | yes | yes | Request / cancel own leave |
| `leaves:decide` | yes | yes | yes | no | Approve / reject (manager: reports only, not self) |
| `attendance:view` | yes | yes | yes | yes | Attendance |
| `attendance:check` | yes | yes | yes | yes | Check-in / out |
| `payroll:view` | yes | yes | yes | yes | Payslips (scoped) |
| `payroll:write` | yes | yes | no | no | Create payroll / mark paid |
| `documents:view` | yes | yes | yes | yes | Files (scoped) |
| `documents:write` | yes | yes | no | no | Upload / delete files |
| `performance:view` | yes | yes | yes | yes | Reviews (scoped) |
| `performance:write` | yes | yes | yes | no | Write reviews |
| `organization:view` | yes | yes | yes | yes | Departments page |
| `organization:write` | yes | yes | no | no | Create / update / delete departments |
| `announcements:view` | yes | yes | yes | yes | News |
| `announcements:write` | yes | yes | no | no | Post news |
| `holidays:view` | yes | yes | yes | yes | Holiday calendar |
| `holidays:write` | yes | yes | no | no | Edit holidays |
| `overtime:view` | yes | yes | yes | yes | OT list |
| `overtime:write` | yes | yes | yes | yes | Submit OT |
| `overtime:decide` | yes | yes | yes | no | Approve OT (manager: reports only, not self) |
| `reports:view` | yes | yes | no | no | Reports + CSV |
| `notifications:view` | yes | yes | yes | yes | Bell |
| `roles:view` | yes | yes | no | no | Roles matrix |
| `users:write` | yes | no | no | no | Login accounts |
| `settings:view` | yes | yes | no | no | Settings |
| `settings:write` | yes | no | no | no | Change settings |

**Lists are scoped:** ADMIN/HR = everyone. MANAGER = self + direct reports. EMPLOYEE = self.

Sidebar: show a link only if `can(user, permission)`.

```js
const NAV = [
  { key: 'dashboard', path: '/', permission: 'dashboard:view' },
  { key: 'employees', path: '/employees', permission: 'employees:directory' },
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
  { key: 'roles', path: '/roles', permission: 'roles:view' },
  { key: 'settings', path: '/settings', permission: 'settings:view' },
];
```

| Method | Path | Permission |
|---|---|---|
| GET | `/rbac/me` | logged in → `{ role, permissions }` |
| GET | `/rbac/matrix` | `roles:view` → `{ permissions, roles: [{ role, permissions }] }` |

Assign a role with `PUT /users/{id}`. You never set permissions one-by-one.

---

## 4. Enums (send as strings)

| Field | Values |
|---|---|
| `role` | `ADMIN` `HR` `MANAGER` `EMPLOYEE` |
| employee `status` | `ACTIVE` `INACTIVE` |
| leave `type` | `ANNUAL` `SICK` `UNPAID` |
| leave `status` | `PENDING` `APPROVED` `REJECTED` `CANCELLED` |
| attendance `status` | `PRESENT` `ABSENT` `LATE` `ON_LEAVE` |
| overtime `status` | `PENDING` `APPROVED` `REJECTED` |
| payroll `status` | `PENDING` `PAID` |
| `documentType` | `CONTRACT` `ID_CARD` `CERTIFICATE` `OTHER` |

---

## 5. All endpoints (cheat sheet)

Paths below are after `/api/v1`. `{id}` = UUID.

### Auth

| | | | |
|---|---|---|---|
| POST | `/auth/login` | none | `{ username, password }` |
| GET | `/auth/me` | token | — |
| POST | `/auth/password` | token | `{ currentPassword, newPassword }` |

### Dashboard

| | | | |
|---|---|---|---|
| GET | `/dashboard` | `dashboard:view` | `{ totalEmployees, activeEmployees, pendingLeaves, todayAttendance, pendingPayrolls }` |
| GET | `/navigation` | `dashboard:view` | `[{ key, label, path, status, permission }]` — map `/dashboard` → `/` |

### Employees

| | | | |
|---|---|---|---|
| GET | `/employees` | `employees:view` | array (scoped) |
| GET | `/employees/search?department=&q=&status=` | `employees:view` | `status` = `ACTIVE` or `INACTIVE` |
| GET | `/employees/summary` | `employees:view` | `{ totalEmployees, activeEmployees, inactiveEmployees, byDepartment: [{ department, employeeCount }] }` |
| GET | `/employees/positions` | `employees:view` | `string[]` |
| GET | `/employees/{id}` | `employees:view` | one employee |
| GET | `/employees/{id}/history` | `employees:view` | `[{ id, employeeId, eventType, description, occurredAt }]` |
| GET | `/employees/{id}/subordinates` | `employees:view` | employee array |
| POST | `/employees` | `employees:write` | create body |
| PUT | `/employees/{id}` | `employees:write` | same as create |
| PATCH | `/employees/{id}/status` | `employees:write` | `{ status: "INACTIVE" }` |
| PATCH | `/employees/{id}/transfer` | `employees:write` | `{ department, position }` |
| PATCH | `/employees/{id}/manager` | `employees:write` | `{ managerId }` |
| DELETE | `/employees/{id}/manager` | `employees:write` | — |
| DELETE | `/employees/{id}` | `employees:write` | — |

```js
await api('/employees', {
  method: 'POST',
  token,
  body: {
    firstName: 'Dara',
    lastName: 'Kim',
    email: 'dara.kim@company.com',
    phoneNumber: '012111112',
    position: 'Software Engineer',
    department: 'IT',
    hireDate: '2021-06-01',
    nationalId: '010101234',
    dateOfBirth: '1995-04-12',
    address: 'Phnom Penh',
    salary: 1200,
  },
});
```

Employee object:

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

### Departments

Use **`/organization/departments`** on the Departments page (objects with `id`).

`GET /employees/departments` = **name strings** for dropdowns.

| | | | |
|---|---|---|---|
| GET | `/organization/departments` | `organization:view` | `[{ id, name, description, managerId, employeeCount }]` |
| GET | `/organization/departments/{id}` | `organization:view` | one |
| POST | `/organization/departments` | `organization:write` | `{ name, description, managerId }` |
| PUT | `/organization/departments/{id}` | `organization:write` | same |
| DELETE | `/organization/departments/{id}` | `organization:write` | blocked if people assigned |
| GET | `/employees/departments` | `employees:view` | `["HR","IT",…]` |
| GET | `/employees/departments/{id}` | `organization:view` | same object |
| POST / PUT / DELETE | `/employees/departments…` | `organization:write` | same CRUD |

Rename updates `employee.department` for people in that department.

### Leaves

| | | | |
|---|---|---|---|
| GET | `/leaves?employeeId=&status=` | `leaves:view` | |
| GET | `/leaves/balances?employeeId=` | `leaves:view` | call `/balances` **before** `/{id}` |
| GET | `/leaves/{id}` | `leaves:view` | |
| POST | `/leaves` | `leaves:create` | create body |
| PATCH | `/leaves/{id}/approve` | `leaves:decide` | no body |
| PATCH | `/leaves/{id}/reject` | `leaves:decide` | no body |
| PATCH | `/leaves/{id}/cancel` | `leaves:create` | own pending only |

```js
await api('/leaves', {
  method: 'POST',
  token,
  body: {
    employeeId: user.employeeId,
    type: 'ANNUAL',
    startDate: '2026-03-01',
    endDate: '2026-03-03',
    reason: 'Family trip',
  },
});
```

Leave: `{ id, employeeId, type, startDate, endDate, reason, status, decidedAt, days }`  
Balances: `[{ employeeId, year, type, entitled, used, pending, remaining }]`

### Attendance

| | | | |
|---|---|---|---|
| GET | `/attendances?employeeId=&date=` **or** `from=&to=` | `attendance:view` | `date` wins if both sent |
| GET | `/attendances/{id}` | `attendance:view` | |
| POST | `/attendances/check-in` | `attendance:check` | `{ employeeId }` |
| POST | `/attendances/check-out` | `attendance:check` | `{ employeeId }` |
| PATCH | `/attendances/{id}` | `employees:write` | HR/Admin: `{ checkIn, checkOut, status, overtimeHours }` any optional |

Object: `{ id, employeeId, workDate, checkIn, checkOut, status, overtimeHours }` — times like `"08:15:00"`.

### Payroll

| | | | |
|---|---|---|---|
| GET | `/payrolls?employeeId=&status=` | `payroll:view` | `PENDING` or `PAID` |
| GET | `/payrolls/{id}` | `payroll:view` | |
| GET | `/payrolls/{id}/payslip` | `payroll:view` | **PDF** → `openFile('/payrolls/'+id+'/payslip', token)` |
| POST | `/payrolls` | `payroll:write` | create body |
| PATCH | `/payrolls/{id}/pay` | `payroll:write` | marks `PAID` |

```js
await api('/payrolls', {
  method: 'POST',
  token,
  body: {
    employeeId: '…',
    periodStart: '2026-01-01',
    periodEnd: '2026-01-31',
    basicSalary: 1200,
    allowances: 80,
    deductions: 20,
    tax: 50,
  },
});
```

Net = basic + allowances − deductions − tax.  
Object: `{ id, employeeId, periodStart, periodEnd, amount, basicSalary, allowances, deductions, tax, netAmount, status }`

### Documents

Allowed: `pdf png jpg jpeg doc docx webp`. Max **10MB**.

| | | | |
|---|---|---|---|
| GET | `/documents?employeeId=` | `documents:view` | |
| GET | `/documents/{id}` | `documents:view` | metadata |
| GET | `/documents/{id}/file` | `documents:view` | **binary** → `openFile` |
| POST | `/documents` | `documents:write` | `FormData` |
| PUT | `/documents/{id}` | `documents:write` | `FormData` (`file` optional) |
| DELETE | `/documents/{id}` | `documents:write` | |

```js
const form = new FormData();
form.append('employeeId', id);
form.append('title', 'Employment Contract');
form.append('documentType', 'CONTRACT');
form.append('file', fileInput.files[0]);
await api('/documents', { method: 'POST', token, formData: form });
```

Metadata: `{ id, employeeId, title, fileUrl, originalFileName, contentType, fileSize, hasFile, documentType, uploadedAt }`  
`fileUrl` is `/api/v1/documents/{id}/file` when `hasFile` is true.

### Performance

| | | | |
|---|---|---|---|
| GET | `/performance-reviews?employeeId=` | `performance:view` | |
| GET | `/performance-reviews/{id}` | `performance:view` | |
| POST | `/performance-reviews` | `performance:write` | `{ employeeId, reviewer, rating, comments, reviewDate }` |
| PUT | `/performance-reviews/{id}` | `performance:write` | same |
| DELETE | `/performance-reviews/{id}` | `performance:write` | |

`rating` is `1`–`5`. Object: `{ id, employeeId, reviewer, rating, comments, reviewDate }`

### Overtime

| | | | |
|---|---|---|---|
| GET | `/overtimes?employeeId=` | `overtime:view` | |
| POST | `/overtimes` | `overtime:write` | `{ employeeId, workDate, hours, reason }` |
| PATCH | `/overtimes/{id}/approve` | `overtime:decide` | |
| PATCH | `/overtimes/{id}/reject` | `overtime:decide` | |

Object: `{ id, employeeId, workDate, hours, reason, status, decidedAt }`

### Holidays

| | | | |
|---|---|---|---|
| GET | `/holidays` | `holidays:view` | |
| POST | `/holidays` | `holidays:write` | `{ name, holidayDate, paid }` |
| PUT | `/holidays/{id}` | `holidays:write` | same, fields optional |
| DELETE | `/holidays/{id}` | `holidays:write` | |

Object: `{ id, name, holidayDate, paid }`

### Announcements

| | | | |
|---|---|---|---|
| GET | `/announcements` | `announcements:view` | |
| GET | `/announcements/{id}` | `announcements:view` | |
| POST | `/announcements` | `announcements:write` | `{ title, content, published }` |
| PUT | `/announcements/{id}` | `announcements:write` | same |
| DELETE | `/announcements/{id}` | `announcements:write` | |

Object: `{ id, title, content, published, createdAt }`

### Notifications

| | | | |
|---|---|---|---|
| GET | `/notifications` | `notifications:view` | current user only |
| PATCH | `/notifications/{id}/read` | `notifications:view` | |

Object: `{ id, title, message, read, createdAt }`

### Users (login accounts) — Admin only

| | | | |
|---|---|---|---|
| GET | `/users` | `users:write` | |
| POST | `/users` | `users:write` | `{ username, password, role, employeeId, enabled }` |
| PUT | `/users/{id}` | `users:write` | same; `password` optional |
| DELETE | `/users/{id}` | `users:write` | cannot delete/disable yourself or last admin |

Object: `{ id, username, role, employeeId, enabled, permissions }` — password never returned.

### Reports — HR / Admin

Default range: **1 Jan this year → today**.

| | | | |
|---|---|---|---|
| GET | `/reports/summary?from=&to=` | `reports:view` | overview JSON |
| GET | `/reports/export?type=&from=&to=` | `reports:view` | **CSV file** |

`type`: `people` `leaves` `attendance` `payrolls` `overtimes` `performance`

```js
const overview = await api('/reports/summary?from=2026-01-01&to=2026-10-09', { token });
await openFile('/reports/export?type=leaves&from=2026-01-01&to=2026-10-09', token, 'leaves.csv');
```

Summary `payload` (top-level + nested):

```json
{
  "from": "2026-01-01",
  "to": "2026-10-09",
  "employees": 10,
  "pendingLeaves": 2,
  "todayAttendance": 4,
  "pendingPayrolls": 1,
  "pendingOvertimes": 3,
  "people": { "total": 10, "active": 9, "inactive": 1, "newHires": 2, "byDepartment": [{ "name": "IT", "count": 3, "percent": 30 }] },
  "leaves": { "total": 8, "pending": 2, "approved": 4, "approvedDays": 12, "byType": [], "byStatus": [] },
  "attendance": { "records": 40, "present": 30, "absent": 2, "late": 5, "onLeave": 3, "byStatus": [] },
  "payroll": { "records": 10, "pending": 1, "paid": 9, "pendingAmount": 1200, "paidAmount": 18000, "totalAmount": 19200, "byStatus": [] },
  "overtime": { "records": 8, "pending": 3, "approvedHours": 18, "byStatus": [] },
  "performance": { "reviews": 6, "averageRating": 4.2, "byRating": [{ "name": "5", "count": 2, "percent": 33.3 }] }
}
```

Breakdown rows: `{ name, count, percent }` (`amount` only when used).

### Settings — HR view / Admin write

| | | | |
|---|---|---|---|
| GET | `/settings` | `settings:view` | `[{ key, value }]` |
| GET | `/settings/{key}` | `settings:view` | one |
| PUT | `/settings/{key}` | `settings:write` | `{ value: "light" }` |

Keys: `company.name` `theme.mode` `theme.primary` (`#0ea5e9`) `leave.annual-days` `leave.sick-days`

---

## 6. Page → which APIs

| Page | Load | Actions |
|---|---|---|
| Login | `POST /auth/login` | — |
| Dashboard | `GET /dashboard`, `GET /employees`, `GET /leaves?status=PENDING` | — |
| Employees | `GET /employees` or `/employees/search`, `GET /employees/summary` | POST PUT DELETE |
| Employee detail | `GET /employees/{id}`, `/history`, `/subordinates` | status, transfer, manager |
| Departments | `GET /organization/departments` | POST PUT DELETE |
| Leaves | `GET /leaves`, `GET /leaves/balances` | POST, PATCH approve/reject/cancel |
| Attendance | `GET /attendances?from=&to=` | POST check-in/out, PATCH correct |
| Payroll | `GET /payrolls` | POST, PATCH pay, GET payslip PDF |
| Documents | `GET /documents` | POST, GET `/{id}/file`, PUT, DELETE |
| Performance | `GET /performance-reviews` | POST PUT DELETE |
| Overtime | `GET /overtimes` | POST, PATCH approve/reject |
| Holidays | `GET /holidays` | POST PUT DELETE |
| Announcements | `GET /announcements` | POST PUT DELETE |
| Reports | `GET /reports/summary?from=&to=` | `GET /reports/export?type=` |
| Users | `GET /users` | POST PUT DELETE |
| Roles | `GET /rbac/matrix` | — |
| Profile | `GET /notifications` | POST `/auth/password`, PATCH read |
| Settings | `GET /settings` | PUT `/settings/{key}` |

---

## 7. Quick test

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
