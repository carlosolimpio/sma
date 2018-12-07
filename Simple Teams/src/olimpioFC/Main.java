package olimpioFC;

import java.net.UnknownHostException;

public class Main {
	public static void main(String[] args) {
		
		try {
			CommandTeam teamA = new CommandTeam("Sport");
			
			teamA.launchTeamAndServer();
		} catch (UnknownHostException e) {
			System.out.println("Deu ruim...");
		}
	}
}
