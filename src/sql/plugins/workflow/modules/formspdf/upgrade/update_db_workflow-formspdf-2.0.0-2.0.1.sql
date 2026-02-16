-- liquibase formatted sql
-- changeset workflow-formspdf:update_db_workflow-formspdf-2.0.0-2.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
DELETE FROM core_admin_right WHERE id_right = 'FORMSPDF_TEMPLATES_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
    ('FORMSPDF_TEMPLATES_MANAGEMENT','module.workflow.formspdf.adminFeature.manageTemplates.name',1,'jsp/admin/plugins/workflow/modules/formspdf/ManageTemplates.jsp','module.workflow.formspdf.adminFeature.manageTemplates.description',0,'workflow-formspdf',NULL,'ti ti-template',NULL,NULL);