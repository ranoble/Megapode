===================
Create a basic Page
===================

1. The Package Setup
--------------------

Before we start building a page, we need to check that we have created a package for the 
Page to be scanned in. So go ahead and create a package, your.package.name.page. 
Next, check that you have added this package to your config file.::

    scan-pages=com.gravspace.sandbox.page


2. Create a new Page
--------------------

To make a page available to Megapode, there a few things you will need to do.

 - Create a class
 - Annotate the class as a [com.gravspace.annotations.]Page.
 - Configure routing for the page
 - Implement [com.gravspace.abstractions.]IPage or one of it's implementations.

We'll handle these in logical groups.

2.1. The annotation and routing
===============================
The Page annotation has two function, first is to flag the class as a page, and the 
second is define the route for the Page. 

The annotation has a path directive for this purpose. It uses the Jersey path matching 
structure. 

So to define a base root path, annotate your page class as follows::

    @Page(path="/")
    public class YourPage {}

And to add an article page, your could define something like::

    @Page(path="/article/{articleId}")
    public class ArticlePage {}

articleId will then be made available to you in a map. This will be demonstrated in
next section.

2.2 Implement IPage
===================

One of the ideals of Megapode is that the framework should not limit the developer, 
so you *could* simply implement IPage and wire up the implementation yourself. But
the framework provides a much simpler framework for this.

Simply extend com.gravspace.bases.PageBase. This will provide the core functionality
to the page. This includes method extraction, basic form handling, session handling,
as well as message handling. 

There are a number of default methods for a page, and they'll be discussed in the next 
section. 

So change your class to look something like this ::

    @Page(path="/")
    public class HomePage extends PageBase {
        public HomePage(Map<Layers, ActorRef> routers, ActorRef coordinatingActor, 
            UntypedActorContext actorContext) {
            super(routers, coordinatingActor, actorContext);
        }
    
        @Override
        public void process() {}

        @Override
        public Future<String> render() throws Exception {
            return Futures.success("<html><body>Hello World!</body></html>")
        } 
    } 

3. Add a Template
-----------------

What use is a web framework, without a decent renderer. Megapode uses velocity 
to render templates. You can read more at :ref:`template_rendering`. But to 
get us started we need to add a template, and add some code to the render method 
of the page. 

Create a template, call it something like home.html::

   <html>
      <body>
         Hello $name, from Renderer!
      </body>
   </html>
   
Then add the following to the render method:: 

   @Override
   public Future<String> render() throws Exception {
      IRenderer renderer = Renderers.getDefault(this);
      Map<String, Object> context = new HashMap<String, Object>();
      context.put("name", "Your Name");
      return renderer.render("home.html", context); 
   } 

 
4. Check your Page
------------------

You now have a fully functional Megapode Page. Just start your server, and go to 
your path. 


