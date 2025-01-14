public class test {
    public static void main(String[] args) {
        Database b = new Database();

        String userOne = "Danny,Danny@yahoo.com,Bio,,six,yes,what";
        String userChange = "change,change@yahoo.com,Bio,,six,yes,what";
        String friendOne = "Friend,friend@yahoo.com,Bio,,six,yes,what";
        String change = "Danny,Danny@yahoo.com,Bio,,six,yes,what";
        String userTwo = "AnotherFriend,another@yahoo.com,Bio,,six,yes,what";


        b.createAccount(userOne, "Password");
        b.createAccount(userChange, "SecondPassword");
        b.createAccount(friendOne, "Third Password");
        b.createAccount(userTwo, "Fourth Password");
    }
}
