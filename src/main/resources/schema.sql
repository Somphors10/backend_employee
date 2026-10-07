CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    manager_id UUID
);

CREATE TABLE IF NOT EXISTS employee_histories (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS leave_requests (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE
);
