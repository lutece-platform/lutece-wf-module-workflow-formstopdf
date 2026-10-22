-- liquibase formatted sql
-- changeset workflow-formstopdf:update_db_workflow-formspdf-2.0.2-2.0.3.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'workflow_task_formspdf_cf' AND column_name = 'name'
-- LUT-34253 : name and description of the PDF task, for the sites installed in 2.0.2 (beta or SNAPSHOT) that skipped update_db_workflow-formspdf-2.0.1-2.0.2.sql-rev2
-- (the liquibase runner only runs an update whose destination version is strictly greater than the installed version)
ALTER TABLE workflow_task_formspdf_cf ADD COLUMN name VARCHAR(255) DEFAULT NULL;
ALTER TABLE workflow_task_formspdf_cf ADD COLUMN description LONG VARCHAR;

-- changeset workflow-formstopdf:update_db_workflow-formspdf-2.0.2-2.0.3-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- The admin user of the core (id 1) gets the management of the PDF templates, which the install script created without giving it to anybody
DELETE FROM core_user_right WHERE id_right = 'FORMSPDF_TEMPLATES_MANAGEMENT' AND id_user = 1;
INSERT INTO core_user_right (id_right,id_user) VALUES ('FORMSPDF_TEMPLATES_MANAGEMENT',1);
