-- liquibase formatted sql
-- changeset workflow-formstopdf:update_db_workflow-formstopdf-1.1.1-1.2.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'workflow_task_formspdf_template' AND column_name = 'is_replace_empty_response'
ALTER TABLE workflow_task_formspdf_template ADD COLUMN is_replace_empty_response SMALLINT DEFAULT 0 NOT NULL;
