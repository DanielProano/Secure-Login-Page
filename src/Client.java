import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Client {
    private User user;
    Database data = new Database();
    private JFrame frame;
    private BufferedReader read;
    private PrintWriter write;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Client client = new Client();
                client.runClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void runClient() {
        frame = new JFrame("My Social Media App");
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        String host = "localhost";
        int port = 6060;
        boolean close;
        try {
            Socket socket = new Socket(host, port);
            read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            write = new PrintWriter(socket.getOutputStream(), true);

            String isConnected = read.readLine();

            if (isConnected.equalsIgnoreCase("Success")) {
                close = false;
                loginPage();
            } else {
                close = true;
            }

            if (close) {
                write.close();
                read.close();
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loginPage() {
        frame.getContentPane();

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel username = new JLabel("Username:");
        JTextField userText = new JTextField(20);
        JLabel password = new JLabel("Password:");
        JPasswordField passText = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        JButton createButton = new JButton("Create Account");

        username.setBounds(50, 50, 100, 30);
        userText.setBounds(150, 50, 200, 30);
        password.setBounds(50, 100, 100, 30);
        passText.setBounds(150, 100, 200, 30);
        loginButton.setBounds(150, 150, 100, 30);
        createButton.setBounds(150, 200, 150, 30);

        panel.add(username);
        panel.add(userText);
        panel.add(password);
        panel.add(passText);
        panel.add(loginButton);
        panel.add(createButton);

        frame.add(panel);

        loginButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // tell server the login button was clicked
                    write.println("Log In");
                    write.flush();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        String usernameSend = userText.getText();
                        write.println(usernameSend);
                        String passSend = String.valueOf(passText.getPassword());
                        write.println(passSend);
                        write.flush();

                        if (read.readLine().equals("Login good")) {
                            String userInfo = data.returnUserInfo(usernameSend);
                            user = new User(userInfo);

                            write.println(true);
                            write.flush();
                            homePage();
                        } else {
                            write.println(false);
                            write.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        });

        createButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // tell server the login button was clicked
                    write.println("Create Account");
                    write.flush();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        frame.getContentPane().removeAll();

                        JPanel panel = new JPanel();
                        panel.setLayout(null);

                        JLabel username = new JLabel("Select a username");
                        JTextField userText = new JTextField(20);
                        JLabel password = new JLabel("Select a password");
                        JPasswordField passText = new JPasswordField(20);
                        JLabel email = new JLabel("What is your email");
                        JTextField emailText = new JTextField(20);
                        JLabel bio = new JLabel("Create your bio");
                        JTextField bioText = new JTextField(20);

                        JButton createButtonSecond = new JButton("Create Account");


                        username.setBounds(40, 50, 200, 30);
                        userText.setBounds(150, 50, 200, 30);
                        password.setBounds(40, 100, 200, 30);
                        passText.setBounds(150, 100, 200, 30);
                        email.setBounds(40, 150, 200, 30);
                        emailText.setBounds(150, 150, 200, 30);
                        bio.setBounds(55, 200, 200, 30);
                        bioText.setBounds(150, 200, 200, 30);

                        createButtonSecond.setBounds(150, 300, 150, 30);

                        panel.add(username);
                        panel.add(userText);
                        panel.add(password);
                        panel.add(passText);
                        panel.add(email);
                        panel.add(emailText);
                        panel.add(bio);
                        panel.add(bioText);
                        panel.add(createButtonSecond);

                        createButtonSecond.addActionListener(e -> {
                            try {
                                String usernameInput = userText.getText();
                                String emailInput = emailText.getText();
                                String passwordInput = String.valueOf(passText.getPassword());
                                String bioInput = bioText.getText();

                                String output = "";
                                output += usernameInput + "," + emailInput + "," + bioInput;

                                user = new User(output);

                                write.println(output);
                                write.println(passwordInput);
                                write.flush();

                                if (read.readLine().equals("Login good")) {
                                    write.println(true);
                                    write.flush();
                                    homePage();
                                } else {
                                    write.println(false);
                                    write.flush();
                                }
                            } catch (IOException i) {
                                i.printStackTrace();
                            }
                        });

                        frame.add(panel);
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        });
    }

    public void homePage() {
        frame.getContentPane().removeAll();

        JPanel panel = new JPanel();
        panel.setLayout(null);

        String usernameDisplay = user.getUsername();
        JLabel username = new JLabel(usernameDisplay);
        username.setBounds(10, 10, 100, 30);

        String bioDisplay = user.getBio();
        JLabel bio = new JLabel(bioDisplay);
        bio.setBounds(10, 50, 100, 30);

        String emailDisplay = user.getEmail();
        JLabel email = new JLabel(emailDisplay);
        email.setBounds(300, 10, 100, 30);

        JButton changeProfile = new JButton("Change Profile");
        changeProfile.setBounds(160, 420, 140, 30);

        JButton exit = new JButton("Exit");
        exit.setBounds(310, 420, 60, 30);

        panel.add(username);
        panel.add(bio);
        panel.add(email);
        panel.add(changeProfile);
        panel.add(exit);

        changeProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateInfo();
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        frame.add(panel);
        frame.revalidate();
        frame.repaint();

    }

    public void updateInfo() {
        frame.getContentPane().removeAll();

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JButton back = new JButton("Back");
        back.setBounds(10, 10, 80, 30);

        JLabel changeUsername = new JLabel("Enter a new username");
        JLabel changeEmail = new JLabel("Enter a new email");
        JLabel changeBio = new JLabel("Enter a new bio");
        JLabel confirmPassword = new JLabel("Enter old password");
        JLabel changePass = new JLabel("Enter a new password");
        JButton changeInfoButton = new JButton("Update");

        JTextField usernameText = new JTextField(20);
        JTextField emailText = new JTextField(20);
        JTextField bioText = new JTextField(20);
        JPasswordField confirmPassText = new JPasswordField(20);
        JPasswordField newPassText = new JPasswordField(20);

        changeUsername.setBounds(30, 50, 200, 30);
        usernameText.setBounds(175, 50, 200, 30);
        changeEmail.setBounds(30, 100, 200, 30);
        emailText.setBounds(175, 100, 200, 30);
        changeBio.setBounds(30, 150, 200, 30);
        bioText.setBounds(175, 150, 200, 30);
        confirmPassword.setBounds(30, 250, 200, 30);
        confirmPassText.setBounds(175, 250, 200, 30);
        changePass.setBounds(30, 300, 200, 30);
        newPassText.setBounds(175, 300, 200, 30);
        changeInfoButton.setBounds(275, 350, 100, 30);

        panel.add(changeUsername);
        panel.add(usernameText);
        panel.add(changeEmail);
        panel.add(emailText);
        panel.add(changeBio);
        panel.add(bioText);
        panel.add(confirmPassword);
        panel.add(confirmPassText);
        panel.add(changePass);
        panel.add(newPassText);
        panel.add(changeInfoButton);

        panel.add(back);

        changeInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                write.println("Changing Profile");
                write.flush();

                String[] replaceInfo = user.toString().split(",");
                String oldInfo = user.toString();
                String[] oldArray = oldInfo.split(",");
                String oldPass = "";
                String newPass = "";
                boolean updateAccount = false;
                boolean justPass = false;

                if (!usernameText.getText().isEmpty()) {
                    String newUsername = usernameText.getText();
                    replaceInfo[0] = newUsername;
                    updateAccount = true;
                    String hashReturnedFromDatabase = data.getHashFromDatabase(oldArray[0]);
                    String returnedSalt = data.getSaltFromDatabase(oldArray[0]);
                    data.deleteUserFromDatabase(user.getUsername());
                    data.replacePassword(newUsername, hashReturnedFromDatabase, returnedSalt);
                }

                if (!emailText.getText().isEmpty()) {
                    String newEmail = emailText.getText();
                    replaceInfo[1] = newEmail;
                    updateAccount = true;
                }

                if (!bioText.getText().isEmpty()) {
                    String newBio = bioText.getText();
                    replaceInfo[2] = newBio;
                    updateAccount = true;
                }


                if (!String.valueOf(confirmPassText.getPassword()).isEmpty() && !String.valueOf(newPassText.getPassword()).isEmpty()) {
                    oldPass = String.valueOf(confirmPassText.getPassword());
                    newPass = String.valueOf(newPassText.getPassword());
                    justPass = true;
                }

                if (updateAccount && justPass) {
                    write.println("Both");
                    write.println(oldInfo);
                    String output = "";
                    output += replaceInfo[0] + "," + replaceInfo[1] + "," + replaceInfo[2];
                    write.println(output);
                    write.println(oldPass);
                    write.println(newPass);
                    write.println(user.getUsername());
                    write.flush();

                    user = new User(output);
                } else if (updateAccount) {
                    write.println("Account, no Password");
                    write.println(oldInfo);
                    String output = "";
                    output += replaceInfo[0] + "," + replaceInfo[1] + "," + replaceInfo[2];
                    write.println(output);
                    write.flush();

                    user = new User(output);
                } else if (justPass) {
                    write.println("Just Password");
                    write.println(oldPass);
                    write.println(newPass);
                    write.println(user.getUsername());
                    write.flush();
                }
            }
        });

        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePage();
            }
        });

        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

    public void exit() {
        write.println("Logging Out");
        write.flush();
        frame.dispose();
    }
}
