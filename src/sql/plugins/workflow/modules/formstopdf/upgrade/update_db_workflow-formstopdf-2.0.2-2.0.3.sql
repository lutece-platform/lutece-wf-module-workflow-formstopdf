-- liquibase formatted sql
-- changeset workflow-formstopdf:update_db_workflow-formstopdf-2.0.2-2.0.3.sql-decode-entities
-- preconditions onFail:MARK_RAN onError:WARN
-- comment LUT-34128 : decodes, once, the HTML entities the removed input filter of lutece-core 8 (sanitizeFilterMode, LUT-34109) wrote into the PDF models saved before the 2.0.2 betas (from 2.0.2 the module decoded its input with unescapeHtml4, which is removed): the name, the file name and the content of a model typed in the plain textarea, a FreeMarker source in which the filter wrote &lt;#if ...&gt; and &#34;. The content of a model typed with the rich text editor (is_rte = 1) is HTML and is left as it is. This changeset runs before the removal of the hand escaping below: an escape directive must be decoded before it can be removed. A model element the filter dropped (html, head, title...) cannot be restored.
UPDATE workflow_task_formspdf_template SET name = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(name, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE name LIKE '%&%;%';
UPDATE workflow_task_formspdf_template SET file_name = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(file_name, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE file_name LIKE '%&%;%';
UPDATE workflow_task_formspdf_template SET content = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(content, '&#39;', ''''), '&#x27;', ''''), '&#34;', '"'), '&quot;', '"'), '&#96;', '`'), '&#61;', '='), '&#64;', '@'), '&#43;', '+'), '&lt;', '<'), '&gt;', '>'), '&amp;', '&') WHERE is_rte = 0 AND content LIKE '%&%;%';

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
