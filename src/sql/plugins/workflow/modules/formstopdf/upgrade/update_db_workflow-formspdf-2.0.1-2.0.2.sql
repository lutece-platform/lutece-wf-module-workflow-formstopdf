-- liquibase formatted sql
-- changeset workflow-formspdf:update_db_workflow-formspdf-2.0.1-2.0.2.sql logicalFilePath:sql/plugins/workflow/modules/formspdf/upgrade/update_db_workflow-formspdf-2.0.1-2.0.2.sql
-- preconditions onFail:MARK_RAN onError:WARN
ALTER TABLE workflow_task_formspdf_template ADD COLUMN is_replace_empty_response SMALLINT DEFAULT 0 NOT NULL;