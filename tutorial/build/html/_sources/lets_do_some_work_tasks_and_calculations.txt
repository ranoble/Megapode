==========================================
Lets do some Work, Tasks and Calculations.
==========================================

To recap, a Task is a fire and forget job, where we do not wait for a response, while 
a Calculation requires a response to be given.

As always, you could skip all of the niceties, and write a raw message to be passed 
to one of these components, but there are safer and easier ways provided. 

As described in the section on Components, you will use the dynamic proxy mechanism
to interact with the tasks and components.

So, how does this work?

Tasks
=====

A Task, like a Page, must be annotated, using the com.gravspace.annotations.Task annotation.
It must be scanned in a package defined in the config file. 

It must implement com.gravspace.abtractions.ITask, or extend com.gravspace.bases.TaskBase (preferred). Once again, if you don't extend TaskBase then you'll have to route the message 
to  the correct method yourself.

Now, you will want to define the public methods you want on the task, and make sure that they all have void return values. You then need to define and interface that has the same method signatures as the tasks you need. You should implement that interface:: 

    interface IWantToPerformATask {
        void performRandomTask(String str, Integer _int);
    }
     
    @Task
    public class WantToPerformATaskImplementation extends TaskBase implements IWantToPerformATask {
        public WantToPerformATaskImplementation(Map<Layers, ActorRef> routers, 
            ActorRef coordinatingActor, UntypedActorContext actorContext) {
                super(routers, coordinatingActor, actorContext);
        }
        
        @Override
        public void performRandomTask(String str, Integer _int){
            ... do some task
        }
    }

Now, to **use** the task. From a Megapode Component, or extended ConcurrantCallable (all of the Components extend ConcurrantCallable), you need to "get" the proxy::

    IWantToPerformATask task = Tasks.get(IWantToPerformATask.class, WantToPerformATaskImplementation.class, this);
    task.performRandomTask("Hello", 3);

Tasks takes three arguments, the Interface class, the Implementation class and the calling
ConcurrantCallable. Once you have the proxy, you can just call the method.

This will send a message to the TaskHandler and handle the task asynchronously.
*A task does not retain state.* So do not attempt to use properties. 

Calculations
============

A calculation works identically to a task except that the calculation returns a future.

So, in the same way, a Calculation must have the com.gravspace.annotation.Calculation 
annotation, implement com.gravspace.abstraction.ICalculation or preferably extend com.gravspace.bases.CaclulationBase. 

You would then define the Calculation methods you want to call, which **must** return 
Futures, and have the same in an interface.::

    interface IDoSomeCalculation {
        Future<String> calculateSomething(String str, Integer _int);
    }
     
    @Calculation
    public class DoSomeCalculationImplementation extends CalculationBase implements IDoSomeCalculation {
        public DoSomeCalculationImplementation(Map<Layers, ActorRef> routers, 
            ActorRef coordinatingActor, UntypedActorContext actorContext) {
                super(routers, coordinatingActor, actorContext);
        }
        
        @Override
        public Future<String> calculateSomething(String str, Integer _int){
            return Futures.success(String.format("%s: %d", str, _int));
        }
    }

Similarly, a calculation can be called from a ConcurrantCallable as such ::

    IDoSomeCalculation calc = Tasks.get(IDoSomeCalculation.class, DoSomeCalculationImplementation.class, this);
    Future<String> future = calc.calculateSomething("Hello", 3);

Handling the resultant Future
-----------------------------

If the ConcurrantCallable is a Widget or a Page, then you could set the future to a 
property as defined in the last chapter. Alternatively, you will need to handle it
using one of the other completion mechanism (also discussed in the previous section).

