-- Migration: Remove soft-delete column from accommodation_employees table
ALTER TABLE accommodation_employees
    DROP COLUMN IF EXISTS deleted_at;