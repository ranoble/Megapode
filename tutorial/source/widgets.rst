=======
Widgets
=======

1. Basic Widgets
----------------

Generally a HTML page is made up of a number of self contained compontents.
We call these Widgets in Megapode. Think of a e-commerce site. Widgets could
be the Basket, the Navigation, each product view could be a widget and so on
and so forth.

So let's add a few items to our new homepage. We will add a profile widget, 
and a comments widget, where a user can see what comments their friends 
have made. 

Widgets are parts of a page, that will be rendered. They can be a profile block, 
a comments feed.


2. Widget Execution
-------------------

Like Pages, Widgets make use of the coordinated blocking system, described in 
the :ref:`blocking_futures_things_you_should_know` section. 

They follow a set pattern of method calls: 

 - contruct
 - initialise
 - collect
 - prepare
 - render
 
3. Widget Structure
-------------------

A default Widget will then look something like this::

   @Widget
   public class YourBasicWidget extends WidgetBase   {
      public UserProfileWidget(Map<Layers, ActorRef> routers,
            ActorRef coordinatingActor, UntypedActorContext actorContext) {
         super(routers, coordinatingActor, actorContext);
      }
   
      @Override
      public void initialise(Object... args) {
         //these are the parameters that were passed into the build call.
      }
   
      @Override
      public void collect() {
         //collect the data you want
      }
   
      @Override
      public void process() {
         // process data
      }
   
      @Override
      public Future<String> render() throws Exception {
         IRenderer renderer = Renderers.getDefault(this);
         Map<String, Object> context = new HashMap<String, Object>();
         //add context data
         
         return renderer.render("some.template.html", context);
      }
   }

 
4. Calling a Widget from another Widget (or Page)
-------------------------------------------------

In the same way as pages, any calls to other components will finish 
before the next method is called. Unlike Pages, however, Widgets are can be called 
from another component, using the the Widgets proxy.

Like other components, Widgets need to have the Widget annotation, and implement IWidget,
or extend WidgetBase. 

There is only one method, the build method. This takes the arguments passed into the 
method, and (after the message passing), uses them to initialise the widget::

   //TODO: example


5. Adding to our Project
------------------------

It then follows the defined flow. We'll need a couple of beans to hold our 
data. Lets add a Comment and a User::

   public class Comment {
      Integer id;
      Integer userId;
      String comment;
      Date created;
      
      public Integer getId() {
         return id;
      }
      public void setCommentId(Integer id) {
         this.id = id;
      }
      public Integer getUserId() {
         return userId;
      }
      public void setUserId(Integer userId) {
         this.userId = userId;
      }
      public String getComment() {
         return comment;
      }
      public void setComment(String comment) {
         this.comment = comment;
      }
      public Date getCreated() {
         return created;
      }
      public void setCreated(Date created) {
         this.created = created;
      }
   }

and ::
   
   public class User {
      Integer id;
      String firstname;
      String lastname;
      String email;
      String profileImage;
      
      public Integer getId() {
         return id;
      }
      public void setId(Integer id) {
         this.id = id;
      }
      public String getFirstname() {
         return firstname;
      }
      public void setFirstname(String firstname) {
         this.firstname = firstname;
      }
      public String getLastname() {
         return lastname;
      }
      public void setLastname(String lastname) {
         this.lastname = lastname;
      }
      public String getEmail() {
         return email;
      }
      public void setEmail(String email) {
         this.email = email;
      }
      public String getProfileImage() {
         return profileImage;
      }
      public void setProfileImage(String profileImage) {
         this.profileImage = profileImage;
      }
   }
      



So for the Profile Widget, we can now use::

   @Widget
   public class ProfileWidget extends WidgetBase   {
      User user;
   
      public UserProfileWidget(Map<Layers, ActorRef> routers,
            ActorRef coordinatingActor, UntypedActorContext actorContext) {
         super(routers, coordinatingActor, actorContext);
      }
   
      @Override
      public void initialise(Object... args) {
         user = new User();
      }
   
      @Override
      public void collect() {
         //collect the data you want
      }
   
      @Override
      public void process() {
         // process data
      }
   
      @Override
      public Future<String> render() throws Exception {
         IRenderer renderer = Renderers.getDefault(this);
         Map<String, Object> context = new HashMap<String, Object>();
         context.put("user", user);
         return renderer.render("widget.profile.html", context);
      }
   }

Dont forget to add the profile template [widget.profile.html] ::
   
   <div>
       <img src="$user.profileImage" />
       <ul>
       <li>$user.firstname $user.lastname</li>
       <li><a href="mailto: $user.email">email</a></li>
       </ul>
   </div>

Next, we can add a Comments Widget::

   @Widget
   public class ProfileWidget extends WidgetBase   {
      List<Comment> comments;
   
      public UserProfileWidget(Map<Layers, ActorRef> routers,
            ActorRef coordinatingActor, UntypedActorContext actorContext) {
         super(routers, coordinatingActor, actorContext);
      }
   
      @Override
      public void initialise(Object... args) {
         comments = new ArrayList<Comment>();
         //these are the parameters that were passed into the build call.
      }
   
      @Override
      public void collect() {
         for (int i = 0; i < 3; i++){
            Comment comment = new Comment();
            comment.setComment(String.format("Comment %d", (i+1)));
            comment.setCreated(new Date());
            comments.add(comment);
         }
      }
   
      @Override
      public void process() {
         // process data
      }
   
      @Override
      public Future<String> render() throws Exception {
         IRenderer renderer = Renderers.getDefault(this);
         Map<String, Object> context = new HashMap<String, Object>();
         context.put("comments", comments);
         return renderer.render("widget.comments.html", context);
      }
   }
   
and the comments template::

   #foreach( $comment in $comments )
   <li>
       $comment.getComment()
   </li>
   #end
   
Now, we could break this down further into each comment being a component. 
The rule of thumb is, the smaller the task the better. 
