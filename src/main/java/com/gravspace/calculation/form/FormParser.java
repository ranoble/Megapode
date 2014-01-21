package com.gravspace.calculation.form;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import scala.concurrent.Future;

import akka.actor.ActorRef;
import akka.actor.UntypedActorContext;
import akka.dispatch.Futures;

import com.gravspace.abstractions.ICalculation;
import com.gravspace.annotations.Calculation;
import com.gravspace.bases.CalculationBase;
import com.gravspace.util.Layers;

@Calculation
public class FormParser extends CalculationBase implements ICalculation, IFormParser {

	Map<String, IFormDataParser> parsers = new HashMap<String, IFormDataParser>();
	public FormParser(Map<Layers, ActorRef> routers,
			ActorRef coordinatingActor, UntypedActorContext actorContext) {
		super(routers, coordinatingActor, actorContext);
		parsers.put("application/x-www-form-urlencoded", new FormEncoding());
		parsers.put("application/json", new JsonEncoding());
		//multipart/form-data
	}

	public Future<List<NameValuePair>> parse(Header[] headers, byte[] data){
		String contentType = null;
		for(Header header: headers) {
			if (StringUtils.equalsIgnoreCase(header.getName(), "Content-Type")){
				contentType = header.getValue().toLowerCase().trim();
				return Futures.successful(parsers.get(contentType).parse(data));
			}
        }
		return Futures.failed(new NotImplementedException(
				String.format("Content Type [%s] is not supported", contentType)));
	}
	
	public static class FormEncoding implements IFormDataParser{
		public List<NameValuePair> parse(byte[] data){
			return URLEncodedUtils.parse(new String(data), Charset.forName("UTF-8"));
		}
	}
	
	public static class JsonEncoding implements IFormDataParser{
		public List<NameValuePair> parse(byte[] data){
			List<NameValuePair> formElements = new ArrayList<NameValuePair>();
			JsonReader reader = Json.createReader(new StringReader(new String(data)));
			JsonObject object = reader.readObject();
			for (String key: object.keySet()){
				//JsonValue value = object.get(key);
				if (object.get(key).getValueType() == ValueType.ARRAY){
					JsonArray values = object.getJsonArray(key);
					for (int i = 0; i < values.size(); i++){
						formElements.add(new BasicNameValuePair(key, values.getString(i)));
					}
//					value
				} else {
					formElements.add(new BasicNameValuePair(key, object.getString(key)));
				}
			}
			return formElements;
//			return URLEncodedUtils.parse(new String(data), Charset.forName("UTF-8"));
		}
	}
}
