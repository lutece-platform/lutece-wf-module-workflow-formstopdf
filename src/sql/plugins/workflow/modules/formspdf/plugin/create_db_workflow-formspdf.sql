DROP TABLE IF EXISTS workflow_task_formspdf_cf ;

CREATE TABLE `workflow_task_formspdf_cf` (
	`id_task` INT(11) NOT NULL DEFAULT '0',
	`id_forms` INT(11) NULL DEFAULT '0',
	`format` VARCHAR(15) NULL DEFAULT NULL,
	`template` VARCHAR(200) NULL DEFAULT NULL,
	PRIMARY KEY (`id_task`)
)
;