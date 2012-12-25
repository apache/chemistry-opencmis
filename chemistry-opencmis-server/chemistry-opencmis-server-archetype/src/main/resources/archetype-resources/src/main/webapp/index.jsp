<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="css/opencmis.css"/>
<title>${artifactId} Server</title>
</head>
<body>

<h1>${artifactId} Server</h1>
<p> Your server is up and running.</p>
<p>
	The ${artifactId} Server is a CMIS server based on Apache Chemistry OpenCMIS.
</p>
<p>
	You have to use a CMIS client to use this application. An example for
	such a client is the <a href="http://chemistry.apache.org/java/developing/tools/dev-tools-workbench.html"> CMIS Workbench.</a>
</p>

<h2>Access Information</h2>
<p>
WS (SOAP) Binding: <a href="services/RepositoryService">All Services</a>
</p>
<p>
AtomPub Binding: <a href="atom">Service Document</a>
</p>
<p>
Browser Binding: <a href="browser">Service Document</a>
</p>
<p>
Authentication: Basic Authentication
Note: After initial creation of the application authentication is disabled.
</p>

<h2>Status</h2>
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

<h2>More Information</h2>
<p>
  <a href="http://chemistry.apache.org"> Apache Chemistry web site</a>
</p>
<p>
  <a href="http://www.oasis-open.org/committees/cmis"> CMIS page at OASIS</a>
</p>


<hr/>
This software is powered by <a href="http://chemistry.apache.org/"> Apache Chemistry.</a>
<br/>

</body>
</html>