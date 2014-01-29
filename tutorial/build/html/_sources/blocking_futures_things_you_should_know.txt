============================================================
Components, Blocking, Futures, and things you should know...
============================================================

You should have a pretty solid understanding of concurrent patterns before 
writing applications in Megapode, but it isn't strictly necessary. The framework 
aims to hold your hand somewhat. 

You can skip this section if you want and come back to it at a later time, but 
a lot of what happens from here, depends on the following subject matter.

Invoking Megapode Components
----------------------------

All core components (except Pages, which are never called directly), make use of 
Dynamic proxies to wire up the messages. These take three forms. 

The *Widgets proxy*, only ever returns a IWidget interface which only has a build 
method. This takes a argument list which is passed into the initialise method of 
the widget and the coordinated methods are then called in order, as described in 
the previous section.

The *Renderers proxy* always returns IRenderer, which allows you to call render in
two ways.

The other components, *DataAccessor, Task, Calculation* all allow you to define an 
interface, and a class, and will route the message accordingly, and return the 
response (except task, which always returns void / null).

Each of these is addressed in their sections in this guide.

Futures
-------

Megapode uses Akka's (Scala's) future model. This [http://doc.akka.io/docs/akka/2.0/java/futures.html] is an excellent primer on the 
topic.

In brief though, a Future is a wrapper for a value that will be fulfilled at some
stage to come. A Promise is returns a future, and allows a developer to fulfil that
future value at some point. 

Most of Megapode consists of passing messages that will be handled later by some 
other process. For this, the future is a tailor made mechanism. This enables us to 
define arbitrary methods, use an interface to define a contract that you can use in 
your client code, and implement a concrete implementation with the same signature 
but very different internals. Dynamic proxies then handle the task of routing 
and fulfilment for you. This is demonstrated in the Calculation section.

Once again, you could just ignore this, and wire the messages yourself, should you 
choose to.

Future completion, and flattening of Futures
--------------------------------------------

OK, so you have a future. This isn't much use. At some stage, you actually need
the result...

You can wait for completion by **blocking**, using:: 

    Object result = Await.result(future, timeout.duration());

You can use a callback:: 

    future.onComplete(new OnComplete<Object>() {
        @Override
        public void onComplete(Throwable arg0, Object response) throws Throwable {
            //handle result here   
        }
    });

Obviously, the non-blocking callback is preferred. But what if you want to use the result to 
call another Future. Look at the following ::

    future.onComplete(new OnComplete<Object>() {
        @Override
        public void onComplete(Throwable arg0, Object response) throws Throwable {
            Future<Object> new_future = someMethod(response);
            new_future.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object response) throws Throwable {
                    ... and potentially more nesting
                }
            });
        }
    });

This could lead to a tangle of nested completions handlers.

Megapode provides a When(){ finishes() }.itWill() pattens to flatten futures. 

When takes a list of arguments, some of them futures and itWill returns a future.
When all of the futures have completed, it will call finishes, passing in all the arguments
in the original order, with the completed results in place of any futures. It then maps 
the new future to the itWill future returned earlier, and will complete accordingly.

Therefore in the above example ::

    // context is the current execution context
    Future future ....
    Future<Object> new_future = (new When(context, future){
            @Override
            Future<?> finishes(Throwable exception, Object... params){
                return someMethod(params[0]);
            }
        }).itWill();
    
Blocking and Components
-----------------------

In general, blocking is to be avoided, but in Megapode two of the Component types block, 
as they need to retain state during their execution. These are the Widget and the Page 
components. By default (it's Megapode, change it if you want...) these components provide 
a default flow. The object is: 

 - constructed 
 - initialised
 - collect is called
 - prepare is called 
 - render is called

and the response is send via a message back to the caller. 

There is a rule here, a developer should attempt to make sure that, as far as is logical, 
as much of the computation is done in parallel as is possible. To do this, most tasks are 
done via calling Futures. But, at some time, you will want the result back, and that 
this returns reliably.

To facilitate this, Megapode provides a set method. To use it however, the field you use 
**must have a setter and a getter**. This is the most common bug that we have found.

The set method takes a field name, and a future, and sets the value of that field with the 
result of that future **before the next coordinated method is called**.
As an example ::

    @Page(path="/")
    public class ArticlePage extends PageBase{ 
        ...
        User author;
        //setter and getter

        @Override
   public void collect() {
            Future<User> futureAuthor = ...
            set("author", futureAuthor);
            //at this point author == null
        }

        @Override
   public void prepare() {
            //at this point author will have been populated or an exception thrown.
        }
        ...
    }

OK, so that takes care of setting, but what if we want to run a number of tasks, perhaps nested? We can create a delaying promise...

Delaying Promise
----------------

if, for some reason, you need to ensure that the next method doesn't start until you have 
completed some work, you can call ::
    
    Promise<Object> delay = delayUntilComplete()

this will cause the next method to wait until the delay is complete, which you can indicate by 
calling delay.successful(...) or delay.failure(...). Normall this is done in a callback, but 
be careful, you will **have** to complete it or the component will not ever complete.
