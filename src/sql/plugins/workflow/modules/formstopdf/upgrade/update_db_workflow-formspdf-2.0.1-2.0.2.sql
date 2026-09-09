-- liquibase formatted sql
-- changeset workflow-formspdf:update_db_workflow-formspdf-2.0.1-2.0.2.sql
-- preconditions onFail:MARK_RAN onError:WARN
ALTER TABLE workflow_task_formspdf_template ADD COLUMN is_replace_empty_response SMALLINT DEFAULT 0 NOT NULL;

-- changeset workflow-formstopdf:update_db_workflow-formspdf-2.0.1-2.0.2-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_admin_right SET plugin_name = 'workflow-formstopdf' WHERE id_right = 'FORMSPDF_TEMPLATES_MANAGEMENT';
