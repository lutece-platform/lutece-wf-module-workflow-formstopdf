-- liquibase formatted sql
-- changeset workflow-formstopdf:init_core_workflow-formstopdf-rename.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(DISTINCT 1) FROM core_datastore WHERE entity_key LIKE '%core.plugins.status.workflow-formspdf.%'

--
-- Migration of the identifiers persisted under the former plugin name, after the
-- rename workflow-formspdf -> workflow-formstopdf (LUT-32273).
--
-- Why an init_ script and not an upgrade_ one : at the startup that brings the
-- rename, TestIncludeAllFilter reads core.plugins.status.workflow-formstopdf.version,
-- which does not exist yet on an existing site. It then takes the branch
-- "alreadyInstalledVersion == null" and only includes creation scripts, so any
-- update_* script would be discarded and would run one release later, leaving the
-- module seen as uninstalled in the meantime.
--
-- Why LIKE and REPLACE rather than exact keys : DatastoreService.getInstanceKey
-- prefixes the key with the webapp instance name whenever the instance is not the
-- default one, so a multi-instance deployment holds
-- NOTIFSTORE-02.core.plugins.status.<plugin>.installed and one such row per
-- instance. .installed and .pool go through setInstanceDataValue and are prefixed,
-- while .version and .lastRunScriptType are written by LiquibaseRunnerContext
-- through setDataValue and are not - REPLACE on the key handles both forms, and
-- every instance, in one statement.
--
-- The precondition returns 1 as soon as one former key exists, whatever the number
-- of instances, and 0 when there is none : on a fresh install, or once the
-- migration has been done, the changeset is MARK_RAN and does nothing.
--
DELETE FROM core_datastore WHERE entity_key LIKE '%core.plugins.status.workflow-formstopdf.%';

UPDATE core_datastore
   SET entity_key = REPLACE( entity_key, 'core.plugins.status.workflow-formspdf.', 'core.plugins.status.workflow-formstopdf.' )
 WHERE entity_key LIKE '%core.plugins.status.workflow-formspdf.%';

UPDATE core_admin_right SET plugin_name = 'workflow-formstopdf'
 WHERE plugin_name = 'workflow-formspdf';
