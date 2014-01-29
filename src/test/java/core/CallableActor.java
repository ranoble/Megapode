package core;

import java.util.Map;

import com.gravspace.bases.ConcurrantCallable;
import com.gravspace.util.Layers;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class CallableActor extends UntypedActor {

	private CallableContainer cc;
	public CallableActor(Map<Layers, ActorRef> routers, ActorRef master, CallableContainer cc){
		this.cc = cc;
		this.cc.setCallable(new ConcurrantCallable(routers, master, this.getContext()));
		//cc.setActorContext(this.getContext());
	}
	@Override
	public void onReceive(Object arg0) throws Exception {
		sender().tell(this.cc, self());
		
	}

}
