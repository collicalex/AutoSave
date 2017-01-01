import gui.Gui;


//TODO: Move the "_listen" hack from Property to JTextField2
//TODO: Prevent standby
//TODO: Prevent hibernate
//TODO: Option (action) to retrieve lost file from local computer into the backup
//TODO: Add a last backup datetime somewhere?
//TODO: Add correct screenshot into website

//TODO: Bug save conf at first time will not put back save button in black
//TODO: Bug when run a backup, the + and - and ignored list are not disabled!

public class Main {
	
	public static void main(String[] args) {
		new Gui();
	}
}
