-- V3__update_schema_for_new_features.sql

-- Add timestamps to User table (if not already present and desired)
-- ALTER TABLE users
-- ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
-- ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL;

-- 1. Update 'boards' table
ALTER TABLE boards RENAME COLUMN name TO title;
ALTER TABLE boards ADD COLUMN description TEXT;
ALTER TABLE boards ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL;
ALTER TABLE boards ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL;

-- 2. Update 'tasks' table
-- Drop old assignee_id column (if it exists and is no longer needed for ManyToOne)
ALTER TABLE tasks DROP COLUMN IF EXISTS assignee_id;

ALTER TABLE tasks ADD COLUMN status VARCHAR(255) DEFAULT 'TODO' NOT NULL;
ALTER TABLE tasks ADD COLUMN priority VARCHAR(255) DEFAULT 'LOW' NOT NULL;
ALTER TABLE tasks ADD COLUMN due_date DATE;
ALTER TABLE tasks ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL;
ALTER TABLE tasks ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL;

-- 3. Create 'task_assignees' join table for ManyToMany relationship
CREATE TABLE task_assignees (
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 4. Update 'comments' table
-- created_at already exists from V2, so only add updated_at
ALTER TABLE comments ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL;

-- Optional: Add default values for existing rows if needed, e.g.:
-- UPDATE boards SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL;
-- UPDATE tasks SET created_at = NOW(), updated_at = NOW(), status = 'TODO', priority = 'LOW' WHERE created_at IS NULL;
-- UPDATE comments SET updated_at = NOW() WHERE updated_at IS NULL;
