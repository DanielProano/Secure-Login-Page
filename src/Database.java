import com.mysql.cj.protocol.Resultset;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Database {
    private static final String ERROR = "Information Not Found";

    //Creates a string in the text file
    //"Username, Email, Bio, Friends, Blocked, photo, notifications,"
    public boolean createAccount(String info, String pass) {
        File file = new File("Storage.txt");
        String[] userInfo = info.split(",");
        try (PrintWriter r = new PrintWriter(new FileWriter(file, true))) {
            if (!checkNewAccount(info)) {
                return false;
            }
            String salt = randomSalt(5);
            insertPassword(userInfo[0], pass, salt);
            r.println(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public synchronized boolean updateAccount(String oldInfo, String newInfo) {
        System.out.println(oldInfo + "!" + newInfo);

        File temp = new File("Temp.txt");
        File storage = new File("Storage.txt");
        String[] oldInfoArray = oldInfo.split(",");
        System.out.println(oldInfo);
        String[] newInfoArray = newInfo.split(",");
        System.out.println(newInfo);
        ArrayList<String> copyStorageContents = new ArrayList<>();
        try (BufferedReader read = new BufferedReader(new FileReader(storage));
             PrintWriter p = new PrintWriter(new FileWriter(temp))) {
            String position = read.readLine();
            if (!oldInfoArray[0].equals(newInfoArray[0])) {
                String salt = getSaltFromDatabase(oldInfoArray[0]);
                String hash = getHashFromDatabase(oldInfoArray[0]);

                deleteUserFromDatabase(oldInfoArray[0]);
                insertPassword(newInfoArray[0], hash, salt);
            }

            while (position != null) {
                String[] positionArray = position.split(",");
                if (oldInfoArray[0].equals(positionArray[0])) {
                    position = read.readLine();
                    continue;
                }
                copyStorageContents.add(position);
                position = read.readLine();
            }
            copyStorageContents.add(newInfo);

            if (!temp.exists()) {
                try {
                    temp.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (String s : copyStorageContents) {
                p.println(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (storage.delete() && temp.renameTo(storage)) {
            return true;
        }

        return false;
    }

    public boolean checkNewAccount(String info) {
        String[] infoArray = info.split(",");

        if (!infoArray[1].contains("@")) {
            return false;
        } else if (returnInfo(infoArray[0], 0).equals(infoArray[0])) {
            return false;
        } else if (returnInfo(infoArray[0], 1).equals(infoArray[1])) {
            return false;
        } else if (infoArray.length != 3) {
            return false;
        }

        for (String c : infoArray) {
            if (c.equals(ERROR)) {
                return false;
            }
        }

        return true;
    }

    public String returnInfo(String username, int column) {
        try (BufferedReader read = new BufferedReader(new FileReader("Storage.txt"))) {
            String line = read.readLine();
            while (line != null) {
                String[] information = line.split(",");
                if (information[0].equals(username)) {
                    return information[column];
                }
                line = read.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return " ";
    }

    public String searchUsers(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("Storage.txt"))) {
            String line;
            String[] contain;
            while ((line = br.readLine()) != null) {
                contain = line.split(",");
                if (contain[0].contains(username)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ERROR;
    }

    public void insertPassword(String user, String pass, String salt) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "fh#j*E*W3*3.14";

        // SQL queries
        String checkQuery = "SELECT COUNT(*) FROM passwords WHERE username = ?";
        String insertQuery = "INSERT INTO passwords (username, password, salt) VALUES (?, ?, ?)";

        // Connection and PreparedStatement
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // Check if the user exists
            checkStmt.setString(1, user);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int userCount = rs.getInt(1);

            if (userCount > 0) {
                System.out.println("User already exists in the database.");
                return;
            }

            String out = pass + salt;

            String hashedPass = hashPassword(out);
            insertStmt.setString(1, user);
            insertStmt.setString(2, hashedPass);
            insertStmt.setString(3, salt);
            int rowsAffected = insertStmt.executeUpdate();
            System.out.println("User added successfully. Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized boolean deleteUserFromDatabase(String user) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "fh#j*E*W3*3.14";

        String insertQuery = "DELETE FROM passwords WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(url, username, password);

            stmt = conn.prepareStatement(insertQuery);

            stmt.setString(1, user);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public synchronized boolean loggingIn(String user, String pass) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "fh#j*E*W3*3.14";

        String checkQuery = "SELECT COUNT(*) FROM passwords WHERE username = ?";
        String checkPass = "SELECT password FROM passwords WHERE username = ?";
        String getSalt = "SELECT salt FROM passwords WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement checkPasswordStmt = conn.prepareStatement(checkPass);
             PreparedStatement getSaltNow = conn.prepareStatement(getSalt)) {

            checkStmt.setString(1, user);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int userCount = rs.getInt(1);

            getSaltNow.setString(1, user);
            ResultSet salt = getSaltNow.executeQuery();
            salt.next();
            String finalSalt = salt.getString(1);
            String hashedPass = hashPassword(pass + finalSalt);

            if (userCount == 1) {

                checkPasswordStmt.setString(1, user);
                ResultSet rsp = checkPasswordStmt.executeQuery();
                rsp.next();
                String passCount = rsp.getString(1);
                if (passCount.equals(hashedPass)) {
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error, hashing did not work", e);
        }
    }

    public String returnUserInfo(String username) {
        try (BufferedReader read = new BufferedReader(new FileReader("Storage.txt"))) {
            String line;
            while ((line = read.readLine()) != null) {
                String[] list = line.split(",");
                if (list[0].equals(username)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ERROR;
    }

    public synchronized String getHashFromDatabase(String user) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "fh#j*E*W3*3.14";

        String insertQuery = "SELECT password FROM passwords WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);

            stmt = conn.prepareStatement(insertQuery);

            stmt.setString(1, user);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("password");
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) { rs.close(); }
                if (stmt != null) { stmt.close(); }
                if (conn != null) { conn.close(); }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized String getSaltFromDatabase(String user) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "fh#j*E*W3*3.14";

        String insertQuery = "SELECT salt FROM passwords WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, username, password);

            stmt = conn.prepareStatement(insertQuery);

            stmt.setString(1, user);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("salt");
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String randomSalt(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        String out = "";

        for (int i = 0; i < length; i++) {
            int position = random.nextInt(characters.length());
            out += characters.charAt(position);
        }

        return out;
    }

    public void replacePassword(String user, String pass, String salt) {
        String url = "jdbc:mysql://localhost:3306/mysql";
        String username = "root";
        String password = "fh#j*E*W3*3.14";

        // SQL queries
        String checkQuery = "SELECT COUNT(*) FROM passwords WHERE username = ?";
        String insertQuery = "INSERT INTO passwords (username, password, salt) VALUES (?, ?, ?)";

        // Connection and PreparedStatement
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // Check if the user exists
            checkStmt.setString(1, user);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int userCount = rs.getInt(1);

            if (userCount > 0) {
                System.out.println("User already exists in the database.");
                return;
            }

            insertStmt.setString(1, user);
            insertStmt.setString(2, pass);
            insertStmt.setString(3, salt);
            int rowsAffected = insertStmt.executeUpdate();
            System.out.println("User added successfully. Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
