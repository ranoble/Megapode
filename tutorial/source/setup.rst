=====
Setup
=====


1. First Steps
--------------
 - Create a database, username and password. 
 - Get Maven installed and set up.
 - Make sure that you are using Java 7 or greater. 

2. Import the Megapode dependancy
---------------------------------
To make the setup as quick and easy as possibly, we have set up a public maven 
repository that you can import Megapode. 

Simply add the following to your repositories section of your POM::
  	    
    <repository>
        <id>megapode-snapshots</id>
        <name>Megapode Snapshots</name>
        <url>http://megapode.info:8081/nexus/content/repositories/megapode-snapshot/</url>
    </repository>

Next, add the dependancy in the dependancies section::

    <dependency>
        <groupId>com.gravspace</groupId>
        <artifactId>megapode</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

3. Create a configuration  file
-------------------------------
By default the configuration is controlled by the property file megapode.conf, 
packaged in the application package. You can override this by pointing the application 
at a file on the local filesystem. Create this file in your src/main/resources. 

This is a sample::

    # scans for application setup
    scan-tasks=com.gravspace.sandbox.task
    scan-calculations=com.gravspace.sandbox.calculation
    scan-dataaccessors=com.gravspace.sandbox.data
    scan-renderers=com.gravspace.defaults
    scan-widgets=com.gravspace.sandbox.widget
    scan-pages=com.gravspace.sandbox.page

    # actor setup
    pages=5
    tasks=5
    calculations=5
    widgets=5
    renders=5
    dataaccessors=5

    #database
    user=postgres
    password=postgres
    url=jdbc:postgresql://xxx.xxx.xxx.xx/megapode_test
 
3.1. The Scanner Section
========================

Each component in Megapode is annotated according to it's type, and the packages
defined here are scanned for these annotations. If an annotation is found, then that
class is loaded as that component type. This will be discussed in more detail, but 
for now, just create a task, calculation, data, renderer, widget and page package, 
and set the scanners accordingly.

3.2 The Actor Section
=====================

Each component type has an actor pool that is created to handle messages for that 
section. This allows tasks to be distributed. Each actor is persistent, and will 
remain active and listening for tasks as needed.  Leave them as they are, but if you 
don't have a database, then you can just set the dataaccessors to zero.

3.3 The Database Section
========================

This is only used if the dataaccessors > 0. It uses standard JDBC syntax, and you should 
remember to include the relevant driver dependancy.

4. Create your HTTP Server
--------------------------

As megapode has an embedded HTTP Server, you'll need to create a runner, and potentially 
modify how it runs. 

Create a class that runs  HttpServer.start(Object...) in the main method

This should suffice for now... ::

    package your.package.name;

    import com.gravspace.core.HttpServer;

    public class Server { 
        public static void main(String[] args) throws Exception {
            HttpServer.start(args);
        }
    } 

You can now run the server, which, by default runs on port 8082.

Once you have checked that the server runs, we can move on to create our first Megapode page!


