package HTTP;
import java.util.Map;


public class CinemaServlet implements MiniServlet {
	
	/* Para el correcto funcionamiento es necesario un constructor 
	 * sin parámetros y público */
	public CinemaServlet(){
		
	}
	
	public String doGet (Map<String, String> parameters){
		String movie = parameters.get("movie");
		String popcorn = "Let's watch " +movie;
		
		return printHeader() + printBody(popcorn) + printEnd();
	}	

	private String printHeader() {
		return "<html><head> <title>Grab your popcorn!</title> </head> ";
	}

	private String printBody(String popcorn) {
		return "<body> <h1>  " +popcorn+"</h1></body>";
	}

	private String printEnd() {
		return "</html>";
	}
}
