package miraj.biid.com.pani_200;

/**
 * Created by Miraj on 19/6/2017.
 */

public class User {
    private static String userId;
    private static String name;
    private static String address;
    private static String number;
    private static String nationalNumber;
    private static String pumpType;
    private static String pumpCapacity;
    private static String position;

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        User.userId = userId;
    }

    public static String getName() {
        return name;
    }

    public static String getAddress() {
        return address;
    }

    public static String getNationalNumber() {
        return nationalNumber;
    }

    public static String getPumpType() {
        return pumpType;
    }

    public static String getPumpCapacity() {
        return pumpCapacity;
    }

    public static void setName(String name) {
        User.name = name;
    }

    public static void setAddress(String address) {
        User.address = address;
    }

    public static void setNationalNumber(String nationalNumber) {
        User.nationalNumber = nationalNumber;
    }

    public static void setPumpType(String pumpType) {
        User.pumpType = pumpType;
    }

    public static void setPumpCapacity(String pumpCapacity) {
        User.pumpCapacity = pumpCapacity;
    }

    public static String getNumber() {
        return number;
    }

    public static void setNumber(String number) {
        User.number = number;
    }

    public static String getPosition() {
        return position;
    }

    public static void setPosition(String position) {
        User.position = position;
    }
}

