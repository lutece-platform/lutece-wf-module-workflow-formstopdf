<%@ page errorPage="../../../../ErrorPage.jsp" %>
${ pageContext.setAttribute( 'strContent', formsPDFTaskTemplateJspBean.processController( pageContext.request , pageContext.response ) ) }
${ pageContext.getAttribute( 'strContent' ) }
