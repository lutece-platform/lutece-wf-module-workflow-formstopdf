-- liquibase formatted sql

--
-- Pre-execution script of the module (LUT-33341 ; plugin-liquibase >= 2.0.2, LUT-33326) : executed in the
-- preliminary liquibase update, BEFORE the main changelog is built and filtered, so that the
-- version resolution of the main run sees its effects.
--
-- Rename workflow-formspdf -> workflow-formstopdf (LUT-32273, LUT-33289) : on an existing site
-- the module identity is persisted outside the changelog (core_datastore keys, core_admin_right,
-- DATABASECHANGELOG paths). Without this migration the new component has no version at startup,
-- the module is seen as a fresh install, its update_db_* scripts are discarded for ever and the
-- recorded version jumps to the current release.
--
-- Precondition on DATABASECHANGELOG (not on the datastore keys) : it is true for every site that
-- installed the module under its former directory, even one whose datastore keys were already
-- migrated by a 2.0.2-SNAPSHOT build ; false on a fresh install (MARK_RAN, and the query itself may
-- fail on an empty database : onError:MARK_RAN).
--
-- LIKE / REPLACE rather than exact keys : DatastoreService prefixes .installed and .pool with the
-- instance name on multi-instance deployments (NOTIFSTORE-02.core.plugins.status.<plugin>.installed).
--
-- Key by key precedence of the former keys : a new key is deleted only when its counterpart under the
-- former name exists, so that the rename does not collide on the primary key and the former value wins
-- (a 2.0.2-SNAPSHOT build without this migration ran the module as a fresh install and recorded the
-- current version, while the former .version still holds the truly installed one). An orphan former key,
-- typically .installed recreated at every startup from the site's plugins.dat, must not wipe a .version
-- already migrated : deleting every new key as soon as any former key exists left the module without
-- version, the main run then included the creation scripts and validated create_db against a checksum
-- rewritten on some sites.
--
-- changeset workflow-formstopdf:prerun-rename-formspdf
-- preconditions onFail:MARK_RAN onError:MARK_RAN
-- precondition-sql-check expectedResult:1 SELECT COUNT(DISTINCT 1) FROM DATABASECHANGELOG WHERE FILENAME LIKE 'sql/plugins/workflow/modules/formspdf/%'
DELETE FROM core_datastore WHERE entity_key LIKE '%core.plugins.status.workflow-formstopdf.%' AND REPLACE(entity_key,'core.plugins.status.workflow-formstopdf.','core.plugins.status.workflow-formspdf.') IN (SELECT entity_key FROM (SELECT entity_key FROM core_datastore WHERE entity_key LIKE '%core.plugins.status.workflow-formspdf.%') AS old_keys);
UPDATE core_datastore SET entity_key = REPLACE(entity_key,'core.plugins.status.workflow-formspdf.','core.plugins.status.workflow-formstopdf.') WHERE entity_key LIKE '%core.plugins.status.workflow-formspdf.%';
UPDATE core_admin_right SET plugin_name = 'workflow-formstopdf' WHERE plugin_name = 'workflow-formspdf';
UPDATE DATABASECHANGELOG SET FILENAME = REPLACE(FILENAME,'sql/plugins/workflow/modules/formspdf/','sql/plugins/workflow/modules/formstopdf/') WHERE FILENAME LIKE 'sql/plugins/workflow/modules/formspdf/%';
