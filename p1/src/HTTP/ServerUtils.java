package HTTP;
import java.util.Map;

public class ServerUtils {

	public static String processDynRequest(String className,
			Map<String, String> parameters) throws Exception {

		MiniServlet servlet;
		Class<?> instance;

		instance = Class.forName(className);
		servlet = (MiniServlet) instance.newInstance();

		return servlet.doGet(parameters);

	}
}
