-- liquibase formatted sql
-- changeset workflow-formspdf:create_db_workflow-formspdf.sql
-- validCheckSum: 9:869465754a9d2e2f6be31bf328827b5b
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = database() AND table_name = 'workflow_task_formspdf_template'
DROP TABLE IF EXISTS workflow_task_formspdf_cf ;

CREATE TABLE workflow_task_formspdf_cf (
	id_task INT DEFAULT '0' NOT NULL,
	id_forms INT DEFAULT '0' NULL,
	format VARCHAR(15) DEFAULT NULL NULL,
	id_template INT DEFAULT '0' NOT NULL,
	PRIMARY KEY (id_task)
)
;

DROP TABLE IF EXISTS workflow_task_formspdf_template ;

CREATE TABLE workflow_task_formspdf_template (
	id_template INT AUTO_INCREMENT,
	name VARCHAR(255) NOT NULL,
	id_form INT DEFAULT -1 NOT NULL,
	is_generic SMALLINT DEFAULT 1 NOT NULL,
	content LONG VARCHAR,
    is_rte SMALLINT DEFAULT 0 NOT NULL,
    is_replace_empty_response SMALLINT DEFAULT 0 NOT NULL,
	PRIMARY KEY (id_template)
)
;
