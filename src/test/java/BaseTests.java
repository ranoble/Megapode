import java.io.IOException;
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
import com.gravspace.impl.tasks.IProfileCalculation;
import com.gravspace.messages.RouterMessage;
import com.gravspace.messages.RouterResponseMessage;
import com.gravspace.page.ProfileCalculation;
import com.gravspace.proxy.CalculationProxyFactory;

import core.CallableContainer;
import core.GetCallable;
import core.TestCoordinator;

public class BaseTests{

	private static ActorSystem system;
	private static Properties config;

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
		} catch (IOException e) {
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
	public void testProxy() throws Exception {
		new JavaTestKit(system) {
			{
				CallableContainer cc = new CallableContainer();
				ActorRef master = system.actorOf(
						Props.create(TestCoordinator.class, config, cc),
						"Coordinator");
				Timeout timeout = new Timeout(Duration.create(1, "minute"));
				Future<Object> routerMessage = Patterns.ask(master,
						new GetCallable(), timeout);				
				cc = (CallableContainer) Await.result(routerMessage, Duration.create(1, "minute"));
				Assert.assertNotNull(cc.getCallable());
				
				IProfileCalculation calc = CalculationProxyFactory.getProxy(IProfileCalculation.class, ProfileCalculation.class, cc.getCallable());
				Future<String> result = calc.getThree();
				
				String res = (String) Await.result(result, Duration.create(1, "minute"));
				Assert.assertEquals("Three!", res);
			}
		};
	}

}
