package com.gravspace.page;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;

import com.gravspace.abstractions.IPersistanceAccessor;
import com.gravspace.bases.PersistanceBase;
import com.gravspace.util.Layers;
//http://commons.apache.org/proper/commons-dbutils/examples.html
public class GetProfileData extends PersistanceBase implements
		IPersistanceAccessor {

	public GetProfileData(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext,
			Connection connection) {
		super(routers, coordinatingActor, actorContext, connection);
		// TODO Auto-generated constructor stub
	}

	public Map<String, ?> performTask(Object... args) {
		return getUserProfile((Integer)args[0]);
	}
	
	private Map<String, ?> getUserProfile(Integer id){
//		return new HashMap<String>
		QueryRunner run = new QueryRunner();
		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		try {
			List<Map<String, Object>> results = run.query(this.connection, "select * from profile where id = "+id, h);
			return results.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
