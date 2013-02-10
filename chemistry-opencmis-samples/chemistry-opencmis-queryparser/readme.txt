Readme.txt chemistry-opencmis-queryparser-example
=================================================

This project is an example how to build a query parser using the OpenCMIS query framework.

The example is standalone program that makes use of the AntLR based query parser. It 
parses a CMIS query string and creates after traversing the syntax tree a new statement in
CMIS query language. While this sounds pretty useless this sample still has value:
 - it is not dependent on any query language of a backend (like a database) or a 
   specific database scheme.
 - it can be easily adapted to generate output in different format (SQL, XML, JSON)
 - it separates classes that can be reused from those that are specific to the output
   language
 - it provides unit tests that can be adapted to other target languages.

The sample provides code for the full cycle to parse and process a query. The entry point
is a static main() method so that it runs standalone without any server. Usually this code
will be called from the CMIS Discovery service in a real CMIS server.

The code consists of the following classes:

ExampleQueryProcessor.java:
This is the main class. It takes a query and parses it. It traverses the generated tree
and finally builds the output.

ExampleQueryWalker.java:
A walker that traverses the syntax tree and generates the CMISQL output. For each node in
the tree callback functions are provided processing a specific token.

ExampleTypeManager.java:
An OpenCMIS parser processes the query and does certain checks for validity. To do this
it needs information about the available types and properties in the backend. This 
information is provided by the interface TypeManager. This class implements a minimal 
type system to get a running sample. It has only one type implemented: cmis:document
(note that this is not allowed for a real server according to the CMIS spec).

ExtendedAbstractPredicateWalker.java
This class extends the predefined walker from the server support package. It provides
more fine grained control and more hooks to plugin while traversing the tree. This part
is independent of the generated output and therefore separated in its own class. It may
be reused for other projects.

ExampleQueryProcessorTest.java
A Junit test that feeds the parser with CMISQL queries and compares the generated output
against an expected result. While the tests are specific to the output language the basic
principles and design of the unit test can be done in the same way for other parsers.



