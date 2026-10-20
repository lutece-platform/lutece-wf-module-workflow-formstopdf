-- liquibase formatted sql
-- changeset workflow-formstopdf:update_db_workflow-formstopdf-2.0.2-2.0.3.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- LUT-33153 (FreeMarker HTML auto-escaping): the content and the file name of the PDF models of
-- workflow_task_formspdf_template are FreeMarker templates (with the markers of the form answers) rendered with HTML
-- auto-escaping. Hand escaping (?html, ?xhtml, ?xml, ?rtf, ?web_safe) and the <#escape x as x?html> directive no longer
-- parse: the PDF generation would fail at run time. They are removed; auto-escaping escapes the values of the answers,
-- the HTML written in the model is kept as is.
-- An XML declaration (<?xml ...?>) of a model is not a built-in: it is set aside before ?xml is removed, then restored.
-- This script rewrites site data (table workflow_task_formspdf_template, columns content and file_name). Idempotent: a
-- value without these built-ins is not updated.
-- Not handled, to review by hand: an escape directive written another way (other variable name, <#noescape>), and a
-- built-in inside a <script> of a model (it needs ?js_string there). To list them before the upgrade:
--   SELECT id_template, name FROM workflow_task_formspdf_template WHERE content LIKE '%#escape%' OR content LIKE '%#noescape%' OR content LIKE '%<script%';
UPDATE workflow_task_formspdf_template SET content =
  REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(
    content, '<?xml', '<!--lut33153-xml-declaration-->' ), '?html', '' ), '?xhtml', '' ), '?xml', '' ), '?rtf', '' ), '?web_safe', '' ),
    '<#escape x as x>', '' ), '</#escape>', '' ), '[#escape x as x]', '' ), '[/#escape]', '' ),
    '<!--lut33153-xml-declaration-->', '<?xml' )
  WHERE content LIKE '%?html%' OR content LIKE '%?xhtml%' OR REPLACE( content, '<?xml', '' ) LIKE '%?xml%' OR content LIKE '%?rtf%'
    OR content LIKE '%?web_safe%';
UPDATE workflow_task_formspdf_template SET file_name = REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( file_name, '?html', '' ), '?xhtml', '' ), '?xml', '' ), '?rtf', '' ), '?web_safe', '' )
  WHERE file_name LIKE '%?html%' OR file_name LIKE '%?xhtml%' OR file_name LIKE '%?xml%' OR file_name LIKE '%?rtf%' OR file_name LIKE '%?web_safe%';
