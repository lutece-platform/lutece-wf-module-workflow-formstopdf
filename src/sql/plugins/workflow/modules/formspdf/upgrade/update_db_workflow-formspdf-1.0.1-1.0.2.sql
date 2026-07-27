-- liquibase formatted sql
-- changeset workflow-formspdf:update_db_workflow-formspdf-1.0.1-1.0.2.sql
-- preconditions onFail:MARK_RAN onError:WARN
ALTER TABLE workflow_task_formspdf_template ADD COLUMN is_replace_empty_response SMALLINT DEFAULT 0 NOT NULL;