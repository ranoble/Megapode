import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.JavaTestKit;
import akka.util.Timeout;

import com.gravspace.abstractions.ConcurrantCallable;
import com.gravspace.abstractions.IRenderer;
import com.gravspace.abstractions.IWidget;
import com.gravspace.impl.tasks.IProfileCalculation;
import com.gravspace.messages.RouterMessage;
import com.gravspace.messages.RouterResponseMessage;
import com.gravspace.page.IProfileDataAccessor;
import com.gravspace.page.ProfileCalculation;
import com.gravspace.page.ProfileDataAccessor;
import com.gravspace.page.ProfileWidget;
import com.gravspace.proxy.Calculations;
import com.gravspace.proxy.DataAccessors;
import com.gravspace.proxy.Renderers;
import com.gravspace.proxy.Widgets;

import core.CallableContainer;
import core.GetCallable;
import core.TestCoordinator;

public class BaseTests{

	private static ActorSystem system;
	private static Properties config;
	private static CallableContainer cc;

	public BaseTests() throws IOException {
		super();
	}

	@BeforeClass
	public static void setup() {
		system = ActorSystem.create();
		config = new Properties();
		try {
			config.load(Properties.class
					.getResourceAsStream("/megapode_test.conf"));
			cc = new CallableContainer();
			ActorRef master = system.actorOf(
					Props.create(TestCoordinator.class, config, cc),
					"Coordinator");
			Timeout timeout = new Timeout(Duration.create(1, "minute"));
			Future<Object> routerMessage = Patterns.ask(master,
					new GetCallable(), timeout);				
			cc = (CallableContainer) Await.result(routerMessage, Duration.create(1, "minute"));
			Assert.assertNotNull(cc.getCallable());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void teardown() {
		JavaTestKit.shutdownActorSystem(system);
		system = null;
	}

	@Test
	public void testCalculationProxy() throws Exception {
		new JavaTestKit(system) {
			{
	
				
				IProfileCalculation calc = Calculations.get(IProfileCalculation.class, ProfileCalculation.class, cc.getCallable());
				Future<String> result = calc.getThree();
				
				String res = (String) Await.result(result, Duration.create(1, "minute"));
				Assert.assertEquals("Three!", res);
			}
		};
	}
	
	@Test
	public void testDataProxy() throws Exception {
		new JavaTestKit(system) {
			{
				
				
				IProfileDataAccessor data = DataAccessors.get(IProfileDataAccessor.class, ProfileDataAccessor.class, cc.getCallable());
				Future<Map<String, Object>> result = data.getUserProfile(1);
				
				Map<String, Object> res = (Map<String, Object>) Await.result(result, Duration.create(1, "minute"));
				Assert.assertEquals("mega.pode@bigfootbirdie.com", res.get("email"));
			}
		};
	}
	
	@Test
	public void testRenderer() throws Exception {
		new JavaTestKit(system) {
			{
				
				
				IRenderer data = Renderers.getDefault(cc.getCallable());
				Map<String, String> context = new HashMap<>();
				context.put("context_var", "Megapode");
				Future<String> result = data.render("test_template.vm", context);
				
				String res = (String) Await.result(result, Duration.create(1, "minute"));
				Assert.assertEquals("Hi Megapode", res);
			}
		};
	}
	
	@Test
	public void testWidget() throws Exception {
		new JavaTestKit(system) {
			{
				IWidget data = Widgets.get(ProfileWidget.class, cc.getCallable());//getDefaultRender(cc.getCallable());

				Future<String> result = data.build(1, 2, 3);
				
				String res = (String) Await.result(result, Duration.create(1, "minute"));
				Assert.assertEquals("Hi Component", res);
			}
		};
	}


}
