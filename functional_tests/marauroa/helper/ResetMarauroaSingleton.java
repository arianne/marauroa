package marauroa.helper;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import marauroa.common.Configuration;
import marauroa.common.ConfigurationParams;
import marauroa.common.net.NetConst;
import marauroa.server.marauroad;
import marauroa.server.game.Statistics;


/**
 * resets the static private reference to the daemon thread in marauroa,
 * which follows singleton pattern.
 *
 */
public class ResetMarauroaSingleton {

	public static void resetMarauroa() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	Field field = marauroad.class.getDeclaredField("marauroa");
		field.setAccessible(true);
		field.set(null, null);
		field.setAccessible(false);

		field = Statistics.class.getDeclaredField("stats");
		field.setAccessible(true);
		field.set(null, null);
		field.setAccessible(false);

		field = NetConst.class.getDeclaredField("tcpPort");
		field.setAccessible(true);
		   Field modifiersField = Field.class.getDeclaredField("modifiers");
		    modifiersField.setAccessible(true);
		    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.setInt(null, 3214);
		field.setAccessible(false);


		try {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName("marauroad:name=Statistics"));
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		field = Configuration.class.getDeclaredField("staticParams");
		field.setAccessible(true);
		field.set(null, new ConfigurationParams());
		field = Configuration.class.getDeclaredField("configuration");
		field.setAccessible(true);
		field.set(null, null);
	}

	public static void sysoutthreads() throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		  marauroad.getMarauroa().finish();
		  resetMarauroa();

		 System.out.println("done");
	}

}
