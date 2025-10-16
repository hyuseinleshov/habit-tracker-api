-- Create roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    type VARCHAR(50) NOT NULL UNIQUE
);

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Create users_roles join table
CREATE TABLE users_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create user_profiles table
-- Note: With @MapsId, the id column IS the foreign key column (user_id)
CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY,
    timezone VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    age INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create habits table
CREATE TABLE habits (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    archived BOOLEAN NOT NULL,
    best_streak INT,
    best_streak_start_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create check_ins table
CREATE TABLE check_ins (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    habit_id UUID NOT NULL,
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
