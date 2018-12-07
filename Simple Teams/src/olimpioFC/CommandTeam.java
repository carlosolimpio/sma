package olimpioFC;

import simple_soccer_lib.AbstractTeam;
import simple_soccer_lib.PlayerCommander;

public class CommandTeam extends AbstractTeam {
	public CommandTeam(String name) {
		super(name + "FC", 7, true);
	}
	
	@Override
	protected void launchPlayer(int ag, PlayerCommander comm) {
		System.out.println("Player Lançado!");
		CommandPlayer p = new CommandPlayer(comm);
		p.start();
	}
}