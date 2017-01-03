import gui.Gui;


//TODO: Move the "_listen" hack from Property to JTextField2
//TODO: Option (action) to retrieve lost file from local computer into the backup
//TODO: Add a last backup datetime somewhere?
//TODO: Add correct screenshot into website
//TODO: Helper to create destinations directory in config panel (= default dest dir + src dir name)

//TODO: BUG: Page up / down does not work in JLobPanel
//TODO: BUG: when run a backup, the + and - and ignored list are not disabled!

public class Main {
	
	public static void main(String[] args) {
		new Gui();
	}
}
