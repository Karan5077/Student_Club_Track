package studentclubtrackerapp;

import java.sql.*;
import java.util.Scanner;

public class StudentClubTracker {

    static final String URL = "jdbc:mysql://localhost:3306/student_club_tracker";
    static final String USER = "root";  
    static final String PASS = "12345"; 

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Welcome to Student Club Tracker");
            System.out.println("Login as:\n1. Student\n2. Staff");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(sc.nextLine());
            if (choice == 1) {
                studentLogin(conn);
            } else if (choice == 2) {
                staffLogin(conn);
            } else {
                System.out.println("Invalid choice");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Student login with reg_no
    static void studentLogin(Connection conn) throws SQLException {
        System.out.print("Enter Register Number: ");
        String regNo = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE reg_no = ?");
        ps.setString(1, regNo);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("Welcome, " + rs.getString("name"));
            studentMenu(conn, regNo);
        } else {
            System.out.println("Student not found!");
        }
    }

    static void studentMenu(Connection conn, String regNo) throws SQLException {
        while (true) {
            System.out.println("\nStudent Menu");
            System.out.println("1. View Clubs");
            System.out.println("2. Register to Club");
            System.out.println("3. View My Clubs' Members and Achievements");
            System.out.println("4. View Club News");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");
            int ch = Integer.parseInt(sc.nextLine());

            switch (ch) {
                case 1:
                    viewClubs(conn);
                    break;
                case 2:
                    registerToClub(conn, regNo);
                    break;
                case 3:
                    viewMembersAchievements(conn, regNo);
                    break;
                case 4:
                    viewClubNews(conn, regNo);
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void viewClubs(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM clubs");
        System.out.println("\nClubs:");
        while (rs.next()) {
            System.out.println(rs.getInt("club_id") + ". " + rs.getString("club_name") + " - " + rs.getString("description"));
        }
    }

    static void registerToClub(Connection conn, String regNo) throws SQLException {
        viewClubs(conn);
        System.out.print("Enter Club ID to register: ");
        int clubId = Integer.parseInt(sc.nextLine());

        PreparedStatement check = conn.prepareStatement("SELECT * FROM memberships WHERE reg_no = ? AND club_id = ?");
        check.setString(1, regNo);
        check.setInt(2, clubId);
        ResultSet rs = check.executeQuery();

        if (rs.next()) {
            System.out.println("You are already registered in this club.");
            return;
        }

        PreparedStatement ps = conn.prepareStatement("INSERT INTO memberships (reg_no, club_id) VALUES (?, ?)");
        ps.setString(1, regNo);
        ps.setInt(2, clubId);
        ps.executeUpdate();
        System.out.println("Registered to club successfully!");
    }

    static void viewMembersAchievements(Connection conn, String regNo) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT c.club_id, c.club_name FROM clubs c JOIN memberships m ON c.club_id = m.club_id WHERE m.reg_no = ?");
        ps.setString(1, regNo);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int clubId = rs.getInt("club_id");
            String clubName = rs.getString("club_name");
            System.out.println("\nClub: " + clubName);

            PreparedStatement membersPs = conn.prepareStatement(
                    "SELECT s.reg_no, s.name FROM students s JOIN memberships m ON s.reg_no = m.reg_no WHERE m.club_id = ?");
            membersPs.setInt(1, clubId);
            ResultSet membersRs = membersPs.executeQuery();

            System.out.println("Members:");
            while (membersRs.next()) {
                String memberReg = membersRs.getString("reg_no");
                String memberName = membersRs.getString("name");
                System.out.println("  " + memberReg + " - " + memberName);

                PreparedStatement achPs = conn.prepareStatement(
                        "SELECT achievement_desc, date FROM achievements WHERE reg_no = ? AND club_id = ?");
                achPs.setString(1, memberReg);
                achPs.setInt(2, clubId);
                ResultSet achRs = achPs.executeQuery();

                while (achRs.next()) {
                    System.out.println("    * " + achRs.getString("achievement_desc") + " (" + achRs.getDate("date") + ")");
                }
            }
        }
    }

    static void viewClubNews(Connection conn, String regNo) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT c.club_id, c.club_name FROM clubs c JOIN memberships m ON c.club_id = m.club_id WHERE m.reg_no = ?");
        ps.setString(1, regNo);
        ResultSet rs = ps.executeQuery();

        System.out.println("\nClub News:");
        while (rs.next()) {
            int clubId = rs.getInt("club_id");
            String clubName = rs.getString("club_name");
            System.out.println("\nNews for " + clubName + ":");

            PreparedStatement newsPs = conn.prepareStatement(
                    "SELECT news, news_date FROM club_news WHERE club_id = ?");
            newsPs.setInt(1, clubId);
            ResultSet newsRs = newsPs.executeQuery();

            while (newsRs.next()) {
                System.out.println(" - " + newsRs.getString("news") + " (" + newsRs.getDate("news_date") + ")");
            }
        }
    }

    // Staff login
    static void staffLogin(Connection conn) throws SQLException {
        System.out.print("Enter Staff ID: ");
        String staffId = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement("SELECT * FROM staff WHERE staff_id = ? AND password = ?");
        ps.setString(1, staffId);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("Welcome Staff: " + rs.getString("name"));
            staffMenu(conn);
        } else {
            System.out.println("Invalid Staff credentials!");
        }
    }

    // Staff menu
    static void staffMenu(Connection conn) throws SQLException {
        while (true) {
            System.out.println("\nStaff Menu");
            System.out.println("1. Add Club");
            System.out.println("2. Edit Club");
            System.out.println("3. Delete Club");
            System.out.println("4. View All Clubs");
            System.out.println("5. Add Achievement");
            System.out.println("6. Add Club News");
            System.out.println("7. Add New Staff");
            System.out.println("8. Add Member to Club");
            System.out.println("9. Add New Student");
            System.out.println("10. Logout");
            System.out.print("Enter your choice: ");
            int ch = Integer.parseInt(sc.nextLine());

            switch (ch) {
                case 1:
                    addClub(conn);
                    break;
                case 2:
                    editClub(conn);
                    break;
                case 3:
                    deleteClub(conn);
                    break;
                case 4:
                    viewClubs(conn);
                    break;
                case 5:
                    addAchievement(conn);
                    break;
                case 6:
                    addClubNews(conn);
                    break;
                case 7:
                    addNewStaff(conn);
                    break;
                case 8:
                    addMemberToClub(conn);
                    break;
                case 9:
                    addNewStudent(conn);
                    break;
                case 10:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void addClub(Connection conn) throws SQLException {
        System.out.print("Enter Club Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Description: ");
        String desc = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO clubs (club_name, description) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, desc);
        int affectedRows = ps.executeUpdate();

        if (affectedRows > 0) {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newClubId = rs.getInt(1);
                System.out.println("Club added successfully with ID: " + newClubId);
                System.out.println("Full Club Details:");
                System.out.println("ID: " + newClubId);
                System.out.println("Name: " + name);
                System.out.println("Description: " + desc);
            }
        } else {
            System.out.println("Failed to add club.");
        }
    }

    static void editClub(Connection conn) throws SQLException {
        viewClubs(conn);
        System.out.print("Enter Club ID to edit: ");
        int clubId = Integer.parseInt(sc.nextLine());

        System.out.print("Enter New Club Name: ");
        String name = sc.nextLine();
        System.out.print("Enter New Description: ");
        String desc = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement("UPDATE clubs SET club_name = ?, description = ? WHERE club_id = ?");
        ps.setString(1, name);
        ps.setString(2, desc);
        ps.setInt(3, clubId);
        ps.executeUpdate();
        System.out.println("Club updated successfully!");
    }

    static void deleteClub(Connection conn) throws SQLException {
        viewClubs(conn);
        System.out.print("Enter Club ID to delete: ");
        int clubId = Integer.parseInt(sc.nextLine());

        PreparedStatement ps = conn.prepareStatement("DELETE FROM clubs WHERE club_id = ?");
        ps.setInt(1, clubId);
        ps.executeUpdate();
        System.out.println("Club deleted successfully!");
    }

    static void addAchievement(Connection conn) throws SQLException {
        System.out.print("Enter Student Register Number: ");
        String regNo = sc.nextLine();

        // Check if student exists
        PreparedStatement checkStudent = conn.prepareStatement("SELECT * FROM students WHERE reg_no = ?");
        checkStudent.setString(1, regNo);
        ResultSet studentRs = checkStudent.executeQuery();
        if (!studentRs.next()) {
            System.out.println("Error: Student with Register Number " + regNo + " does not exist.");
            return;
        }

        System.out.print("Enter Club ID: ");
        int clubId = Integer.parseInt(sc.nextLine());

        System.out.print("Enter Achievement Description: ");
        String desc = sc.nextLine();

        java.sql.Date sqlDate = null;
        while (true) {
            System.out.print("Enter Date (YYYY-MM-DD): ");
            String dateStr = sc.nextLine();
            try {
                sqlDate = java.sql.Date.valueOf(dateStr);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO achievements (reg_no, club_id, achievement_desc, date) VALUES (?, ?, ?, ?)");
        ps.setString(1, regNo);
        ps.setInt(2, clubId);
        ps.setString(3, desc);
        ps.setDate(4, sqlDate);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Achievement added successfully!");
        } else {
            System.out.println("Failed to add achievement.");
        }
    }

    static void addClubNews(Connection conn) throws SQLException {
        System.out.print("Enter Club ID: ");
        int clubId = Integer.parseInt(sc.nextLine());
        System.out.print("Enter News Text: ");
        String news = sc.nextLine();

        java.sql.Date sqlDate = null;
        while (true) {
            System.out.print("Enter News Date (YYYY-MM-DD): ");
            String dateStr = sc.nextLine();
            try {
                sqlDate = java.sql.Date.valueOf(dateStr);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        PreparedStatement ps = conn.prepareStatement("INSERT INTO club_news (club_id, news, news_date) VALUES (?, ?, ?)");
        ps.setInt(1, clubId);
        ps.setString(2, news);
        ps.setDate(3, sqlDate);
        ps.executeUpdate();
        System.out.println("News added!");
    }

    static void addNewStaff(Connection conn) throws SQLException {
        System.out.print("Enter New Staff ID: ");
        String staffId = sc.nextLine();

        PreparedStatement checkStaff = conn.prepareStatement("SELECT * FROM staff WHERE staff_id = ?");
        checkStaff.setString(1, staffId);
        ResultSet rs = checkStaff.executeQuery();
        if (rs.next()) {
            System.out.println("Staff with this ID already exists.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement("INSERT INTO staff (staff_id, name, password) VALUES (?, ?, ?)");
        ps.setString(1, staffId);
        ps.setString(2, name);
        ps.setString(3, password);
        ps.executeUpdate();
        System.out.println("New staff added successfully!");
    }

    static void addMemberToClub(Connection conn) throws SQLException {
        System.out.print("Enter Student Register Number: ");
        String regNo = sc.nextLine();

        PreparedStatement checkStudent = conn.prepareStatement("SELECT * FROM students WHERE reg_no = ?");
        checkStudent.setString(1, regNo);
        ResultSet rsStudent = checkStudent.executeQuery();
        if (!rsStudent.next()) {
            System.out.println("Student not found.");
            return;
        }

        System.out.print("Enter Club ID: ");
        int clubId = Integer.parseInt(sc.nextLine());
        PreparedStatement checkClub = conn.prepareStatement("SELECT * FROM clubs WHERE club_id = ?");
        checkClub.setInt(1, clubId);
        ResultSet rsClub = checkClub.executeQuery();
        if (!rsClub.next()) {
            System.out.println("Club not found.");
            return;
        }

        PreparedStatement checkMembership = conn.prepareStatement("SELECT * FROM memberships WHERE reg_no = ? AND club_id = ?");
        checkMembership.setString(1, regNo);
        checkMembership.setInt(2, clubId);
        ResultSet rsMembership = checkMembership.executeQuery();
        if (rsMembership.next()) {
            System.out.println("Student is already a member of this club.");
            return;
        }

        PreparedStatement ps = conn.prepareStatement("INSERT INTO memberships (reg_no, club_id) VALUES (?, ?)");
        ps.setString(1, regNo);
        ps.setInt(2, clubId);
        ps.executeUpdate();
        System.out.println("Member added to club successfully!");
    }

    static void addNewStudent(Connection conn) throws SQLException {
        System.out.print("Enter New Student Register Number: ");
        String regNo = sc.nextLine();

        PreparedStatement checkStudent = conn.prepareStatement("SELECT * FROM students WHERE reg_no = ?");
        checkStudent.setString(1, regNo);
        ResultSet rs = checkStudent.executeQuery();
        if (rs.next()) {
            System.out.println("Student with this Register Number already exists.");
            return;
        }

        System.out.print("Enter Student Name: ");
        String name = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement("INSERT INTO students (reg_no, name) VALUES (?, ?)");
        ps.setString(1, regNo);
        ps.setString(2, name);
        ps.executeUpdate();

        System.out.println("New student added successfully!");
    }
}
