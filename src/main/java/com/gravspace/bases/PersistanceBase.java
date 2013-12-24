package com.gravspace.bases;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.IPersistanceAccessor;
import com.gravspace.util.Layers;

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
	
}
