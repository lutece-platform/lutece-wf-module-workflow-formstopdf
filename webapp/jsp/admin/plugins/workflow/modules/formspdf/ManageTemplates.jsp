<jsp:useBean id="manageFormsPDFTaskTemplate" scope="session" class="fr.paris.lutece.plugins.workflow.modules.formspdf.web.task.FormsPDFTaskTemplateJspBean" />
<% String strContent = manageFormsPDFTaskTemplate.processController ( request , response ); %>

<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../../../AdminFooter.jsp" %>
