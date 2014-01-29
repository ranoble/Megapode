=======
Widgets
=======

Widgets are parts of a page, that will be rendered. They can be a profile block, 
a comments feed, a comment in 

Like Pages, Widgets make use of the coordinated blocking system, described in 
the Blocking and Components section. 

They follow a set pattern of method calls: 

 - contruct
 - initialise
 - collect
 - prepare
 - render
 
In the same way as pages, any calls to other components will finish 
before the next method is called. Unlike Pages, however, Widgets are can be called 
from another component, using the the Widgets proxy.

Like other components, Widgets need to have the Widget annotation, and implement IWidget,
or extend WidgetBase. 

There is only one method, the build method. This takes the arguments passed into the 
method, and (after the message passing), uses them to initialise the widget. 

It then follows the defined flow.