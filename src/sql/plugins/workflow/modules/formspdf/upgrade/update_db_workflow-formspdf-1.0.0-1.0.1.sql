-- liquibase formatted sql
-- changeset workflow-formspdf:update_db_workflow-formspdf-1.0.0-1.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
ALTER TABLE workflow_task_formspdf_cf MODIFY COLUMN id_task INT DEFAULT '0' NOT NULL;
ALTER TABLE workflow_task_formspdf_cf MODIFY COLUMN id_forms INT DEFAULT '0' NULL;
ALTER TABLE workflow_task_formspdf_cf MODIFY COLUMN id_template INT DEFAULT '0' NOT NULL;

ALTER TABLE workflow_task_formspdf_template MODIFY COLUMN id_form INT DEFAULT -1 NOT NULL;
ALTER TABLE workflow_task_formspdf_template MODIFY COLUMN content LONG VARCHAR;