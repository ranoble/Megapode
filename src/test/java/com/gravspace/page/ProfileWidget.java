package com.gravspace.page;

import java.util.Map;

import scala.concurrent.Future;
import scala.concurrent.Promise;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;

import com.gravspace.abstractions.IComponent;
import com.gravspace.abstractions.IWidget;
import com.gravspace.annotations.Widget;
import com.gravspace.bases.ComponentBase;
import com.gravspace.proxy.DataAccessors;
import com.gravspace.util.Layers;

@Widget
public class ProfileWidget extends ComponentBase implements IComponent {

	Map<String, Object> profileContext;
	public ProfileWidget(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
	}

	public void initialise(Object... args) {
		// TODO Auto-generated method stub

	}

	public void collect() {
		IProfileDataAccessor dp = DataAccessors.get(IProfileDataAccessor.class, ProfileDataAccessor.class, this);
		set("profileContext", dp.getUserProfile(1));//ask(new PersistanceMessage("doX", Arrays.asList(new Integer[]{1}))));

		getLogger().info("collected");

	}

	public void process() {
		getLogger().info("processing...");
		getLogger().info((String) profileContext.get("firstname"));
		getLogger().info("processed");
	}

	public Future<String> render() throws Exception {
		getLogger().info("rendering...");
		// TODO Auto-generated method stub
		try {
			return Futures.successful("Hi Component");
		} finally {
			getLogger().info("rendered");
		}
	}

	public Map<String, Object> getProfileContext() {
		return profileContext;
	}

	public void setProfileContext(Map<String, Object> profileContext) {
		this.profileContext = profileContext;
	}

}
