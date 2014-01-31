================
What is Megapode
================

A friend of mine came back from a DJUGL meet up one day, with the question:
"Why, if we make use of techniques like map-reduce, queueing, actor based models, etc,
for any data work, do we still write web application code in the same old sequential way?". 
This sparked a discussion, and the concept of Megapode was born. 

The ideal would be to have a system, where each section could be scaled in isolation
of the others, and as much of the processing cold be handled in parallel as possible. 
The focus would be to handle each request as quickly as possible. 

Megapode is a Akka based concurrent MVC framework. In practical terms, 
it is a web framework designed from the ground up to split tasks into 
small, testable components, and run as many of them in parallel as possible. 

Disclaimers: Plenty of frameworks do a very good job at rendering pages quickly and 
providing good throughput. This project is an experiment at how fast we can make the
request/response cycle, and how we can make the framework "think concurrently". 
Therefore, this may not be a problem that needs "solving", but is an interesting 
paradigm to explore.
Also, it is still VERY much in it's infancy, but feel free to play with it and make 
suggestions.

Ideology
========

 - We are more interested in request/response time than in throughput.
 - You are not an idiot, therefore, the framework need not be idiot proof.
 - Nothing should block expect where it REALLY needs to block.
 - Add caching last.
 - Let something like nginx handle static files.

Components
==========

Megapode has a number of core components, these are backed by a set of actors, and 
routers that handle the message passing.

The components are:

Model
-----
 - DataPersistor: All persistance layer interaction are handled by one of these.
 - Calculation: Any processing that requires a response, but can be offloaded to run in 
 parallel should be a Calculation
 -  Task: A fire and forget operation that you don't need to get a response from.

Controller
----------
 - Page: Responsible for page routing, as well as coordinating the other actors. Will
 likely contain Widgets 
 - Widget: A self contained part of the page rendering. It can have other components nested
 within it.

View 
----
 - Renderer
 
And to contain state, there is a session component.


Technologies
============

Megapode is built using pretty standard Java tooling:
 - DbUtils [http://commons.apache.org/proper/commons-dbutils/] for the data access
 - Velocity [http://velocity.apache.org] for template rendering
 - Jersey's [https://jersey.java.net/] path handling 
 - Apache HTTPCore [http://hc.apache.org/httpcomponents-core-ga/] for a basic HTTPServer
 - Akka [http://akka.io/] for message passing and actors
 
Although the plan in the future is to allow a user to swap out a component however they 
want.

Y u no have Hibernate?
======================

Because while Hibernate is a fantastic ORM, we felt that the ORM mechanism didn't fit the 
message paradigm that Megapode is built on. 

