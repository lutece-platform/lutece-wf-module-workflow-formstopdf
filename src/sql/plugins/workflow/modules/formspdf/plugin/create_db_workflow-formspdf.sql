DROP TABLE IF EXISTS workflow_task_formspdf_cf ;

CREATE TABLE workflow_task_formspdf_cf (
	id_task INT(11) NOT NULL DEFAULT '0',
	id_forms INT(11) NULL DEFAULT '0',
	format VARCHAR(15) NULL DEFAULT NULL,
	id_template INT(11) NOT NULL DEFAULT '0',
	PRIMARY KEY (id_task)
)
;

DROP TABLE IF EXISTS workflow_task_formspdf_template ;

CREATE TABLE workflow_task_formspdf_template (
	id_template INT AUTO_INCREMENT,
	name VARCHAR(255) NOT NULL,
	id_form INT(11) NOT NULL DEFAULT -1,
	is_generic SMALLINT NOT NULL DEFAULT 1,
	content LONGTEXT,
    is_rte SMALLINT NOT NULL DEFAULT 0,
	PRIMARY KEY (id_template)
)
;
