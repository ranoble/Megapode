package com.gravspace.handlers;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.gravspace.abstractions.PersistanceAccessor;
import com.gravspace.messages.PersistanceMessage;
import com.gravspace.util.Layers;

public class PersistanceHandler extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	Map<String, Class<? extends PersistanceAccessor>> persistanceTasks;
	private Map<Layers, ActorRef> routers;
	private Connection connection;
	
	public PersistanceHandler(Map<Layers, ActorRef> routers, 
			Map<String, Class<? extends PersistanceAccessor>> persistanceTasks,
			Properties connectionDetails){
		this.persistanceTasks = persistanceTasks;
		this.routers = routers;
		try {
			this.connection = connect(connectionDetails);
			// maybe try and reconnect?
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void postStop(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	protected Connection connect(Properties props) throws SQLException{
		String url = props.getProperty("url");
		return DriverManager.getConnection(url, props);
	}


	@Override
	public void onReceive(Object rawMessage) throws Exception {
		log.info("CalculationHandler got: "+rawMessage.getClass().getCanonicalName());
		if (rawMessage instanceof PersistanceMessage){
			PersistanceMessage message = (PersistanceMessage)rawMessage;

			String task_name = message.getPersistanceTask();
			Class<? extends PersistanceAccessor> calculationClass = persistanceTasks.get(task_name);
			Constructor<? extends PersistanceAccessor> constr = calculationClass.getConstructor(Map.class, ActorRef.class, UntypedActorContext.class, Connection.class);
			PersistanceAccessor persistor = constr.newInstance(routers, getSender(), this.context(), this.connection);
			Map<String, ?> result = persistor.performTask(message.getArgs().toArray());
			getSender().tell(result, getSelf());
		} 
		else {
			unhandled(rawMessage);
		}
	}

}
