<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="java.util.Date, java.text.SimpleDateFormat, java.util.Locale, java.util.Calendar" %>
<%@ page import="org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager" %>
<%@ page import="org.apache.chemistry.opencmis.commons.server.CallContext" %>
<%@ page import="org.apache.chemistry.opencmis.inmemory.DummyCallContext" %>
<%@ page import="org.apache.chemistry.opencmis.commons.server.CmisServiceFactory" %>
<%@ page import="org.apache.chemistry.opencmis.commons.server.CmisService" %>
<%@ page import="org.apache.chemistry.opencmis.inmemory.server.InMemoryService" %>
<%@ page import="org.apache.chemistry.opencmis.inmemory.ConfigConstants" %>
<%@ page import="org.apache.chemistry.opencmis.inmemory.ConfigurationSettings" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Apache Chemistry OpenCMIS-InMemory Server</title>
<style type="text/css">

body {
  font-family: Verdana, arial, sans-serif;
  color: black;
  font-size: 12px;
}

h1 {
  font-size: 24px;
  line-height: normal;
  font-weight: bold;
  background-color: #f0f0f0;
  color: #003366;
   border-bottom: 1px solid #3c78b5;
  padding: 2px;
  margin: 36px 0px 4px 0px;
}

h2 {
  font-size: 18px;
  line-height: normal;
  font-weight: bold;
  background-color: #f0f0f0;
   border-bottom: 1px solid #3c78b5;
  padding: 2px;
  margin: 27px 0px 4px 0px;
}

h3 {
  font-size: 14px;
  line-height: normal;
  font-weight: bold;
  background-color: #f0f0f0;
  padding: 2px;
  margin: 21px 0px 4px 0px;
}

h4 {
  font-size: 12px;
  line-height: normal;
  font-weight: bold;
  background-color: #f0f0f0;
  padding: 2px;
  margin: 18px 0px 4px 0px;
}

HR {
  color: 3c78b5;
  height: 1;
}

th  {
    border: 1px solid #ccc;
    padding: 2px 4px 2px 4px;
    background: #f0f0f0;
    text-align: center;
}

td  {
    border: 1px solid #ccc;
    padding: 3px 4px 3px 4px;
}

<%!
	private StoreManager getStoreManager(HttpServletRequest request) {
	    CallContext context = new DummyCallContext();
	    CmisServiceFactory servicesFactory = (CmisServiceFactory) request.getSession().getServletContext().getAttribute(
	        "org.apache.chemistry.opencmis.servicesfactory");
	    // AbstractServiceFactory factory = (AbstractServiceFactory)
	    CmisService service = servicesFactory.getService(context);
	    if (!(service instanceof InMemoryService))
	      throw new RuntimeException("Illegal configuration, service must be of type InMemoryService.");
	    return  ((InMemoryService) service).getStoreManager();
	}
%>

</style>
</head>
<body>
  <img alt="Apache Chemistry Logo" title="Apache Chemistry Logo" src="chemistry_logo_small.png" />

<h1>OpenCMIS InMemory Server</h1>
<p> Your server is up an running.</p>
<p>
	The OpenCMIS InMemory Server is a CMIS server for development and test purposes.
	All objects are hold in memory and will be lost after shutdown.
</p>
<p>
	You have to use a CMIS client to use this application. An example for
	such a client is the <a href="http://chemistry.apache.org/java/developing/tools/dev-tools-workbench.html"> CMIS Workbench.</a>
</p>

<h2>Access Information</h2>
<p>
WS (SOAP) Binding: <a href="services/RepositoryService"> All Services</a>
</p>
<p>
AtomPub Binding: <a href="atom"> 
<% 
String reqStr = request.getRequestURL().toString();
out.println(reqStr.substring(0, reqStr.lastIndexOf('/')+1) + "atom/");
%>
</a>
</p>
<p>
Authentication: Basic Authentication (user name and password are arbitrary)
Note: Authentication is optional and only informational. User names are stored 
in properties (createdBy, etc.), password is not required. The server does 
not perform any kind of secure authentication.
</p>

<h2>Monitor</h2>
<p>
  Current state of the server:
</p>

<table>
<tr> <th> Repository Id </th> <th> No. of objects</th></tr>
<% 
   StoreManager sm = getStoreManager(request);
   for (String repId: sm.getAllRepositoryIds() ) {
       out.println("<td>" + repId + "</td>");
       out.println("<td>" + sm.getObjectStore(repId).getObjectCount() + "</td>");
   }       
%>
</table>
<p>&nbsp;</p>
<table>
<tr> <th> Java VM </th> <th>Size</th></tr>
<% 
   Runtime runtime = Runtime.getRuntime ();   
   long mb = 1048576;
   long value;
   value = runtime.totalMemory ();
   value = (value + mb/2) / mb; 
   out.println("<tr><td> Used Memory </td>");
   out.println("<td>" +  value + "MB</td></tr>");
   value = runtime.maxMemory ();
   value = (value + mb/2) / mb; 
   out.println("<tr><td> Max Memory </td>");
   out.println("<td>" + value + "MB</td></tr>");
   value = runtime.freeMemory ();
   value = (value + mb/2) / mb; 
   out.println("<tr><td> Free Memory </td>");
   out.println("<td>" + value + "MB</td>");
   out.println("<tr><td> Processors </td>");
   out.println("<td>" + runtime.availableProcessors() + "</td></tr>");
%>
</table>

<h2>Configuration</h2>
<p>
  Important configuration settings
</p>

<table>
<tr> <th> Setting </th> <th> Value</th></tr>
<tr>
	<td>Max. allowed content size </td>
	<% 
	  out.println("<td>" + ConfigurationSettings.getConfigurationValueAsString(ConfigConstants.MAX_CONTENT_SIZE_KB) + "KB</td>");
	%>
</tr>
<tr>
	<td>Auto clean every</td>
	<% 
	  String cleanInterValStr = ConfigurationSettings.getConfigurationValueAsString(ConfigConstants.CLEAN_REPOSITORY_INTERVAL);
	  if (null == cleanInterValStr)
	      out.println("<td> - </td>");
	  else
	  	out.println("<td>" + cleanInterValStr + " minutes </td>");
	%>
</tr>
<tr>
	<td>Time of deployment</td>
	<% 
	  out.println("<td>" + ConfigurationSettings.getConfigurationValueAsString(ConfigConstants.DEPLOYMENT_TIME) + "</td>");
	%>
</tr>
<tr>
	<td>Next cleanup</td>
	<% 
	  String dateStr;
	  Long interval = ConfigurationSettings.getConfigurationValueAsLong(ConfigConstants.CLEAN_REPOSITORY_INTERVAL);
	  if (null == interval)
	      dateStr = "Never";
	  else {
		  try {
		      Date now = new Date();
		      Calendar calNow = Calendar.getInstance();
		      Calendar calNextClean = Calendar.getInstance();
		      calNow.setTime(now);
			  SimpleDateFormat formatter ; 
		      Date deploy; 
		      formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
		      deploy = formatter.parse(ConfigurationSettings.getConfigurationValueAsString(ConfigConstants.DEPLOYMENT_TIME));
		      calNextClean.setTime(deploy);
		      while (calNextClean.before(calNow))
		          calNextClean.add(Calendar.MINUTE, interval.intValue());
		      dateStr = formatter.format(calNextClean.getTime());
		  } catch (Exception e) {
		      dateStr = e.getMessage();
		  }
	  }
	  // Date deploy = new Date(Date.parse();
	  out.println("<td>" + dateStr + "</td>");
	%>
</tr>
</table>

<h2>More Information</h2>
<p>
<a href="http://chemistry.apache.org"> Apache Chemistry web site</a>
</p>
<p>
<a href="http://www.oasis-open.org/committees/cmis"> CMIS page at OASIS</a>
</p>


<hr/>
<h2>License Information</h2>
This software is licensed under the 
<a href="http://www.apache.org/licenses/LICENSE-2.0.html"> Apache 2.0 License </a>
<br/>

<a href="http://www.apache.org">
  <img alt="ASF Logo" title="ASF Logo" src="asf_logo.png" align="right"/>
</a>

</body>
</html>