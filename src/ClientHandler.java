import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    Database data = new Database();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("Success");
            writer.flush();

            boolean flag = false;
            do {
                String login = reader.readLine();
                if (login.equals("Log In")) {
                    userLogin(reader, writer);
                } else if (login.equals("Create Account")) {
                    userCreateAccount(reader, writer);
                }
                flag = Boolean.parseBoolean(reader.readLine());
            } while (!flag);

            // now user is logged in with a valid account
            while (true) {
                String input = reader.readLine();
                if (input.equals("Changing Profile")) {
                    changeProfile(reader, writer);
                } else if (input.equals("Logging Out")) {
                    break;
                }
            }

        } catch (IOException e) {
            return;
        }
    }

    public void userLogin(BufferedReader read, PrintWriter write) {
        try {
            String username = read.readLine();
            String pass = read.readLine();

            if (data.loggingIn(username, pass)) {
                write.println("Login good");
                write.flush();
            } else {
                write.println("Login failed");
                write.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void userCreateAccount(BufferedReader read, PrintWriter write) {
        try {
            String info = read.readLine();
            String pass = read.readLine();

            if (data.createAccount(info, pass)) {
                write.println("Login good");
                write.flush();
            } else {
                write.println("Login failed");
                write.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeProfile(BufferedReader read, PrintWriter write) {
        try {
            String decision = read.readLine();

            if (decision.equals("Account, no Password")) {
                String old = read.readLine();
                String newInfo = read.readLine();
                if (data.updateAccount(old, newInfo)) {
                    write.println("Change good");
                    write.flush();
                } else {
                    write.println("Change failed");
                    write.flush();
                }
            } else if (decision.equals("Just Password")) {
                String old = read.readLine();
                String newPass = read.readLine();
                String username = read.readLine();
                String salt = data.randomSalt(5);

                if (data.loggingIn(username, old)) {
                    if (data.deleteUserFromDatabase(username)) {
                        data.insertPassword(username, newPass, salt);
                        write.println("Password changed");
                        write.flush();
                    } else {
                        write.println("Password change failed");
                        write.flush();
                    }
                } else {
                    write.println("Password change failed");
                    write.flush();
                }
            } else if (decision.equals("Both")){
                String oldInfo = read.readLine();
                String newInfo = read.readLine();
                String oldPass = read.readLine();
                String newPass = read.readLine();
                String username = read.readLine();

                if (data.updateAccount(oldInfo, newInfo)) {
                    write.println("Change good");
                    write.flush();
                } else {
                    write.println("Change failed");
                    write.flush();
                }
                String[] newUser = newInfo.split(",");
                String salt = data.getSaltFromDatabase(newUser[0]);

                if (data.loggingIn(newUser[0], oldPass)) {
                    if (data.deleteUserFromDatabase(newUser[0])) {
                        data.insertPassword(newUser[0], newPass, salt);
                        write.println("Password changed");
                        write.flush();
                    } else {
                        write.println("Password change failed");
                        write.flush();
                    }
                } else {
                    write.println("Password change failed");
                    write.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
