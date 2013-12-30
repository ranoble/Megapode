package com.gravspace.bases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.IPersistanceAccessor;
import com.gravspace.util.Layers;
import com.gravspace.util.TypeUtils;

public abstract class PersistanceBase extends ConcurrantCallable implements
		IPersistanceAccessor {

	protected Connection connection;

	public PersistanceBase(Map<Layers, ActorRef> routers, 
			ActorRef coordinatingActor, 
			UntypedActorContext actorContext, 
			Connection connection) {
		super(routers, coordinatingActor, actorContext);
		this.connection = connection;
		
	}
	
	public int update(CallableStatement statement, Map<String, ?> args) throws SQLException{
		statement = (CallableStatement) prepareStatement(statement, args);
		return statement.executeUpdate();
	}
	

	public ResultSet fetch(Statement statement) throws SQLException{
		return statement.getResultSet();
		
	}
	
	public ResultSet fetch(CallableStatement statement, Map<String, ?> args) throws SQLException{
		statement = (CallableStatement) prepareStatement(statement, args);
		return statement.getResultSet();
		
	}

	private Statement prepareStatement(CallableStatement statement,
			Map<String, ?> args) {
		for (String key: args.keySet()){
			try {
				statement.setObject(key, args.get(key));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return statement;
	}
	
	public Map<String, ?> performTask(Object... args) throws Exception {
		List<Object> arguments = new ArrayList<>(Arrays.asList(args));
		String methodName = (String) arguments.remove(0);
		List<Class<?>> types = TypeUtils.getListTypes(arguments);
		try {
			Method method = this.getClass().getMethod(methodName, types.toArray(new Class[0]));
			//Do I do it here?
			return (Map<String, ?>) method.invoke(this, arguments.toArray(new Object[0]));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
}
