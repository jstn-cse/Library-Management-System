
package lms;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.table.TableRowSorter;
import java.sql.SQLException;

public class Admin_UI extends javax.swing.JFrame {
    

    DefaultTableModel model;
    public Admin_UI() {
        initComponents();
        
          setUserDetailsToTable();
          insertDataToIssueStudentTable();
          setTime();

         int delay = 500; // Refresh interval in milliseconds (e.g., 1000 ms = 1 second)
         Timer refreshTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to refresh the table
                insertIssuedBooksListTable();
                insertDataToIssueBookTable();
                setBookDetailsToTable();
                issuedBookList();
                updateOverdueStatus();
                updateDefaulterList(); // Update defaulter list after overdue status is updated
                loadDefaulterListIntoTable();
                fetchReturnedBooks();
                dashboardStudentDetails();
                dashboardBookDetails();
                updateAvailableBooksCount();
                dashboardPendingBooksCount();
                 fetchReports();
             }
         });
          refreshTimer.start();
        
        UserManager_Table.getColumnModel().getColumn(0).setPreferredWidth(110);
        UserManager_Table.getColumnModel().getColumn(1).setPreferredWidth(200);
        UserManager_Table.getColumnModel().getColumn(4).setPreferredWidth(250);
        UserManager_Table.getColumnModel().getColumn(5).setPreferredWidth(120);
        UserManager_Table.getColumnModel().getColumn(6).setPreferredWidth(270);
        Dashboard_BookDetails.getColumnModel().getColumn(0).setPreferredWidth(5);
        Dashboard_BookDetails.getColumnModel().getColumn(1).setPreferredWidth(200);
        Dashboard_BookDetails.getColumnModel().getColumn(2).setPreferredWidth(5);
        Dashboard_BookDetails.getColumnModel().getColumn(3).setPreferredWidth(25);
        Dashboard_StudentDetails.getColumnModel().getColumn(0).setPreferredWidth(25);
        Dashboard_StudentDetails.getColumnModel().getColumn(1).setPreferredWidth(100);
        Dashboard_StudentDetails.getColumnModel().getColumn(2).setPreferredWidth(200);
        Dashboard_StudentDetails.getColumnModel().getColumn(3).setPreferredWidth(10);
        BookDetails_Table.getColumnModel().getColumn(0).setPreferredWidth(5);
        BookDetails_Table.getColumnModel().getColumn(1).setPreferredWidth(200);
        BookDetails_Table.getColumnModel().getColumn(3).setPreferredWidth(25);
        BookDetails_Table.getColumnModel().getColumn(4).setPreferredWidth(5);
        BookDetails_Table.getColumnModel().getColumn(5).setPreferredWidth(25);
        Reports_Table.getColumnModel().getColumn(0).setPreferredWidth(5);
        Reports_Table.getColumnModel().getColumn(1).setPreferredWidth(25);
        Reports_Table.getColumnModel().getColumn(2).setPreferredWidth(25);
        Reports_Table.getColumnModel().getColumn(3).setPreferredWidth(250);
        
        Settings_Panel.setVisible(false);
        ManageUser_Panel.setVisible(false);
        ManageBooks_Panel.setVisible(false);
        IssueBooks_Panel.setVisible(false);
        Defaulter_Panel.setVisible(false);
        Records_Panel.setVisible(false);
        ReturnedBooks_Panel.setVisible(false);
        BookSelection_Panel.setVisible(false);
        BookList_Panel.setVisible(false);
        Reports_Panel.setVisible(false);
        IssueBooksTab.setVisible(false);
        
        tf_studentID.setEditable(false);
        tf_studentName.setEditable(false);
        tf_studentSection.setEditable(false);
        tf_studentStrand.setEditable(false);
        tf_studentEmail.setEditable(false);
        tf_studentMobile.setEditable(false);
        tf_studentAddress.setEditable(false);

    }
// ------------------------------------------------------ Start of Home Dashboard Code ------------------------------------------------------//

// Insert Student Details in DASHBOARD
    public  void dashboardStudentDetails() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM users");
            DefaultTableModel dashboardStudentDetails = (DefaultTableModel) Dashboard_StudentDetails.getModel();
            dashboardStudentDetails.setRowCount(0);
            int availableBookCount = 0;

            while (rs.next()) {
                String studentID = rs.getString("student_no");
                String studentName = rs.getString("student_name");
                String studentEmail = rs.getString("email");
                String studentStrand = rs.getString("strand");
                String userRole = rs.getString("role");
                if (!"admin".equalsIgnoreCase(userRole) && (!"librarian".equalsIgnoreCase(userRole))) {
                    Object[] userManagerRow = {studentID, studentName, studentEmail, studentStrand};
                    dashboardStudentDetails.addRow(userManagerRow);
                    availableBookCount++;
                }
            }
            Dashboard_StudentCount.setText(String.valueOf(availableBookCount));
            rs.close();
            st.close();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }
    
// Insert Book Details in DASHBOARD
    public  void dashboardBookDetails() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM book_details");
            DefaultTableModel dashboardBookDetails = (DefaultTableModel) Dashboard_BookDetails.getModel();
            dashboardBookDetails.setRowCount(0);
            int bookCount = 0;

            while (rs.next()) {
                String bookID = rs.getString("id");
                String bookTitle = rs.getString("book_title");
                String bookQuantity = rs.getString("quantity");
                String bookStatus = rs.getString("status");
                
                Object[] userManagerRow = {bookID, bookTitle, bookQuantity, bookStatus};
                dashboardBookDetails.addRow(userManagerRow);
                bookCount++;
            }
            Dashboard_BooksCount.setText(String.valueOf(bookCount));
            rs.close();
            st.close();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }
    
    // Insert Available Books in DASHBOARD
        public void updateAvailableBooksCount() {
            try {
                // Establish connection
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

                // Create statement
                Statement stmt = con.createStatement();

                // Execute query to count available books
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS availableCount FROM book_details WHERE status = 'Available'");

                // Check if result set has data
                if (rs.next()) {
                    // Get available book count from result set
                    int availableCount = rs.getInt("availableCount");

                    // Update JLabel with available book count
                    Dashboard_AvailableBooks.setText(Integer.toString(availableCount));
                }

                // Close connections
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle SQLException
            }
        }
    
        // Insert Book Details in DASHBOARD
    private void dashboardPendingBooksCount() {
            try {
                // Establish connection
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

                // Create statement
                Statement stmt = con.createStatement();

                // Execute query to count available books
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS availableCount FROM issuedbooks WHERE status = 'Pending'");

                // Check if result set has data
                if (rs.next()) {
                    // Get available book count from result set
                    int availableCount = rs.getInt("availableCount");

                    // Update JLabel with available book count
                    DashBoard_PendingCount.setText(Integer.toString(availableCount));
                }

                // Close connections
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle SQLException
            }
    }
    
    
// ------------------------------------------------------ End of Home Dashboard Code ------------------------------------------------------//
    
// ------------------------------------------------------ Start of User Manager Code ------------------------------------------------------//
    
        // Automatically Insert User Details to UserManager Table
        public void setUserDetailsToTable() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms","root","");
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from users");

                // Clear existing rows from both tables
                DefaultTableModel userManagerModel = (DefaultTableModel) UserManager_Table.getModel();
                userManagerModel.setRowCount(0);
                DefaultTableModel studentListModel = (DefaultTableModel) StudentList_Table.getModel();
                studentListModel.setRowCount(0);

                while(rs.next()) {
                    String userId = rs.getString("student_no");
                    String email = rs.getString("email");
                    String studentName = rs.getString("student_name");
                    String studentSection = rs.getString("section");
                    String studentStrand = rs.getString("strand");
                    String mobileNo = rs.getString("mobile");
                    String address = rs.getString("address");
                    String userRole = rs.getString("role");

                    // Check if the role is not "admin" before adding to the tables
                    if (!"admin".equalsIgnoreCase(userRole) && !"librarian".equalsIgnoreCase(userRole)) {
                        Object[] userManagerRow = {userId, studentName, studentSection, studentStrand, email, mobileNo, address};
                        userManagerModel.addRow(userManagerRow);

                        Object[] studentListRow = {userId, studentName, studentSection, studentStrand, email, mobileNo, address};
                        studentListModel.addRow(studentListRow);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
       
        
    // Search function User Details Table
    public void searchUserDetails(String selectedCategory, String searchString, JTable UserManager_Table) {
        DefaultTableModel model = (DefaultTableModel) UserManager_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        UserManager_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "Student ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Student Name":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Section":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2)); 
               break;
            case "Strand":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 3));
                break;
            case "Email":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 4));
                break;
            case "Mobile No.":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 5));
                break;
            case "Address":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 6));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }

    //Clear TextFields in User Manager
    public void clearUserTextField() {
        
        DefaultTableModel model = (DefaultTableModel)  UserManager_Table.getModel();
        model.setRowCount(0);   
        txt_studentid.setText("");
        txt_studentname.setText("");
        txt_section.setText("");
        txt_strand.setText("");
        txt_email.setText("");
        txt_mobileNo.setText("");
        txt_address.setText("");
    }
    
// Update Method for User Manager Table
    public boolean updateUser() {
        boolean isUpdated = false;
        String studentId = txt_studentid.getText();
        String studentName = txt_studentname.getText();
        String section = txt_section.getText();
        String strand = txt_strand.getText();
        String email = txt_email.getText();
        String mobileNo = txt_mobileNo.getText();
        String address = txt_address.getText();

        try {
            Connection con = DBConnection.getConnection();
            String sql = "update users set student_name = ?, section = ?, strand = ?, email = ?, mobile = ?, address = ? where student_no = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, studentName);
            pst.setString(2, section);
            pst.setString(3, strand);
            pst.setString(4, email);
            pst.setString(5, mobileNo);
            pst.setString(6, address);
            pst.setString(7, studentId);

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                isUpdated = true;
            } else {
                isUpdated = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isUpdated) {
            JOptionPane.showMessageDialog(this, "Update failed. Please check the input values.");
        }
        return isUpdated;
    }
    
// Add Student Method for User Manager Table
            public boolean addStudent() {
            boolean isAdded = false;
            String studentId = txt_studentid.getText().trim();
            String studentName = txt_studentname.getText().trim();
            String section = txt_section.getText().trim();
            String strand = txt_strand.getText().trim();
            String email = txt_email.getText().trim();
            String mobileNo = txt_mobileNo.getText().trim();
            String address = txt_address.getText().trim();

            // Validate student ID length
            if (studentId.length() != 6 || !studentId.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Student ID must be exactly 6 digits.");
                return false;
            }

            if (studentId.isEmpty() || studentName.isEmpty() || section.isEmpty() || strand.isEmpty() ||
                email.isEmpty() || mobileNo.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled out.");
                return false;
            }

            Connection con = null;
            PreparedStatement pst = null;
            ResultSet rs = null;

            try {
                con = DBConnection.getConnection();

                // Check for duplicate student ID
                String checkSql = "SELECT COUNT(*) FROM users WHERE student_no = ?";
                pst = con.prepareStatement(checkSql);
                pst.setString(1, studentId);
                rs = pst.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists.");
                    return false;
                }

                // Close the previous PreparedStatement and ResultSet
                pst.close();
                rs.close();

                // Insert the new student record
                String insertSql = "INSERT INTO users (student_no, student_name, section, strand, email, mobile, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
                pst = con.prepareStatement(insertSql);
                pst.setString(1, studentId);
                pst.setString(2, studentName);
                pst.setString(3, section);
                pst.setString(4, strand);
                pst.setString(5, email);
                pst.setString(6, mobileNo);
                pst.setString(7, address);

                int rowCount = pst.executeUpdate();
                if (rowCount > 0) {
                    isAdded = true;
                    JOptionPane.showMessageDialog(this, "Student added successfully.");
                } else {
                    isAdded = false;
                    JOptionPane.showMessageDialog(this, "Failed to add student. Please check the input values.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while adding the student.");
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pst != null) pst.close();
                    if (con != null) con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return isAdded;
        }


    //Delete Method for User Manager
    public boolean deleteUser() {
        boolean isDeleted = false;
            int studentNo = Integer.parseInt(txt_studentid.getText());
            
            try {
                Connection con = DBConnection.getConnection();
                String sql = "delete from users  where student_no = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, studentNo);
                
                int rowCount = pst.executeUpdate();
                if (rowCount > 0 ) {
                    isDeleted = true;
                }else {
                    isDeleted = false;
                }
                
            } catch (Exception e){
                e.printStackTrace();
            }
            return isDeleted;
    }
    
    
// ------------------------------------------------------ End  of User Manager Code ------------------------------------------------------//

// ------------------------------------------------------ Start of Book Details Code ------------------------------------------------------//
        
    // --------------------------Search function BookDetails Table--------------------------//
    public void searchBookInTable(String selectedCategory, String searchString, JTable BookDetails_Table) {
        DefaultTableModel model = (DefaultTableModel) BookDetails_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        BookDetails_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "Book ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Book Title":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Author":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2)); 
               break;
            case "Category":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 4));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
    
    //--------------------------TRANSFER DATABASE INTO JTABLE--------------------------//
    public void setBookDetailsToTable() {
        DefaultTableModel model = (DefaultTableModel) BookDetails_Table.getModel();
        model.setRowCount(0); // Clear existing rows from the table

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms","root","");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM book_details");

            while(rs.next()) {
                String bookId = rs.getString("id");
                String bookTitle = rs.getString("book_title");
                String authorName = rs.getString("author");
                String section = rs.getString("section");
                int qty = rs.getInt("quantity");
                String bookStatus = qty > 0 ? "Available" : "Unavailable"; // Set status based on quantity

                Object[] obj = {bookId, bookTitle, authorName, section, qty, bookStatus};
                model.addRow(obj);
            }       
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
        //-------------------------- ADD BOOK FUNCTION--------------------------//
        public boolean addBook() {
            boolean isAdded = false;
            String bookTitle = txt_booktitle.getText();
            String author = txt_bookauthor.getText();
            String section = String.valueOf(txt_booksection.getSelectedItem());
            String qty = txt_quantity.getText();
            String bookStatus = "Unavailable"; // Default status

            // Check if quantity is a valid integer and not below 0
            try {
                int quantity = Integer.parseInt(qty);
                if (quantity > 0) {
                    bookStatus = "Available";
                }

                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid input");
                    return false; // Return false to indicate that the update failed
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input");
                return false; // Return false to indicate that the update failed
            }

            try {
                Connection con = DBConnection.getConnection();
                String sql = "INSERT INTO book_details(book_title, author, section, quantity, status) VALUES(?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                pst.setString(1, bookTitle);
                pst.setString(2, author);
                pst.setString(3, section);
                pst.setString(4, qty);
                pst.setString(5, bookStatus); // Set the book status

                int rowCount = pst.executeUpdate();
                if (rowCount > 0) {
                    isAdded = true;

                    // Retrieve the auto-generated ID
                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int generatedID = generatedKeys.getInt(1);
                        // Optionally set the generated ID somewhere if needed
                    }
                } else {
                    isAdded = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return isAdded;
        }

//-------------------------- UPDATE BOOK FUNCTION--------------------------//
    public boolean updateBook() {
        boolean isUpdated = false;
        String bookID = txt_bookID.getText();
        String bookTitle = txt_booktitle.getText();
        String bookAuthor = txt_bookauthor.getText();
        String bookSection = String.valueOf(txt_booksection.getSelectedItem());
        String qty = txt_quantity.getText();
        String bookStatus = "Unavailable"; // Default status

        try {
            int quantity = Integer.parseInt(qty);
            if (quantity > 0) {
                bookStatus = "Available";
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
            return false; // Return false to indicate that the update failed
        }

        try {
            Connection con = DBConnection.getConnection();
            String sql = "UPDATE book_details SET book_title = ?, author = ?, section = ?, quantity = ?, status = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, bookTitle);
            pst.setString(2, bookAuthor);
            pst.setString(3, bookSection);
            pst.setInt(4, Integer.parseInt(qty));
            pst.setString(5, bookStatus); // Set the book status
            pst.setInt(6, Integer.parseInt(bookID));

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                isUpdated = true;
            } else {
                isUpdated = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isUpdated) {
            JOptionPane.showMessageDialog(this, "Update failed. Please check the input values.");
        }
        return isUpdated;
    }

    
    // --------------------------Delete Method for Book Details--------------------------//
    public boolean deleteBook() {
        boolean isDeleted = false;

        // Check if a book ID is selected
        if (txt_bookID.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.");
            return false;
        }

        int bookID = Integer.parseInt(txt_bookID.getText());

        try {
            Connection con = DBConnection.getConnection();
            String sql = "DELETE FROM book_details WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, bookID);

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                isDeleted = true;
            } else {
                isDeleted = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isDeleted;
    }
    
// ------------------------------------------------------End of Book Details Code ------------------------------------------------------//
    
// ------------------------------------------------------Start of Issue Books Code ------------------------------------------------------//
            //Automatically Insert the Data into StudentList_Table
            public void insertDataToIssueStudentTable() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms","root","");
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from users");
                
                DefaultTableModel studentListModel = (DefaultTableModel) StudentList_Table.getModel();
                studentListModel.setRowCount(0);
                
                while(rs.next()) {
                    String userId = rs.getString("student_no");
                    String studentName = rs.getString("student_name");
                    String studentSection = rs.getString("section");
                    String studentStrand = rs.getString("strand");
                    String email = rs.getString("email");
                    String mobileNo = rs.getString("mobile");
                    String address = rs.getString("address");
                    String userRole = rs.getString("role");
                    
                    
                    // Check if the role is not "admin" before adding to the table
                    if (!"admin".equalsIgnoreCase(userRole) && (!"librarian".equalsIgnoreCase(userRole))) {
                        Object[] obj = {userId, studentName, studentSection, studentStrand, email, mobileNo, address};
                        model = (DefaultTableModel) StudentList_Table.getModel();
                        model.addRow(obj);
                        }
                    } 
                } catch (Exception e) {
                    e.printStackTrace(); // Handle exceptions appropriately
                }
              
            }
          
         //Automatically Insert Data into BookList_Table   
        public void insertDataToIssueBookTable() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms","root","");
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("select * from book_details");

                DefaultTableModel model = (DefaultTableModel) BookList_Table.getModel();
                model.setRowCount(0); // Clear existing rows from the table

                while(rs.next()) {
                    String bookId = rs.getString("id");
                    String bookTitle = rs.getString("book_title");
                    String bookAuthor = rs.getString("author");
                    String section = rs.getString("section");
                    int qty = rs.getInt("quantity");
                    String status = rs.getString("status");
                    // Add book details to the table model
                    Object[] obj = {bookId, bookTitle, bookAuthor, section, qty, status};
                    model.addRow(obj);
                }

                // Close resources
                rs.close();
                st.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
         
            //Insert the Information of the Issued Books to the Data Base
            public boolean insertIssuedDetails() {
                boolean isAdded = false;
                String studentName = tf_studentName.getText();
                String studentSection = tf_studentSection.getText();
                String studentStrand = tf_studentStrand.getText();
                String studentID = tf_studentID.getText();
                String studentEmail = tf_studentEmail.getText();
                String studentMobile = tf_studentMobile.getText();
                String studentAddress = tf_studentAddress.getText();
                Date uIssueDate = IssueDate.getDate();
                Date uDueDate = DueDate.getDate();

                // Check if the due date is earlier than the issue date
                if (uDueDate.before(uIssueDate)) {
                    JOptionPane.showMessageDialog(this, "Due date cannot be earlier than issue date.");
                    return false;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                String issueDate = sdf.format(uIssueDate);
                String dueDate = sdf.format(uDueDate);

                try {
                    Connection con = DBConnection.getConnection();
                    con.setAutoCommit(false); // Start a transaction

                    // Iterate over the rows of SelectedBook_Table
                    DefaultTableModel model = (DefaultTableModel) SelectedBook_Table.getModel();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        String bookTitle = model.getValueAt(i, 0).toString();
                        String bookID = model.getValueAt(i, 1).toString();
                        int requestedQuantity = Integer.parseInt(model.getValueAt(i, 2).toString());

                        // Check if the requested quantity is available
                        String sql = "SELECT quantity FROM book_details WHERE id = ?";
                        PreparedStatement pst = con.prepareStatement(sql);
                        pst.setString(1, bookID);
                        ResultSet rs = pst.executeQuery();

                        if (rs.next()) {
                            int availableQuantity = rs.getInt("quantity");
                            if (availableQuantity >= requestedQuantity) {
                                // Update the book quantity
                                String updateSql = "UPDATE book_details SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
                                PreparedStatement updatePst = con.prepareStatement(updateSql);
                                updatePst.setInt(1, requestedQuantity);
                                updatePst.setString(2, bookID);
                                updatePst.setInt(3, requestedQuantity);
                                updatePst.executeUpdate();

                                // Insert the issued book details with status
                                String insertSql = "INSERT INTO issuedbooks (student_name, student_section, student_strand, student_id, student_email," 
                                        +"student_mobile, student_address, issue_date, due_date, book_id, book_title, qty, status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                PreparedStatement insertPst = con.prepareStatement(insertSql);
                                insertPst.setString(1, studentName);
                                insertPst.setString(2, studentSection);
                                insertPst.setString(3, studentStrand);
                                insertPst.setString(4, studentID);
                                insertPst.setString(5, studentEmail);
                                insertPst.setString(6, studentMobile);
                                insertPst.setString(7, studentAddress);
                                insertPst.setString(8, issueDate);
                                insertPst.setString(9, dueDate);
                                insertPst.setString(10, bookID);
                                insertPst.setString(11, bookTitle);
                                insertPst.setInt(12, requestedQuantity);
                                insertPst.setString(13, "Pending");
                                insertPst.executeUpdate();
                            } else {
                                JOptionPane.showMessageDialog(this, "Insufficient quantity available for book: " + bookTitle);
                                con.rollback(); // Rollback the transaction
                                con.close();
                                return false;
                            }
                        }
                        rs.close();
                    }

                    con.commit(); // Commit the transaction
                    con.close();
                    isAdded = true;

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "An error occurred while inserting the issued book details.");
                    return false;
                }
                return isAdded;
            }

            // Update overdue statuses
            public void updateOverdueStatus() {
                Connection con = null;
                PreparedStatement pstSelect = null;
                PreparedStatement pstUpdate = null;
                ResultSet rs = null;

                try {
                    con = DBConnection.getConnection();

                    // Get the current date
                    Date currentDate = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    String currentDateString = sdf.format(currentDate);

                    // Select books that are overdue
                    String selectSql = "SELECT id, due_date FROM issuedbooks WHERE status = 'Pending'";
                    pstSelect = con.prepareStatement(selectSql);
                    rs = pstSelect.executeQuery();

                    // Prepare the update statement
                    String updateSql = "UPDATE issuedbooks SET status = 'overdue' WHERE id = ?";
                    pstUpdate = con.prepareStatement(updateSql);

                    // Iterate over the results and update the status
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String dueDateString = rs.getString("due_date");

                        // Compare dates
                        Date dueDate = sdf.parse(dueDateString);
                        if (currentDate.after(dueDate)) {
                            // Update the status to "over due"
                            pstUpdate.setString(1, id);
                            pstUpdate.executeUpdate();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (pstSelect != null) pstSelect.close();
                        if (pstUpdate != null) pstUpdate.close();
                        if (con != null) con.close();
                    } catch (Exception closeEx) {
                        closeEx.printStackTrace();
                    }
                }
            }
            
        //Collect the Data from DataBase and insert to Issued List JTable
         public void insertIssuedBooksListTable() {
                DefaultTableModel model = (DefaultTableModel) IssuedList_Table.getModel();
                model.setRowCount(0); // Clear existing rows from the table
        
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms","root","");
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("select * from issuedbooks");

                    while(rs.next()) {
                            String ID = rs.getString("id");
                            String studentName = rs.getString("student_name");
                            String studentSection = rs.getString("student_section");
                            String studentStrand = rs.getString("student_strand");
                            String studentID = rs.getString("student_id");
                            String studentEmail = rs.getString("student_email");
                            String studentMobile = rs.getString("student_mobile");
                            String studentAddress = rs.getString("student_address");
                            String issueDate = rs.getString("issue_date");
                            String dueDate = rs.getString("due_date");
                            String bookID = rs.getString("book_id");
                            String bookTitle = rs.getString("book_title");
                            String QTY = rs.getString("qty");

                            Object[] obj = {ID, studentName, studentSection, studentStrand, studentID, studentEmail, studentMobile, studentAddress, issueDate, dueDate, bookID, bookTitle, QTY};
                            model = (DefaultTableModel) IssuedList_Table.getModel();
                            model.addRow(obj);
                             }       
                        }catch (Exception e) {
                    }
    }
        
        // Search function Issue Books Table -- Student Table
    public void searchIssueBooksStudentDetails(String selectedCategory, String searchString, JTable StudentList_Table) {
        DefaultTableModel model = (DefaultTableModel) StudentList_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        StudentList_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "Student ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Student Name":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
    
        // Search function Issue Books Table -- Books Table
    public void searchIssueBooksDetails(String selectedCategory, String searchString, JTable BookList_Table) {
        DefaultTableModel model = (DefaultTableModel) BookList_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        BookList_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "Book ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Book Title":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Author":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2)); 
               break;
            case "Section":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 3));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
      
// Add report about Issue'ing Book
        private void addIssueBookReport() {
            // Get student details
            String studentID = tf_studentID.getText();
            String studentName = tf_studentName.getText();
            // Validate the input fields
            if (studentID.isEmpty() || studentName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill out student details.");
                return;
            }
            // Get book details from SelectedBook_Table
            DefaultTableModel selectedBookModel = (DefaultTableModel) SelectedBook_Table.getModel();
            int rowCount = selectedBookModel.getRowCount();
            if (rowCount == 0) {
                JOptionPane.showMessageDialog(this, "No book selected.");
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                for (int i = 0; i < rowCount; i++) {
                    String bookTitle = selectedBookModel.getValueAt(i, 0).toString();
                    int quantity = Integer.parseInt(selectedBookModel.getValueAt(i, 2).toString());

                    // Insert report
                    String reportQuery = "INSERT INTO reports (date, student_name, student_id, action) VALUES (CURRENT_DATE(), ?, ?, ?)";
                    String action = studentName + "has borrowed " + quantity + "x " + bookTitle;
                    try (PreparedStatement pst = con.prepareStatement(reportQuery)) {
                        pst.setString(1, studentName);
                        pst.setString(2, studentID);
                        pst.setString(3, action);
                        pst.executeUpdate();
                    }
                }
                // Clear the selected books table after issuing
                selectedBookModel.setRowCount(0);
                fetchReports();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        

        
        
// ------------------------------------------------------End of Issue Books Code ------------------------------------------------------//
// ------------------------------------------------------Start of Records Code ------------------------------------------------------//


        //Collect the Data from DataBase and insert to Issued List JTable
         public void issuedBookList() {
                DefaultTableModel model = (DefaultTableModel) IssuedList_Table2.getModel();
                model.setRowCount(0); // Clear existing rows from the table
        
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms","root","");
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("select * from issuedbooks");
                    int bookCount = 0;
                    while(rs.next()) {
                            String ID = rs.getString("id");
                            String studentName = rs.getString("student_name");
                            String studentID = rs.getString("student_id");
                            String issueDate = rs.getString("issue_date");
                            String dueDate = rs.getString("due_date");
                            String bookID = rs.getString("book_id");
                            String bookTitle = rs.getString("book_title");
                            String QTY = rs.getString("qty");
                            String Status = rs.getString("status");

                            Object[] obj = {ID, studentName, studentID, issueDate, dueDate, bookID, bookTitle, QTY, Status};
                            model = (DefaultTableModel) IssuedList_Table2.getModel();
                            model.addRow(obj); 
                             }       
                        }catch (Exception e) {
                    }
    }
         
    // Search function Issued Books Table
    public void searchIssuedBooksTable(String selectedCategory, String searchString, JTable IssuedList_Table2) {
        DefaultTableModel model = (DefaultTableModel) IssuedList_Table2.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        IssuedList_Table2.setRowSorter(trs);

        switch (selectedCategory) {
            case "ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Name":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Student ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2));
                break;
            case "Issue Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString,3));
                break;
            case "Due Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 4));
                break;
            case "Book ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString,5));
                break;
            case "Book Title":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 6));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
    
    //Get the data of the borrower by typing the BORROWED ID
    public void getBorrowerDetails() {
        String borrowerIDStr = tf_studentid.getText().trim();

        // Check if the txt_id field is empty
        if (borrowerIDStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter Student ID.");
            return; // Exit the method if txt_id is empty
        }

        try {
            int borrowerID = Integer.parseInt(borrowerIDStr);

            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT * FROM issuedbooks WHERE student_id = ?");
            pst.setInt(1, borrowerID);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                tf_id.setText(rs.getString("id"));
                tf_name.setText(rs.getString("student_name"));
                tf_studentid.setText(rs.getString("student_id"));
                tf_issuedate.setText(rs.getString("issue_date"));
                tf_duedate.setText(rs.getString("due_date"));
                tf_bookid.setText(rs.getString("book_id"));
                tf_booktitle.setText(rs.getString("book_title"));
                tf_qty.setText(rs.getString("qty"));
                tf_status.setText(rs.getString("status"));
            } else {
                JOptionPane.showMessageDialog(null, "Student ID Cannot be found.");
            }

            rs.close();
            pst.close();
            con.close();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid ID.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while retrieving borrower details.");
        }
    }
    
//Return book function. INSERT RETURNED BOOKS INTO RETURNEDBOOKS DATABASE
    public void returnBook() {
        String studentName = tf_name.getText();
        String studentId = tf_studentid.getText();
        String bookId = tf_bookid.getText();
        String bookTitle = tf_booktitle.getText();
        String id = tf_id.getText();
        int returnQuantity = Integer.parseInt(tf_qty.getText());
        String status = "returned";
        String returnDateStr = Date.getText();  // Assuming Date JLabel displays the current date

        Connection conn = null;
        PreparedStatement pst = null;

        try {
            // Establish the database connection
            conn = DBConnection.getConnection();

            // Retrieve the total quantity borrowed for this book
            String selectIssuedBookSQL = "SELECT qty FROM issuedbooks WHERE id = ?";
            pst = conn.prepareStatement(selectIssuedBookSQL);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int totalQuantityBorrowed = rs.getInt("qty");

                if (totalQuantityBorrowed <= 0) {
                    // If the total quantity borrowed is already 0, prompt an error
                    JOptionPane.showMessageDialog(null, "Error: Book quantity is already 0");
                    return; // Exit the method
                }

                // Ensure return quantity does not exceed total quantity borrowed
                if (returnQuantity > totalQuantityBorrowed) {
                    // If the return quantity is greater than what was borrowed, prompt an error
                    JOptionPane.showMessageDialog(null, "Error: Return quantity cannot exceed total quantity borrowed");
                    return; // Exit the method
                }

                if (returnQuantity <= 0) {
                    // If the return quantity is 0 or negative, prompt an error
                    JOptionPane.showMessageDialog(null, "Error: Please specify a positive quantity to return");
                    return; // Exit the method
                }

                // Insert into returnedbooks table
                String insertReturnedBookSQL = "INSERT INTO returnedbooks (student_name, student_id, returned_date, book_id, book_title, quantity, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                pst = conn.prepareStatement(insertReturnedBookSQL);
                pst.setString(1, studentName);
                pst.setString(2, studentId);
                pst.setString(3, returnDateStr); // Use formatted date string
                pst.setString(4, bookId);
                pst.setString(5, bookTitle);
                pst.setInt(6, returnQuantity); // Use return quantity
                pst.setString(7, status);
                pst.executeUpdate();

                // Update issuedbooks table
                String updateIssuedBookSQL = "UPDATE issuedbooks SET qty = qty - ? WHERE id = ?";
                pst = conn.prepareStatement(updateIssuedBookSQL);
                pst.setInt(1, returnQuantity); // Update quantity based on return quantity
                pst.setString(2, id);
                pst.executeUpdate();

                // Update book_details table
                String updateBookDetailsSQL = "UPDATE book_details SET quantity = quantity + ? WHERE id = ?";
                pst = conn.prepareStatement(updateBookDetailsSQL);
                pst.setInt(1, returnQuantity);
                pst.setString(2, bookId);
                pst.executeUpdate();

                // Check if quantity becomes 0 after returning
                if (totalQuantityBorrowed - returnQuantity <= 0) {
                    // If quantity becomes 0, delete the book from issuedbooks table
                    String deleteIssuedBookSQL = "DELETE FROM issuedbooks WHERE id = ?";
                    pst = conn.prepareStatement(deleteIssuedBookSQL);
                    pst.setString(1, id);
                    pst.executeUpdate();
                }

                // Display success message
                JOptionPane.showMessageDialog(null, "Book returned successfully");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while returning the book");
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
//Reports Return Books
        private void returnBookAndReport() {
            // Get student and book details from text fields
            String studentID = tf_studentid.getText();
            String studentName = tf_name.getText();
            String bookID = tf_bookid.getText();
            String bookTitle = tf_booktitle.getText();
            String issueDate = tf_issuedate.getText();
            String dueDate = tf_duedate.getText();
            int quantity = Integer.parseInt(tf_qty.getText());
            String status = tf_status.getText();

            // Validate the input fields
            if (studentID.isEmpty() || studentName.isEmpty() || bookID.isEmpty() || bookTitle.isEmpty() ||
                issueDate.isEmpty() || dueDate.isEmpty() || tf_qty.getText().isEmpty() || status.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill out all the fields.");
                return;
            }

            try {
                Connection con = DBConnection.getConnection();
                con.setAutoCommit(false); // Start transaction

                // Insert report
                String reportQuery = "INSERT INTO reports (date, student_name, student_id, action) VALUES (CURRENT_DATE(), ?, ?, ?)";
                String action = studentName + " has returned " + quantity + "x " + bookTitle;
                try (PreparedStatement pst = con.prepareStatement(reportQuery)) {
                    pst.setString(1, studentName);
                    pst.setString(2, studentID);
                    pst.setString(3, action);
                    pst.executeUpdate();
                }
                con.commit(); // Commit transaction
                // Clear the text fields after returning the book
                tf_id.setText("");
                tf_name.setText("");
                tf_studentid.setText("");
                tf_issuedate.setText("");
                tf_duedate.setText("");
                tf_bookid.setText("");
                tf_booktitle.setText("");
                tf_qty.setText("");
                tf_status.setText("");
                // Update the Reports_Table to reflect the new report
                fetchReports();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

// ------------------------------------------------------End of Records Code ------------------------------------------------------//
// ------------------------------------------------------Start of Defaulter List Code ------------------------------------------------------//
    
// Move students with OVER DUE status to defaulter list DATABASE    
        public void updateDefaulterList() {
            Connection con = null;
            PreparedStatement pstSelectOverdue = null;
            PreparedStatement pstCheckExists = null;
            PreparedStatement pstInsert = null;
            ResultSet rsOverdue = null;
            ResultSet rsCheckExists = null;

            try {
                con = DBConnection.getConnection();

                // Select overdue books from issuedbooks
                String selectOverdueSql = "SELECT * FROM issuedbooks WHERE status = 'overdue'";
                pstSelectOverdue = con.prepareStatement(selectOverdueSql);
                rsOverdue = pstSelectOverdue.executeQuery();

                // Prepare the insert statement for defaulterlist
                String insertSql = "INSERT INTO defaulterlist (student_name, student_section, student_strand, student_id, student_email, " +
                                   "student_mobile, student_address, issue_date, due_date, book_id, book_title, qty, status) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                pstInsert = con.prepareStatement(insertSql);

                // Prepare the select statement to check for existing entries in defaulterlist
                String checkExistsSql = "SELECT COUNT(*) FROM defaulterlist WHERE student_id = ? AND book_id = ? AND status = 'overdue'";
                pstCheckExists = con.prepareStatement(checkExistsSql);

                while (rsOverdue.next()) {
                    String studentId = rsOverdue.getString("student_id");
                    String bookId = rsOverdue.getString("book_id");

                    // Check if the record already exists in defaulterlist
                    pstCheckExists.setString(1, studentId);
                    pstCheckExists.setString(2, bookId);
                    rsCheckExists = pstCheckExists.executeQuery();

                    if (rsCheckExists.next() && rsCheckExists.getInt(1) == 0) {
                        // If the record does not exist, insert it into defaulterlist
                        pstInsert.setString(1, rsOverdue.getString("student_name"));
                        pstInsert.setString(2, rsOverdue.getString("student_section"));
                        pstInsert.setString(3, rsOverdue.getString("student_strand"));
                        pstInsert.setString(4, studentId);
                        pstInsert.setString(5, rsOverdue.getString("student_email"));
                        pstInsert.setString(6, rsOverdue.getString("student_mobile"));
                        pstInsert.setString(7, rsOverdue.getString("student_address"));
                        pstInsert.setString(8, rsOverdue.getString("issue_date"));
                        pstInsert.setString(9, rsOverdue.getString("due_date"));
                        pstInsert.setString(10, bookId);
                        pstInsert.setString(11, rsOverdue.getString("book_title"));
                        pstInsert.setInt(12, rsOverdue.getInt("qty"));
                        pstInsert.setString(13, rsOverdue.getString("status"));
                        pstInsert.addBatch();
                    }
                }

                // Execute the batch insert
                pstInsert.executeBatch();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rsOverdue != null) rsOverdue.close();
                    if (rsCheckExists != null) rsCheckExists.close();
                    if (pstSelectOverdue != null) pstSelectOverdue.close();
                    if (pstCheckExists != null) pstCheckExists.close();
                    if (pstInsert != null) pstInsert.close();
                    if (con != null) con.close();
                } catch (Exception closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
        
// Insert the DATABASE TO DEFAULTER LIST JTABLE
        public void loadDefaulterListIntoTable() {
            Connection con = null;
            PreparedStatement pstSelect = null;
            ResultSet rs = null;

            try {
                con = DBConnection.getConnection();

                // Select all records from defaulterlist
                String selectSql = "SELECT * FROM defaulterlist";
                pstSelect = con.prepareStatement(selectSql);
                rs = pstSelect.executeQuery();

                // Clear the table before loading new data
                DefaultTableModel model = (DefaultTableModel) DefaulterList_Table.getModel();
                model.setRowCount(0);
                

                // Load data into the table
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("id"),
                        rs.getString("student_name"),
                        rs.getString("student_id"),
                        rs.getString("issue_date"),
                        rs.getString("due_date"),
                        rs.getString("book_id"),
                        rs.getString("book_title"),
                        rs.getInt("qty"),
                        rs.getString("status")
                    };
                    model.addRow(row);
                    removeLiftedStatusFromTable();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pstSelect != null) pstSelect.close();
                    if (con != null) con.close();
                } catch (Exception closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
        
        // Get the data of the DEFAULTER by typing the ID
        public void getDefaulterDetails() {
            String idText = txt_studentid2.getText().trim();

            // Check if the ID field is empty
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter the Student ID first.");
                return; // Exit the method if the ID field is empty
            }

            try {
                int borrowerID = Integer.parseInt(idText); // Convert the ID to an integer

                Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement("Select * from defaulterlist where student_id = ?");
                pst.setInt(1, borrowerID);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    txt_id.setText(rs.getString("id"));
                    txt_fullname.setText(rs.getString("student_name"));
                    txt_studentid.setText(rs.getString("student_id"));
                    txt_issuedate.setText(rs.getString("issue_date"));
                    txt_duedate.setText(rs.getString("due_date"));
                    txt_bookid.setText(rs.getString("book_id"));
                    txt_booktitle.setText(rs.getString("book_title"));
                    txt_quantity2.setText(rs.getString("qty"));
                    txt_status.setText(rs.getString("status"));
                } else {
                    JOptionPane.showMessageDialog(this, "Student ID cannot be found.");
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Student ID. Please enter an Student ID.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
    // Search function for DEFAULTER LIST Table
    public void defaulterlistSearchBar(String selectedCategory, String searchString, JTable DefaulterList_Table) {
        DefaultTableModel model = (DefaultTableModel) DefaulterList_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        DefaulterList_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Name":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Student ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2));
                break;
            case "Issue Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString,3));
                break;
            case "Due Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 4));
                break;
            case "Book ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 5));
                break;
            case "Book Title":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 6));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
    
      
        //Lift status function and report function
        private void liftStatus() {
            // Check if any of the text fields are empty
            if (txt_id.getText().isEmpty() || txt_fullname.getText().isEmpty() || txt_studentid2.getText().isEmpty() || 
                txt_issuedate.getText().isEmpty() || txt_duedate.getText().isEmpty() || txt_bookid.getText().isEmpty() || 
                txt_title.getText().isEmpty() || txt_quantity2.getText().isEmpty() || txt_status.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please fill out all fields.");
                return;
            }

            String studentID = txt_studentid2.getText();
            String studentName = txt_fullname.getText();

            try {
                Connection con = DBConnection.getConnection();

                // Check if the user has returned the book
                if (!hasReturnedBook(con, studentID)) {
                    JOptionPane.showMessageDialog(this, "The user has not returned the book yet.");
                    return;
                }

                // Update defaulterlist table
                String updateQuery = "UPDATE defaulterlist SET status = 'lifted' WHERE student_id = ?";
                try (PreparedStatement pst = con.prepareStatement(updateQuery)) {
                    pst.setString(1, studentID);
                    pst.executeUpdate();
                }

                // Insert report into reports table
                String insertReportQuery = "INSERT INTO reports (date, student_name, student_id, action) VALUES (CURRENT_DATE(), ?, ?, ?)";
                try (PreparedStatement pst = con.prepareStatement(insertReportQuery)) {
                    pst.setString(1, studentName);
                    pst.setString(2, studentID);
                    pst.setString(3, "Librarian has lifted " + studentName + " defaulter status");
                    pst.executeUpdate();
                }

                // Remove "lifted" status from the table
                removeLiftedStatusFromTable();

                JOptionPane.showMessageDialog(this, "Defaulter status lifted successfully.");
                fetchReports(); // Update the Reports_Table with the latest reports

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while lifting the defaulter status.");
            }
        }
    
        // Method to check if the user has returned the book
        private boolean hasReturnedBook(Connection con, String studentID) throws SQLException {
            String checkQuery = "SELECT COUNT(*) FROM issuedbooks WHERE student_id = ? AND status = 'overdue'";
            try (PreparedStatement pst = con.prepareStatement(checkQuery)) {
                pst.setString(1, studentID);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        return count == 0; // True if no books are "not returned"
                    }
                }
            }
            return false;
        }
        
        // Method to remove "lifted" status from the table
        private void removeLiftedStatusFromTable() {
            DefaultTableModel model = (DefaultTableModel) DefaulterList_Table.getModel();
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                String status = (String) model.getValueAt(i, model.findColumn("Status"));
                if ("lifted".equalsIgnoreCase(status)) {
                    model.removeRow(i);
                }
            }
        }
// ------------------------------------------------------End of Defaulter List Code ------------------------------------------------------//
// ------------------------------------------------------Start of Returned Books Code ------------------------------------------------------//
        
// Method to fetch data from returnedbooks and update ReturnedBooks_Table
            public void fetchReturnedBooks() {
                Connection conn = null;
                PreparedStatement pst = null;
                ResultSet rs = null;
                DefaultTableModel model = (DefaultTableModel) ReturnedBooks_Table.getModel();
                model.setRowCount(0);  // Clear existing data

                try {
                    // Establish the database connection
                    conn = DBConnection.getConnection();
                    String fetchSQL = "SELECT * FROM returnedbooks";
                    pst = conn.prepareStatement(fetchSQL);
                    rs = pst.executeQuery();

                    while (rs.next()) {
                        Object[] row = {
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("student_id"),
                            rs.getString("returned_date"),
                            rs.getString("book_id"),
                            rs.getString("book_title"),
                            rs.getInt("quantity"),
                            rs.getString("status")
                        };
                        model.addRow(row);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    // Close resources
                    try {
                        if (rs != null) rs.close();
                        if (pst != null) pst.close();
                        if (conn != null) conn.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
    // Search function for RETURNED BOOKS Table
    public void returnedbooksSearchBar(String selectedCategory, String searchString, JTable ReturnedBooks_Table) {
        DefaultTableModel model = (DefaultTableModel) ReturnedBooks_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        ReturnedBooks_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Name":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Student ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2));
                break;
            case "Returned Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString,3));
                break;
            case "Book ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString,4));
                break;
            case "Book Title":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 5));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }

// ------------------------------------------------------End of Returned Books  Code ------------------------------------------------------//
// ------------------------------------------------------Start of Reports Section Code ------------------------------------------------------//
    
    //Fetch reports data and insert it to Reports_Table
        public void fetchReports() {
        try {
            Connection con = DBConnection.getConnection();
            String query = "SELECT * FROM reports";
            try (PreparedStatement pst = con.prepareStatement(query);
                 ResultSet rs = pst.executeQuery()) {

                DefaultTableModel reportsTableModel = (DefaultTableModel) Reports_Table.getModel();
                reportsTableModel.setRowCount(0); // Clear the existing rows

                while (rs.next()) {
                    String date = rs.getString("date");
                    String studentName = rs.getString("student_name");
                    String studentID = rs.getString("student_id");
                    String action = rs.getString("action");

                    Object[] row = {date, studentName, studentID, action};
                    reportsTableModel.addRow(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        
    // Search function for Reports Table
    public void ReportsSearchFunction(String selectedCategory, String searchString, JTable Reports_Table) {
        DefaultTableModel model = (DefaultTableModel) Reports_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        Reports_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Name":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Student ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
    
// ------------------------------------------------------End of Reports Section Code ------------------------------------------------------//


  
        

//Basically a Clock
    public void setTime() {
            new Thread (new Runnable() {
                @Override
                public void run() {
                   while (true){
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Admin_UI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Date date = new Date();
                    SimpleDateFormat tf = new SimpleDateFormat("h:mm:ss aa");
                    SimpleDateFormat df = new SimpleDateFormat("EEEE, dd-MM-yyyy");
                    String time = tf.format(date);
                    Time.setText(time.split(" ")[0]+" "+time.split(" ")[1]);
                    Date.setText(df.format(date));
                }
                }

        }).start();
                    }
    
      
    
    // Change the color of Tabs inside the SideBar
     private void resetTabColors() {
        HomeTab.setBackground(new Color(15,28,44));
        ManageBooksTab.setBackground(new Color(15,28,44));
        ManageUserTab.setBackground(new Color(15,28,44));
        IssueBooksTab.setBackground(new Color(15,28,44));
        Records.setBackground(new Color(15,28,44));
        ReturnedBooksTab.setBackground(new Color(15,28,44));
        DefaulterTab.setBackground(new Color(15,28,44));
        ReportsTab.setBackground(new Color(15,28,44));
    }
    
     // For switching panels
    private void togglePanelVisibility(JPanel panelToShow) {
    resetTabColors();
    
    Home_Panel.setVisible(false);
    ManageUser_Panel.setVisible(false);
    ManageBooks_Panel.setVisible(false);
    IssueBooks_Panel.setVisible(false);
    Defaulter_Panel.setVisible(false);
    Records_Panel.setVisible(false);
    ReturnedBooks_Panel.setVisible(false);
    Reports_Panel.setVisible(false);
    
    panelToShow.setVisible(true);
}
    
    

    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MainPanel = new javax.swing.JPanel();
        minimize = new javax.swing.JLabel();
        close = new javax.swing.JLabel();
        Home_Panel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        Dashboard_StudentCount = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        DashBoard_PendingCount = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        Dashboard_BooksCount = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        Dashboard_AvailableBooks = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Dashboard_StudentDetails = new rojeru_san.complementos.RSTableMetro();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Dashboard_BookDetails = new rojeru_san.complementos.RSTableMetro();
        Time = new javax.swing.JLabel();
        Date = new javax.swing.JLabel();
        ManageBooks_Panel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txt_searchBook = new app.bolivia.swing.JCTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        BookDetails_Table = new rojeru_san.complementos.RSTableMetro();
        txt_searchBy = new lms.ComboBoxSuggestion();
        jPanel1 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        txt_booktitle = new app.bolivia.swing.JCTextField();
        jLabel37 = new javax.swing.JLabel();
        txt_bookauthor = new app.bolivia.swing.JCTextField();
        jLabel38 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        txt_quantity = new app.bolivia.swing.JCTextField();
        Add_button = new rojeru_san.complementos.RSButtonHover();
        Update_button = new rojeru_san.complementos.RSButtonHover();
        Delete_button = new rojeru_san.complementos.RSButtonHover();
        jLabel40 = new javax.swing.JLabel();
        txt_bookID = new app.bolivia.swing.JCTextField();
        txt_booksection = new javax.swing.JComboBox<>();
        ManageUser_Panel = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        addStudent_button = new rojeru_san.complementos.RSButtonHover();
        Delete_button1 = new rojeru_san.complementos.RSButtonHover();
        jLabel24 = new javax.swing.JLabel();
        txt_studentid = new app.bolivia.swing.JCTextField();
        jLabel31 = new javax.swing.JLabel();
        txt_studentname = new app.bolivia.swing.JCTextField();
        jLabel32 = new javax.swing.JLabel();
        txt_section = new app.bolivia.swing.JCTextField();
        jLabel33 = new javax.swing.JLabel();
        txt_strand = new app.bolivia.swing.JCTextField();
        jLabel35 = new javax.swing.JLabel();
        txt_email = new app.bolivia.swing.JCTextField();
        jLabel36 = new javax.swing.JLabel();
        txt_mobileNo = new app.bolivia.swing.JCTextField();
        Address = new javax.swing.JLabel();
        txt_address = new app.bolivia.swing.JCTextField();
        Update_button1 = new rojeru_san.complementos.RSButtonHover();
        jLabel42 = new javax.swing.JLabel();
        Delete_button2 = new rojeru_san.complementos.RSButtonHover();
        jScrollPane5 = new javax.swing.JScrollPane();
        UserManager_Table = new rojeru_san.complementos.RSTableMetro();
        jLabel3 = new javax.swing.JLabel();
        user_Category = new lms.ComboBoxSuggestion();
        txt_searchUser = new app.bolivia.swing.JCTextField();
        IssueBooks_Panel = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        BookSelection_Panel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        SelectedBook_Table = new rojeru_san.complementos.RSTableMetro();
        Proceed_Button = new rojeru_san.complementos.RSButtonHover();
        Back_Button = new rojeru_san.complementos.RSButtonHover();
        Delete_Button = new rojeru_san.complementos.RSButtonHover();
        SelectDelete_Button = new rojeru_san.complementos.RSButtonHover();
        jLabel63 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        IssueDate = new org.jdesktop.swingx.JXDatePicker();
        DueDate = new org.jdesktop.swingx.JXDatePicker();
        StudentSelection_Panel = new javax.swing.JPanel();
        tf_studentID = new app.bolivia.swing.JCTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        tf_studentName = new app.bolivia.swing.JCTextField();
        jLabel48 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        tf_studentSection = new app.bolivia.swing.JCTextField();
        tf_studentEmail = new app.bolivia.swing.JCTextField();
        jLabel51 = new javax.swing.JLabel();
        tf_studentMobile = new app.bolivia.swing.JCTextField();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        tf_studentStrand = new app.bolivia.swing.JCTextField();
        tf_studentAddress = new app.bolivia.swing.JCTextField();
        StudentPanel_Button = new rojeru_san.complementos.RSButtonHover();
        Cancel_Button = new rojeru_san.complementos.RSButtonHover();
        jLabel62 = new javax.swing.JLabel();
        BookList_Panel = new javax.swing.JPanel();
        BookList_JScrollPane = new javax.swing.JScrollPane();
        BookList_Table = new rojeru_san.complementos.RSTableMetro();
        StudentList_Panel = new javax.swing.JPanel();
        StudentList_JScrollPane = new javax.swing.JScrollPane();
        StudentList_Table = new rojeru_san.complementos.RSTableMetro();
        BorrowerList_JScrollPane = new javax.swing.JScrollPane();
        IssuedList_Table = new rojeru_san.complementos.RSTableMetro();
        Book_SearchBar = new javax.swing.JPanel();
        issueBook_bookSearchBar = new app.bolivia.swing.JCTextField();
        issueBook_bookCategory = new lms.ComboBoxSuggestion();
        jLabel7 = new javax.swing.JLabel();
        Student_SearchBar = new javax.swing.JPanel();
        issueBook_studentSearchBar = new app.bolivia.swing.JCTextField();
        issueBook_studentCategory = new lms.ComboBoxSuggestion();
        jLabel5 = new javax.swing.JLabel();
        Records_Panel = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        BorrowerList_JScrollPane1 = new javax.swing.JScrollPane();
        IssuedList_Table2 = new rojeru_san.complementos.RSTableMetro();
        jLabel43 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        user_Category3 = new lms.ComboBoxSuggestion();
        txt_searchUser3 = new app.bolivia.swing.JCTextField();
        jLabel49 = new javax.swing.JLabel();
        tf_bookid = new app.bolivia.swing.JCTextField();
        tf_id = new app.bolivia.swing.JCTextField();
        tf_name = new app.bolivia.swing.JCTextField();
        tf_studentid = new app.bolivia.swing.JCTextField();
        tf_issuedate = new app.bolivia.swing.JCTextField();
        tf_duedate = new app.bolivia.swing.JCTextField();
        tf_booktitle = new app.bolivia.swing.JCTextField();
        tf_qty = new app.bolivia.swing.JCTextField();
        tf_status = new app.bolivia.swing.JCTextField();
        cancel_button = new rojeru_san.complementos.RSButtonHover();
        return_button = new rojeru_san.complementos.RSButtonHover();
        records_search = new rojeru_san.complementos.RSButtonHover();
        jLabel60 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        ReturnedBooks_Panel = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        BorrowerList_JScrollPane3 = new javax.swing.JScrollPane();
        ReturnedBooks_Table = new rojeru_san.complementos.RSTableMetro();
        jLabel16 = new javax.swing.JLabel();
        returnedbooksSearchBar = new app.bolivia.swing.JCTextField();
        returnedBook_Category = new lms.ComboBoxSuggestion();
        Defaulter_Panel = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        BorrowerList_JScrollPane2 = new javax.swing.JScrollPane();
        DefaulterList_Table = new rojeru_san.complementos.RSTableMetro();
        jLabel56 = new javax.swing.JLabel();
        defaulterlist_Category = new lms.ComboBoxSuggestion();
        defaulterlistSearchBar = new app.bolivia.swing.JCTextField();
        jPanel7 = new javax.swing.JPanel();
        txt_id = new app.bolivia.swing.JCTextField();
        txt_issuedate = new app.bolivia.swing.JCTextField();
        txt_quantity2 = new app.bolivia.swing.JCTextField();
        txt_duedate = new app.bolivia.swing.JCTextField();
        txt_fullname = new app.bolivia.swing.JCTextField();
        txt_status = new app.bolivia.swing.JCTextField();
        txt_bookid = new app.bolivia.swing.JCTextField();
        txt_studentid2 = new app.bolivia.swing.JCTextField();
        defaulterList_search = new rojeru_san.complementos.RSButtonHover();
        txt_title = new app.bolivia.swing.JCTextField();
        LiftStatus_button = new rojeru_san.complementos.RSButtonHover();
        cancel_button1 = new rojeru_san.complementos.RSButtonHover();
        jLabel70 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        Reports_Panel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        BorrowerList_JScrollPane4 = new javax.swing.JScrollPane();
        Reports_Table = new rojeru_san.complementos.RSTableMetro();
        jLabel57 = new javax.swing.JLabel();
        reports_Category = new lms.ComboBoxSuggestion();
        Reports_SearchBar = new app.bolivia.swing.JCTextField();
        jLabel58 = new javax.swing.JLabel();
        SideTab = new javax.swing.JPanel();
        logo = new javax.swing.JLabel();
        features = new javax.swing.JLabel();
        Settings_Panel = new javax.swing.JPanel();
        Logout_button = new javax.swing.JButton();
        HomeTab = new javax.swing.JPanel();
        Home_button = new javax.swing.JButton();
        ManageBooksTab = new javax.swing.JPanel();
        Managebooks_button = new javax.swing.JButton();
        ManageUserTab = new javax.swing.JPanel();
        User_button = new javax.swing.JButton();
        Records = new javax.swing.JPanel();
        Records_button = new javax.swing.JButton();
        ReportsTab = new javax.swing.JPanel();
        Reports_button = new javax.swing.JButton();
        ReturnedBooksTab = new javax.swing.JPanel();
        Returnbooks_button = new javax.swing.JButton();
        DefaulterTab = new javax.swing.JPanel();
        Receipts_button = new javax.swing.JButton();
        IssueBooksTab = new javax.swing.JPanel();
        Issuebooks_button = new javax.swing.JButton();
        SettingsTab = new javax.swing.JPanel();
        Settings_button = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        USERNAME = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        MainPanel.setBackground(new java.awt.Color(255, 255, 255));
        MainPanel.setForeground(new java.awt.Color(255, 255, 255));
        MainPanel.setPreferredSize(new java.awt.Dimension(1279, 720));
        MainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        minimize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/minimize.png"))); // NOI18N
        minimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                minimizeMousePressed(evt);
            }
        });
        MainPanel.add(minimize, new org.netbeans.lib.awtextra.AbsoluteConstraints(1220, 10, -1, -1));

        close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/close.png"))); // NOI18N
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeMousePressed(evt);
            }
        });
        MainPanel.add(close, new org.netbeans.lib.awtextra.AbsoluteConstraints(1250, 10, -1, -1));

        Home_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Home_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel14.setBackground(new java.awt.Color(15, 28, 44));
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel22.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Total Users");
        jPanel14.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 110, 40));

        jLabel23.setBackground(new java.awt.Color(153, 153, 153));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user_1.png"))); // NOI18N
        jLabel23.setOpaque(true);
        jPanel14.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 70, 70));

        Dashboard_StudentCount.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Dashboard_StudentCount.setForeground(new java.awt.Color(255, 255, 255));
        Dashboard_StudentCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Dashboard_StudentCount.setText("0");
        jPanel14.add(Dashboard_StudentCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, 110, 40));

        jLabel25.setBackground(new java.awt.Color(207, 76, 56));
        jLabel25.setOpaque(true);
        jPanel14.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 20));

        jPanel3.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 230, 130));

        jPanel12.setBackground(new java.awt.Color(15, 28, 44));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel14.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Pending Books");
        jPanel12.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 150, 40));

        jLabel15.setBackground(new java.awt.Color(153, 153, 153));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/defaulterlist.png"))); // NOI18N
        jLabel15.setOpaque(true);
        jPanel12.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 70, 70));

        DashBoard_PendingCount.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        DashBoard_PendingCount.setForeground(new java.awt.Color(255, 255, 255));
        DashBoard_PendingCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DashBoard_PendingCount.setText("0");
        jPanel12.add(DashBoard_PendingCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 110, 40));

        jLabel17.setBackground(new java.awt.Color(207, 76, 56));
        jLabel17.setOpaque(true);
        jPanel12.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 20));

        jPanel3.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 70, 230, 130));

        jPanel15.setBackground(new java.awt.Color(15, 28, 44));
        jPanel15.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel26.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Total Books");
        jPanel15.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 110, 40));

        jLabel27.setBackground(new java.awt.Color(153, 153, 153));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/book_1.png"))); // NOI18N
        jLabel27.setOpaque(true);
        jPanel15.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 70, 70));

        Dashboard_BooksCount.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Dashboard_BooksCount.setForeground(new java.awt.Color(255, 255, 255));
        Dashboard_BooksCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Dashboard_BooksCount.setText("0");
        jPanel15.add(Dashboard_BooksCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, 110, 40));

        jLabel29.setBackground(new java.awt.Color(207, 76, 56));
        jLabel29.setOpaque(true);
        jPanel15.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 20));

        jPanel3.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 70, 230, 130));

        jPanel13.setBackground(new java.awt.Color(15, 28, 44));
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Available Book");
        jPanel13.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 140, 40));

        jLabel19.setBackground(new java.awt.Color(153, 153, 153));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/issuedbooks.png"))); // NOI18N
        jLabel19.setOpaque(true);
        jPanel13.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 70, 70));

        Dashboard_AvailableBooks.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Dashboard_AvailableBooks.setForeground(new java.awt.Color(255, 255, 255));
        Dashboard_AvailableBooks.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Dashboard_AvailableBooks.setText("0");
        jPanel13.add(Dashboard_AvailableBooks, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 110, 40));

        jLabel21.setBackground(new java.awt.Color(207, 76, 56));
        jLabel21.setOpaque(true);
        jPanel13.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 20));

        jPanel3.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 70, 230, 130));

        Dashboard_StudentDetails.setBackground(new java.awt.Color(15, 28, 44));
        Dashboard_StudentDetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Student ID", "Student Name", "Email", "Strand"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        Dashboard_StudentDetails.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        Dashboard_StudentDetails.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        Dashboard_StudentDetails.setColorBordeHead(new java.awt.Color(15, 28, 44));
        Dashboard_StudentDetails.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        Dashboard_StudentDetails.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        Dashboard_StudentDetails.setColumnSelectionAllowed(true);
        Dashboard_StudentDetails.setFuenteFilas(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        Dashboard_StudentDetails.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        Dashboard_StudentDetails.setFuenteHead(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        Dashboard_StudentDetails.setGridColor(new java.awt.Color(15, 28, 44));
        Dashboard_StudentDetails.setRowHeight(25);
        Dashboard_StudentDetails.setSelectionBackground(new java.awt.Color(15, 28, 44));
        Dashboard_StudentDetails.setShowGrid(false);
        Dashboard_StudentDetails.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(Dashboard_StudentDetails);
        Dashboard_StudentDetails.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (Dashboard_StudentDetails.getColumnModel().getColumnCount() > 0) {
            Dashboard_StudentDetails.getColumnModel().getColumn(0).setResizable(false);
            Dashboard_StudentDetails.getColumnModel().getColumn(1).setResizable(false);
            Dashboard_StudentDetails.getColumnModel().getColumn(2).setResizable(false);
            Dashboard_StudentDetails.getColumnModel().getColumn(3).setResizable(false);
        }

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 500, 390));

        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Student Details");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 120, 40));

        jLabel11.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Books Detail");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 210, 120, 40));

        Dashboard_BookDetails.setBackground(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        Dashboard_BookDetails.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setColorBordeHead(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        Dashboard_BookDetails.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        Dashboard_BookDetails.setColumnSelectionAllowed(true);
        Dashboard_BookDetails.setFuenteFilas(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        Dashboard_BookDetails.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        Dashboard_BookDetails.setFuenteHead(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        Dashboard_BookDetails.setGridColor(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setRowHeight(25);
        Dashboard_BookDetails.setSelectionBackground(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setShowGrid(false);
        Dashboard_BookDetails.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(Dashboard_BookDetails);
        Dashboard_BookDetails.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (Dashboard_BookDetails.getColumnModel().getColumnCount() > 0) {
            Dashboard_BookDetails.getColumnModel().getColumn(0).setResizable(false);
            Dashboard_BookDetails.getColumnModel().getColumn(1).setResizable(false);
            Dashboard_BookDetails.getColumnModel().getColumn(2).setResizable(false);
            Dashboard_BookDetails.getColumnModel().getColumn(3).setResizable(false);
        }

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 250, 500, 390));

        Time.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Time.setText("12:00:00 AM");
        jPanel3.add(Time, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 160, 30));

        Date.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Date.setText("Sunday, 01-12, 2024");
        jPanel3.add(Date, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 160, 30));

        Home_Panel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 1030, 650));

        MainPanel.add(Home_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        ManageBooks_Panel.setBackground(new java.awt.Color(204, 204, 204));
        ManageBooks_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel4MouseClicked(evt);
            }
        });
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txt_searchBook.setBackground(new java.awt.Color(15, 28, 44));
        txt_searchBook.setForeground(new java.awt.Color(255, 255, 255));
        txt_searchBook.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_searchBookKeyReleased(evt);
            }
        });
        jPanel4.add(txt_searchBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 40, 210, -1));

        jLabel1.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Search by - ");
        jPanel4.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, 100, 30));

        BookDetails_Table.setBackground(new java.awt.Color(15, 28, 44));
        BookDetails_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Book ID", "Book Title", "Author", "Category", "Qty", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        BookDetails_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        BookDetails_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        BookDetails_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        BookDetails_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        BookDetails_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        BookDetails_Table.setColumnSelectionAllowed(true);
        BookDetails_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        BookDetails_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        BookDetails_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        BookDetails_Table.setGridColor(new java.awt.Color(15, 28, 44));
        BookDetails_Table.setRowHeight(25);
        BookDetails_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        BookDetails_Table.setShowGrid(false);
        BookDetails_Table.getTableHeader().setReorderingAllowed(false);
        BookDetails_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BookDetails_TableMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(BookDetails_Table);
        BookDetails_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (BookDetails_Table.getColumnModel().getColumnCount() > 0) {
            BookDetails_Table.getColumnModel().getColumn(0).setResizable(false);
            BookDetails_Table.getColumnModel().getColumn(1).setResizable(false);
            BookDetails_Table.getColumnModel().getColumn(2).setResizable(false);
            BookDetails_Table.getColumnModel().getColumn(3).setResizable(false);
            BookDetails_Table.getColumnModel().getColumn(4).setResizable(false);
            BookDetails_Table.getColumnModel().getColumn(5).setResizable(false);
        }

        jPanel4.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 70, 740, 570));

        txt_searchBy.setEditable(false);
        txt_searchBy.setForeground(new java.awt.Color(255, 255, 255));
        txt_searchBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Book ID", "Book Title", "Author", "Category" }));
        jPanel4.add(txt_searchBy, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 40, 110, 30));

        jPanel1.setBackground(new java.awt.Color(15, 28, 44));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel30.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(207, 76, 56));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/book.png"))); // NOI18N
        jLabel30.setText("BOOK MANAGER");
        jPanel1.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 110));

        jPanel6.setBackground(new java.awt.Color(15, 28, 44));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Enter Book Details");
        jPanel6.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 180, 20));

        jLabel39.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("Book Title");
        jPanel6.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 120, 20));

        txt_booktitle.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_booktitle.setPlaceholder("Enter Book Title");
        jPanel6.add(txt_booktitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 200, 30));

        jLabel37.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("Book Author");
        jPanel6.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 130, 20));

        txt_bookauthor.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_bookauthor.setPlaceholder("Enter Book Author");
        jPanel6.add(txt_bookauthor, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 200, 30));

        jLabel38.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("Category");
        jPanel6.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 140, -1));

        jLabel20.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Quantity");
        jPanel6.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 70, -1));

        txt_quantity.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_quantity.setPlaceholder("Enter Quantity");
        jPanel6.add(txt_quantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, 200, -1));

        Add_button.setBackground(new java.awt.Color(255, 255, 255));
        Add_button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Add_button.setForeground(new java.awt.Color(15, 28, 44));
        Add_button.setText("Add");
        Add_button.setColorHover(new java.awt.Color(207, 76, 56));
        Add_button.setColorText(new java.awt.Color(15, 28, 44));
        Add_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Add_button.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Add_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Add_buttonActionPerformed(evt);
            }
        });
        jPanel6.add(Add_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, 60, 20));

        Update_button.setBackground(new java.awt.Color(255, 255, 255));
        Update_button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Update_button.setForeground(new java.awt.Color(15, 28, 44));
        Update_button.setText("Update");
        Update_button.setColorHover(new java.awt.Color(207, 76, 56));
        Update_button.setColorText(new java.awt.Color(15, 28, 44));
        Update_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Update_button.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Update_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Update_buttonActionPerformed(evt);
            }
        });
        jPanel6.add(Update_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 380, 60, 20));

        Delete_button.setBackground(new java.awt.Color(255, 255, 255));
        Delete_button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Delete_button.setForeground(new java.awt.Color(15, 28, 44));
        Delete_button.setText("Delete");
        Delete_button.setColorHover(new java.awt.Color(207, 76, 56));
        Delete_button.setColorText(new java.awt.Color(15, 28, 44));
        Delete_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Delete_button.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Delete_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Delete_buttonActionPerformed(evt);
            }
        });
        jPanel6.add(Delete_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 380, 60, 20));

        jLabel40.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("Book ID");
        jPanel6.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 120, 20));

        txt_bookID.setEditable(false);
        txt_bookID.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_bookID.setPlaceholder("Book ID");
        jPanel6.add(txt_bookID, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 200, 30));

        txt_booksection.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_booksection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ICT", "ABM", "Science", "Psychology", "Law", "Math", "Research", "English", "HUMSS" }));
        txt_booksection.setBorder(null);
        txt_booksection.setOpaque(true);
        jPanel6.add(txt_booksection, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 200, 30));

        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 240, 520));

        jPanel4.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 650));

        ManageBooks_Panel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 1020, 650));

        MainPanel.add(ManageBooks_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        ManageUser_Panel.setBackground(new java.awt.Color(204, 204, 204));
        ManageUser_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel16.setBackground(new java.awt.Color(255, 255, 255));
        jPanel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel16MouseClicked(evt);
            }
        });
        jPanel16.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(15, 28, 44));
        jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel2MouseClicked(evt);
            }
        });
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel34.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(207, 76, 56));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel34.setText("USER MANAGER");
        jPanel2.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 110));

        jPanel18.setBackground(new java.awt.Color(15, 28, 44));
        jPanel18.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addStudent_button.setBackground(new java.awt.Color(255, 255, 255));
        addStudent_button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        addStudent_button.setForeground(new java.awt.Color(15, 28, 44));
        addStudent_button.setText("Add");
        addStudent_button.setColorHover(new java.awt.Color(207, 76, 56));
        addStudent_button.setColorText(new java.awt.Color(15, 28, 44));
        addStudent_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        addStudent_button.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        addStudent_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudent_buttonActionPerformed(evt);
            }
        });
        jPanel18.add(addStudent_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, 50, 20));

        Delete_button1.setBackground(new java.awt.Color(255, 255, 255));
        Delete_button1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Delete_button1.setForeground(new java.awt.Color(15, 28, 44));
        Delete_button1.setText("Delete");
        Delete_button1.setColorHover(new java.awt.Color(207, 76, 56));
        Delete_button1.setColorText(new java.awt.Color(15, 28, 44));
        Delete_button1.setColorTextHover(new java.awt.Color(15, 28, 44));
        Delete_button1.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Delete_button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Delete_button1ActionPerformed(evt);
            }
        });
        jPanel18.add(Delete_button1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 490, 60, 20));

        jLabel24.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Enter Student Detail");
        jPanel18.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 220, 20));

        txt_studentid.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_studentid.setPlaceholder("Enter Student ID");
        jPanel18.add(txt_studentid, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 220, -1));

        jLabel31.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("Student Name");
        jPanel18.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 120, 20));

        txt_studentname.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_studentname.setPlaceholder("Enter Student Name");
        jPanel18.add(txt_studentname, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 220, -1));

        jLabel32.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("Section");
        jPanel18.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 90, 20));

        txt_section.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_section.setPlaceholder("Enter Section");
        jPanel18.add(txt_section, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 220, -1));

        jLabel33.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setText("Strand");
        jPanel18.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 110, 20));

        txt_strand.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_strand.setPlaceholder("Enter Strand");
        jPanel18.add(txt_strand, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 220, -1));

        jLabel35.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("Email");
        jPanel18.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 120, 20));

        txt_email.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_email.setPlaceholder("Enter Email");
        jPanel18.add(txt_email, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 330, 220, -1));

        jLabel36.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("Mobile No,");
        jPanel18.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 110, 20));

        txt_mobileNo.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_mobileNo.setPlaceholder("Enter Mobile No.");
        jPanel18.add(txt_mobileNo, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 220, -1));

        Address.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Address.setForeground(new java.awt.Color(255, 255, 255));
        Address.setText("Address");
        jPanel18.add(Address, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 430, 100, 20));

        txt_address.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        txt_address.setPlaceholder("Enter Address");
        jPanel18.add(txt_address, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, 220, -1));

        Update_button1.setBackground(new java.awt.Color(255, 255, 255));
        Update_button1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Update_button1.setForeground(new java.awt.Color(15, 28, 44));
        Update_button1.setText("Update");
        Update_button1.setColorHover(new java.awt.Color(207, 76, 56));
        Update_button1.setColorText(new java.awt.Color(15, 28, 44));
        Update_button1.setColorTextHover(new java.awt.Color(15, 28, 44));
        Update_button1.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Update_button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Update_button1ActionPerformed(evt);
            }
        });
        jPanel18.add(Update_button1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 490, 60, 20));

        jLabel42.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setText("Student ID");
        jPanel18.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 150, 20));

        Delete_button2.setBackground(new java.awt.Color(255, 255, 255));
        Delete_button2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Delete_button2.setForeground(new java.awt.Color(15, 28, 44));
        Delete_button2.setText("Cancel");
        Delete_button2.setColorHover(new java.awt.Color(207, 76, 56));
        Delete_button2.setColorText(new java.awt.Color(15, 28, 44));
        Delete_button2.setColorTextHover(new java.awt.Color(15, 28, 44));
        Delete_button2.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Delete_button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Delete_button2ActionPerformed(evt);
            }
        });
        jPanel18.add(Delete_button2, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 490, 50, 20));

        jPanel2.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 240, 540));

        jPanel16.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 650));

        UserManager_Table.setBackground(new java.awt.Color(15, 28, 44));
        UserManager_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Student ID", "Student Name", "Section", "Strand", "Email", "Mobile No.", "Address"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        UserManager_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        UserManager_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        UserManager_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        UserManager_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        UserManager_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        UserManager_Table.setColumnSelectionAllowed(true);
        UserManager_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        UserManager_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        UserManager_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        UserManager_Table.setGridColor(new java.awt.Color(15, 28, 44));
        UserManager_Table.setMultipleSeleccion(false);
        UserManager_Table.setRowHeight(25);
        UserManager_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        UserManager_Table.setShowGrid(false);
        UserManager_Table.getTableHeader().setReorderingAllowed(false);
        UserManager_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                UserManager_TableMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(UserManager_Table);
        UserManager_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (UserManager_Table.getColumnModel().getColumnCount() > 0) {
            UserManager_Table.getColumnModel().getColumn(0).setResizable(false);
            UserManager_Table.getColumnModel().getColumn(2).setResizable(false);
            UserManager_Table.getColumnModel().getColumn(3).setResizable(false);
            UserManager_Table.getColumnModel().getColumn(4).setResizable(false);
            UserManager_Table.getColumnModel().getColumn(5).setResizable(false);
        }

        jPanel16.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 50, 750, 590));

        jLabel3.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Search by - ");
        jPanel16.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 20, 100, 30));

        user_Category.setEditable(false);
        user_Category.setForeground(new java.awt.Color(255, 255, 255));
        user_Category.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Student ID", "Student Name" }));
        jPanel16.add(user_Category, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 20, 110, -1));

        txt_searchUser.setBackground(new java.awt.Color(15, 28, 44));
        txt_searchUser.setForeground(new java.awt.Color(255, 255, 255));
        txt_searchUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_searchUserActionPerformed(evt);
            }
        });
        txt_searchUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_searchUserKeyReleased(evt);
            }
        });
        jPanel16.add(txt_searchUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 20, 210, -1));

        ManageUser_Panel.add(jPanel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 1020, 650));

        MainPanel.add(ManageUser_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        IssueBooks_Panel.setBackground(new java.awt.Color(204, 204, 204));
        IssueBooks_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel17.setBackground(new java.awt.Color(255, 255, 255));
        jPanel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel17MouseClicked(evt);
            }
        });
        jPanel17.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel5.setBackground(new java.awt.Color(15, 28, 44));
        jPanel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel5MouseClicked(evt);
            }
        });
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel41.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(207, 76, 56));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel41.setText("Issue Books");
        jPanel5.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 300, 110));

        BookSelection_Panel.setBackground(new java.awt.Color(15, 28, 44));
        BookSelection_Panel.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        BookSelection_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel47.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("SELECT BOOK");
        BookSelection_Panel.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 130, -1));

        SelectedBook_Table.setBackground(new java.awt.Color(15, 28, 44));
        SelectedBook_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Book Title", "Book ID", "QTY"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        SelectedBook_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        SelectedBook_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        SelectedBook_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        SelectedBook_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        SelectedBook_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        SelectedBook_Table.setColumnSelectionAllowed(true);
        SelectedBook_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        SelectedBook_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        SelectedBook_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        SelectedBook_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        SelectedBook_Table.setGridColor(new java.awt.Color(15, 28, 44));
        SelectedBook_Table.setMultipleSeleccion(false);
        SelectedBook_Table.setRowHeight(25);
        SelectedBook_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        SelectedBook_Table.setShowGrid(false);
        SelectedBook_Table.getTableHeader().setReorderingAllowed(false);
        jScrollPane8.setViewportView(SelectedBook_Table);
        SelectedBook_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (SelectedBook_Table.getColumnModel().getColumnCount() > 0) {
            SelectedBook_Table.getColumnModel().getColumn(0).setResizable(false);
            SelectedBook_Table.getColumnModel().getColumn(1).setResizable(false);
            SelectedBook_Table.getColumnModel().getColumn(2).setResizable(false);
        }

        BookSelection_Panel.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 280, 370));

        Proceed_Button.setBackground(new java.awt.Color(255, 255, 255));
        Proceed_Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Proceed_Button.setForeground(new java.awt.Color(15, 28, 44));
        Proceed_Button.setText("Proceed");
        Proceed_Button.setColorHover(new java.awt.Color(207, 76, 56));
        Proceed_Button.setColorText(new java.awt.Color(15, 28, 44));
        Proceed_Button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Proceed_Button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        Proceed_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Proceed_ButtonActionPerformed(evt);
            }
        });
        BookSelection_Panel.add(Proceed_Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 490, 70, 20));

        Back_Button.setBackground(new java.awt.Color(255, 255, 255));
        Back_Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Back_Button.setForeground(new java.awt.Color(15, 28, 44));
        Back_Button.setText("Back");
        Back_Button.setColorHover(new java.awt.Color(207, 76, 56));
        Back_Button.setColorText(new java.awt.Color(15, 28, 44));
        Back_Button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Back_Button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        Back_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Back_ButtonActionPerformed(evt);
            }
        });
        BookSelection_Panel.add(Back_Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 490, 70, 20));

        Delete_Button.setBackground(new java.awt.Color(255, 255, 255));
        Delete_Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Delete_Button.setForeground(new java.awt.Color(15, 28, 44));
        Delete_Button.setText("Delete");
        Delete_Button.setColorHover(new java.awt.Color(207, 76, 56));
        Delete_Button.setColorText(new java.awt.Color(15, 28, 44));
        Delete_Button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Delete_Button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        Delete_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Delete_ButtonActionPerformed(evt);
            }
        });
        BookSelection_Panel.add(Delete_Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 490, 70, 20));

        SelectDelete_Button.setBackground(new java.awt.Color(255, 255, 255));
        SelectDelete_Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        SelectDelete_Button.setForeground(new java.awt.Color(15, 28, 44));
        SelectDelete_Button.setText("Delete Item");
        SelectDelete_Button.setColorHover(new java.awt.Color(207, 76, 56));
        SelectDelete_Button.setColorText(new java.awt.Color(15, 28, 44));
        SelectDelete_Button.setColorTextHover(new java.awt.Color(15, 28, 44));
        SelectDelete_Button.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        SelectDelete_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectDelete_ButtonActionPerformed(evt);
            }
        });
        BookSelection_Panel.add(SelectDelete_Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 490, 70, 20));

        jLabel63.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel63.setForeground(new java.awt.Color(255, 255, 255));
        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel63.setText("Due Date");
        BookSelection_Panel.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 430, 80, -1));

        jLabel54.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel54.setText("Issue Date");
        BookSelection_Panel.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 430, 80, -1));

        IssueDate.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        BookSelection_Panel.add(IssueDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, 120, 20));

        DueDate.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        BookSelection_Panel.add(DueDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 450, 120, 20));

        jPanel5.add(BookSelection_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 280, 530));

        StudentSelection_Panel.setBackground(new java.awt.Color(15, 28, 44));
        StudentSelection_Panel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(255, 255, 255)));
        StudentSelection_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tf_studentID.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentID.setPlaceholder("Student ID");
        StudentSelection_Panel.add(tf_studentID, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 230, 30));

        jLabel44.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setText("Student ID");
        StudentSelection_Panel.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 150, -1));

        jLabel45.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("SELECT STUDENT");
        StudentSelection_Panel.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 130, -1));

        tf_studentName.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentName.setPlaceholder("Student Name");
        StudentSelection_Panel.add(tf_studentName, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 230, 30));

        jLabel48.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("Student Name");
        StudentSelection_Panel.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 150, -1));

        jLabel50.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setText("Section");
        StudentSelection_Panel.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 150, -1));

        tf_studentSection.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentSection.setPlaceholder("Section");
        StudentSelection_Panel.add(tf_studentSection, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 230, 30));

        tf_studentEmail.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentEmail.setPlaceholder("Email");
        StudentSelection_Panel.add(tf_studentEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, 230, 30));

        jLabel51.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("Email");
        StudentSelection_Panel.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 150, -1));

        tf_studentMobile.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentMobile.setPlaceholder("Mobile No.");
        StudentSelection_Panel.add(tf_studentMobile, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, 230, 30));

        jLabel52.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setText("Mobile No.");
        StudentSelection_Panel.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 150, -1));

        jLabel53.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("Strand");
        StudentSelection_Panel.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 150, -1));

        tf_studentStrand.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentStrand.setPlaceholder("Strand");
        StudentSelection_Panel.add(tf_studentStrand, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 250, 230, 30));

        tf_studentAddress.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        tf_studentAddress.setPlaceholder("Address");
        StudentSelection_Panel.add(tf_studentAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 430, 230, 30));

        StudentPanel_Button.setBackground(new java.awt.Color(255, 255, 255));
        StudentPanel_Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        StudentPanel_Button.setForeground(new java.awt.Color(15, 28, 44));
        StudentPanel_Button.setText("Proceed");
        StudentPanel_Button.setColorHover(new java.awt.Color(207, 76, 56));
        StudentPanel_Button.setColorText(new java.awt.Color(15, 28, 44));
        StudentPanel_Button.setColorTextHover(new java.awt.Color(15, 28, 44));
        StudentPanel_Button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        StudentPanel_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StudentPanel_ButtonActionPerformed(evt);
            }
        });
        StudentSelection_Panel.add(StudentPanel_Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 480, 90, 20));

        Cancel_Button.setBackground(new java.awt.Color(255, 255, 255));
        Cancel_Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Cancel_Button.setForeground(new java.awt.Color(15, 28, 44));
        Cancel_Button.setText("Cancel");
        Cancel_Button.setColorHover(new java.awt.Color(207, 76, 56));
        Cancel_Button.setColorText(new java.awt.Color(15, 28, 44));
        Cancel_Button.setColorTextHover(new java.awt.Color(15, 28, 44));
        Cancel_Button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        Cancel_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Cancel_ButtonActionPerformed(evt);
            }
        });
        StudentSelection_Panel.add(Cancel_Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 480, 90, 20));

        jLabel62.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(255, 255, 255));
        jLabel62.setText("Address");
        StudentSelection_Panel.add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, 150, -1));

        jPanel5.add(StudentSelection_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, 280, 530));

        jPanel17.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 300, 650));

        BookList_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        BookList_Table.setBackground(new java.awt.Color(15, 28, 44));
        BookList_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Book ID", "Book Title", "Author", "Section", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        BookList_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        BookList_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        BookList_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        BookList_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        BookList_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        BookList_Table.setColumnSelectionAllowed(true);
        BookList_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        BookList_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        BookList_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        BookList_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        BookList_Table.setGridColor(new java.awt.Color(15, 28, 44));
        BookList_Table.setMultipleSeleccion(false);
        BookList_Table.setRowHeight(25);
        BookList_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        BookList_Table.setShowGrid(false);
        BookList_Table.getTableHeader().setReorderingAllowed(false);
        BookList_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BookList_TableMouseClicked(evt);
            }
        });
        BookList_JScrollPane.setViewportView(BookList_Table);
        BookList_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (BookList_Table.getColumnModel().getColumnCount() > 0) {
            BookList_Table.getColumnModel().getColumn(0).setResizable(false);
            BookList_Table.getColumnModel().getColumn(3).setResizable(false);
            BookList_Table.getColumnModel().getColumn(4).setResizable(false);
        }

        BookList_Panel.add(BookList_JScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 690, 350));

        jPanel17.add(BookList_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 40, 690, 350));

        StudentList_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        StudentList_Table.setBackground(new java.awt.Color(15, 28, 44));
        StudentList_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Student ID", "Student Name", "Section", "Strand", "Email", "Mobile No.", "Address"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        StudentList_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        StudentList_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        StudentList_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        StudentList_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        StudentList_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        StudentList_Table.setColumnSelectionAllowed(true);
        StudentList_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        StudentList_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        StudentList_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        StudentList_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        StudentList_Table.setGridColor(new java.awt.Color(15, 28, 44));
        StudentList_Table.setMultipleSeleccion(false);
        StudentList_Table.setRowHeight(25);
        StudentList_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        StudentList_Table.setShowGrid(false);
        StudentList_Table.getTableHeader().setReorderingAllowed(false);
        StudentList_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                StudentList_TableMouseClicked(evt);
            }
        });
        StudentList_JScrollPane.setViewportView(StudentList_Table);
        StudentList_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (StudentList_Table.getColumnModel().getColumnCount() > 0) {
            StudentList_Table.getColumnModel().getColumn(0).setResizable(false);
            StudentList_Table.getColumnModel().getColumn(2).setResizable(false);
            StudentList_Table.getColumnModel().getColumn(3).setResizable(false);
            StudentList_Table.getColumnModel().getColumn(4).setResizable(false);
            StudentList_Table.getColumnModel().getColumn(5).setResizable(false);
            StudentList_Table.getColumnModel().getColumn(6).setResizable(false);
        }

        StudentList_Panel.add(StudentList_JScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 690, 350));

        jPanel17.add(StudentList_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 40, 690, 350));

        IssuedList_Table.setBackground(new java.awt.Color(15, 28, 44));
        IssuedList_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Section", "Strand", "Student ID", "Email", "Mobile No.", "Address", "Issue Date", "Due Date", "BOOK ID", "BOOK TITLE", "QTY"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        IssuedList_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        IssuedList_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        IssuedList_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        IssuedList_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        IssuedList_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        IssuedList_Table.setColumnSelectionAllowed(true);
        IssuedList_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        IssuedList_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        IssuedList_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        IssuedList_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        IssuedList_Table.setGridColor(new java.awt.Color(15, 28, 44));
        IssuedList_Table.setMultipleSeleccion(false);
        IssuedList_Table.setRowHeight(25);
        IssuedList_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        IssuedList_Table.setShowGrid(false);
        IssuedList_Table.setShowHorizontalLines(true);
        IssuedList_Table.setShowVerticalLines(true);
        IssuedList_Table.getTableHeader().setReorderingAllowed(false);
        IssuedList_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                IssuedList_TableMouseClicked(evt);
            }
        });
        BorrowerList_JScrollPane.setViewportView(IssuedList_Table);
        IssuedList_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (IssuedList_Table.getColumnModel().getColumnCount() > 0) {
            IssuedList_Table.getColumnModel().getColumn(0).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(2).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(3).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(4).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(5).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(6).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(8).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(9).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(10).setResizable(false);
            IssuedList_Table.getColumnModel().getColumn(12).setResizable(false);
        }

        jPanel17.add(BorrowerList_JScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 400, 690, 240));

        Book_SearchBar.setBackground(new java.awt.Color(255, 255, 255));
        Book_SearchBar.setForeground(new java.awt.Color(255, 255, 255));
        Book_SearchBar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        issueBook_bookSearchBar.setBackground(new java.awt.Color(15, 28, 44));
        issueBook_bookSearchBar.setForeground(new java.awt.Color(255, 255, 255));
        issueBook_bookSearchBar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        issueBook_bookSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueBook_bookSearchBarKeyReleased(evt);
            }
        });
        Book_SearchBar.add(issueBook_bookSearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 190, 28));

        issueBook_bookCategory.setEditable(false);
        issueBook_bookCategory.setForeground(new java.awt.Color(255, 255, 255));
        issueBook_bookCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Book ID", "Book Title", "Author", "Section" }));
        issueBook_bookCategory.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        Book_SearchBar.add(issueBook_bookCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 0, 110, 30));

        jLabel7.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Search by - ");
        Book_SearchBar.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 70, 30));

        jPanel17.add(Book_SearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 10, -1, 30));

        Student_SearchBar.setBackground(new java.awt.Color(255, 255, 255));
        Student_SearchBar.setForeground(new java.awt.Color(255, 255, 255));
        Student_SearchBar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        issueBook_studentSearchBar.setBackground(new java.awt.Color(15, 28, 44));
        issueBook_studentSearchBar.setForeground(new java.awt.Color(255, 255, 255));
        issueBook_studentSearchBar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        issueBook_studentSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issueBook_studentSearchBarKeyReleased(evt);
            }
        });
        Student_SearchBar.add(issueBook_studentSearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, 190, 28));

        issueBook_studentCategory.setEditable(false);
        issueBook_studentCategory.setForeground(new java.awt.Color(255, 255, 255));
        issueBook_studentCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Student ID", "Student Name" }));
        issueBook_studentCategory.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        Student_SearchBar.add(issueBook_studentCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 0, 110, 30));

        jLabel5.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Search by - ");
        Student_SearchBar.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 70, 30));

        jPanel17.add(Student_SearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 10, -1, 30));

        IssueBooks_Panel.add(jPanel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 1020, -1));

        MainPanel.add(IssueBooks_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        Records_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Records_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel19MouseClicked(evt);
            }
        });
        jPanel19.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel20.setBackground(new java.awt.Color(15, 28, 44));
        jPanel20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel20MouseClicked(evt);
            }
        });
        jPanel20.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        IssuedList_Table2.setBackground(new java.awt.Color(15, 28, 44));
        IssuedList_Table2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        IssuedList_Table2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Student ID", "Issue Date", "Due Date", "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        IssuedList_Table2.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        IssuedList_Table2.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        IssuedList_Table2.setColorBordeHead(new java.awt.Color(15, 28, 44));
        IssuedList_Table2.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        IssuedList_Table2.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        IssuedList_Table2.setColumnSelectionAllowed(true);
        IssuedList_Table2.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        IssuedList_Table2.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        IssuedList_Table2.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        IssuedList_Table2.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        IssuedList_Table2.setGridColor(new java.awt.Color(15, 28, 44));
        IssuedList_Table2.setMultipleSeleccion(false);
        IssuedList_Table2.setRowHeight(25);
        IssuedList_Table2.setSelectionBackground(new java.awt.Color(15, 28, 44));
        IssuedList_Table2.setShowGrid(false);
        IssuedList_Table2.setShowHorizontalLines(true);
        IssuedList_Table2.setShowVerticalLines(true);
        IssuedList_Table2.getTableHeader().setReorderingAllowed(false);
        IssuedList_Table2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                IssuedList_Table2MouseClicked(evt);
            }
        });
        BorrowerList_JScrollPane1.setViewportView(IssuedList_Table2);
        IssuedList_Table2.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (IssuedList_Table2.getColumnModel().getColumnCount() > 0) {
            IssuedList_Table2.getColumnModel().getColumn(0).setResizable(false);
            IssuedList_Table2.getColumnModel().getColumn(2).setResizable(false);
            IssuedList_Table2.getColumnModel().getColumn(3).setResizable(false);
            IssuedList_Table2.getColumnModel().getColumn(4).setResizable(false);
            IssuedList_Table2.getColumnModel().getColumn(5).setResizable(false);
            IssuedList_Table2.getColumnModel().getColumn(7).setResizable(false);
            IssuedList_Table2.getColumnModel().getColumn(8).setResizable(false);
        }

        jPanel20.add(BorrowerList_JScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1020, 370));

        jLabel43.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(207, 76, 56));
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel43.setText("Records");
        jPanel20.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 60));

        jLabel12.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Student Detail");
        jPanel20.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 420, 140, 30));

        user_Category3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        user_Category3.setEditable(false);
        user_Category3.setForeground(new java.awt.Color(255, 255, 255));
        user_Category3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Name", "Student ID" }));
        jPanel20.add(user_Category3, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 10, 130, 30));

        txt_searchUser3.setBackground(new java.awt.Color(15, 28, 44));
        txt_searchUser3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_searchUser3.setForeground(new java.awt.Color(255, 255, 255));
        txt_searchUser3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_searchUser3KeyReleased(evt);
            }
        });
        jPanel20.add(txt_searchUser3, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 10, 260, 30));

        jLabel49.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel49.setText("Search by: ");
        jPanel20.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 10, 140, 30));

        tf_bookid.setBackground(new java.awt.Color(15, 28, 44));
        tf_bookid.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_bookid.setForeground(new java.awt.Color(255, 255, 255));
        tf_bookid.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_bookid.setPhColor(new java.awt.Color(255, 255, 255));
        tf_bookid.setPlaceholder("Book ID");
        jPanel20.add(tf_bookid, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 520, 140, 30));

        tf_id.setBackground(new java.awt.Color(15, 28, 44));
        tf_id.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_id.setForeground(new java.awt.Color(255, 255, 255));
        tf_id.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_id.setPhColor(new java.awt.Color(255, 255, 255));
        tf_id.setPlaceholder("ID");
        jPanel20.add(tf_id, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 470, 140, 30));

        tf_name.setBackground(new java.awt.Color(15, 28, 44));
        tf_name.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_name.setForeground(new java.awt.Color(255, 255, 255));
        tf_name.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_name.setPhColor(new java.awt.Color(255, 255, 255));
        tf_name.setPlaceholder("Name");
        tf_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_nameActionPerformed(evt);
            }
        });
        jPanel20.add(tf_name, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 470, 140, 30));

        tf_studentid.setBackground(new java.awt.Color(15, 28, 44));
        tf_studentid.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_studentid.setForeground(new java.awt.Color(255, 255, 255));
        tf_studentid.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_studentid.setPhColor(new java.awt.Color(255, 255, 255));
        tf_studentid.setPlaceholder("Insert Student ID");
        jPanel20.add(tf_studentid, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 570, 140, 30));

        tf_issuedate.setBackground(new java.awt.Color(15, 28, 44));
        tf_issuedate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_issuedate.setForeground(new java.awt.Color(255, 255, 255));
        tf_issuedate.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_issuedate.setPhColor(new java.awt.Color(255, 255, 255));
        tf_issuedate.setPlaceholder("Issue Date");
        jPanel20.add(tf_issuedate, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 570, 140, 30));

        tf_duedate.setBackground(new java.awt.Color(15, 28, 44));
        tf_duedate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_duedate.setForeground(new java.awt.Color(255, 255, 255));
        tf_duedate.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_duedate.setPhColor(new java.awt.Color(255, 255, 255));
        tf_duedate.setPlaceholder("Due Date");
        jPanel20.add(tf_duedate, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 570, 140, 30));

        tf_booktitle.setBackground(new java.awt.Color(15, 28, 44));
        tf_booktitle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_booktitle.setForeground(new java.awt.Color(255, 255, 255));
        tf_booktitle.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_booktitle.setPhColor(new java.awt.Color(255, 255, 255));
        tf_booktitle.setPlaceholder("Book Title");
        jPanel20.add(tf_booktitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 520, 140, 30));

        tf_qty.setBackground(new java.awt.Color(15, 28, 44));
        tf_qty.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_qty.setForeground(new java.awt.Color(255, 255, 255));
        tf_qty.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_qty.setPhColor(new java.awt.Color(255, 255, 255));
        tf_qty.setPlaceholder("Insert Quantity");
        jPanel20.add(tf_qty, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 470, 140, 30));

        tf_status.setBackground(new java.awt.Color(15, 28, 44));
        tf_status.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_status.setForeground(new java.awt.Color(255, 255, 255));
        tf_status.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tf_status.setPhColor(new java.awt.Color(255, 255, 255));
        tf_status.setPlaceholder("Status");
        jPanel20.add(tf_status, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 520, 140, 30));

        cancel_button.setBackground(new java.awt.Color(255, 255, 255));
        cancel_button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cancel_button.setForeground(new java.awt.Color(15, 28, 44));
        cancel_button.setText("Cancel");
        cancel_button.setColorHover(new java.awt.Color(207, 76, 56));
        cancel_button.setColorText(new java.awt.Color(15, 28, 44));
        cancel_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        cancel_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        cancel_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_buttonActionPerformed(evt);
            }
        });
        jPanel20.add(cancel_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 610, 70, 20));

        return_button.setBackground(new java.awt.Color(255, 255, 255));
        return_button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        return_button.setForeground(new java.awt.Color(15, 28, 44));
        return_button.setText("Return");
        return_button.setColorHover(new java.awt.Color(207, 76, 56));
        return_button.setColorText(new java.awt.Color(15, 28, 44));
        return_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        return_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        return_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                return_buttonActionPerformed(evt);
            }
        });
        jPanel20.add(return_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 610, 70, 20));

        records_search.setBackground(new java.awt.Color(255, 255, 255));
        records_search.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        records_search.setForeground(new java.awt.Color(15, 28, 44));
        records_search.setText("Search");
        records_search.setColorHover(new java.awt.Color(207, 76, 56));
        records_search.setColorText(new java.awt.Color(15, 28, 44));
        records_search.setColorTextHover(new java.awt.Color(15, 28, 44));
        records_search.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        records_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                records_searchActionPerformed(evt);
            }
        });
        jPanel20.add(records_search, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 610, 70, 20));

        jLabel60.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel60.setForeground(new java.awt.Color(255, 255, 255));
        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel60.setText("Quantity:");
        jPanel20.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 450, 80, 20));

        jLabel28.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel28.setText("Student Name:");
        jPanel20.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 450, 80, 20));

        jLabel61.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(255, 255, 255));
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel61.setText("Record ID");
        jPanel20.add(jLabel61, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 450, 80, 20));

        jLabel64.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel64.setForeground(new java.awt.Color(255, 255, 255));
        jLabel64.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel64.setText("Book Title");
        jPanel20.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 500, 80, 20));

        jLabel67.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel67.setForeground(new java.awt.Color(255, 255, 255));
        jLabel67.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel67.setText("Issue Date");
        jPanel20.add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 550, 80, 20));

        jLabel65.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel65.setForeground(new java.awt.Color(255, 255, 255));
        jLabel65.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel65.setText("Status");
        jPanel20.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 500, 80, 20));

        jLabel68.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel68.setForeground(new java.awt.Color(255, 255, 255));
        jLabel68.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel68.setText("Student ID");
        jPanel20.add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 550, 80, 20));

        jLabel66.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel66.setForeground(new java.awt.Color(255, 255, 255));
        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel66.setText("Due Date");
        jPanel20.add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 550, 80, 20));

        jLabel69.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel69.setForeground(new java.awt.Color(255, 255, 255));
        jLabel69.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel69.setText("Book ID");
        jPanel20.add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 500, 80, 20));

        jPanel19.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1030, 640));

        Records_Panel.add(jPanel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 1020, -1));

        MainPanel.add(Records_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        ReturnedBooks_Panel.setBackground(new java.awt.Color(204, 204, 204));
        ReturnedBooks_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel21.setBackground(new java.awt.Color(15, 28, 44));
        jPanel21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel21MouseClicked(evt);
            }
        });
        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel46.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(207, 76, 56));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel46.setText("Returned Books");
        jPanel21.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 220, 60));

        ReturnedBooks_Table.setBackground(new java.awt.Color(15, 28, 44));
        ReturnedBooks_Table.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        ReturnedBooks_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Student ID", "Returned Date", "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        ReturnedBooks_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        ReturnedBooks_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        ReturnedBooks_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        ReturnedBooks_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        ReturnedBooks_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        ReturnedBooks_Table.setColumnSelectionAllowed(true);
        ReturnedBooks_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        ReturnedBooks_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        ReturnedBooks_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        ReturnedBooks_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        ReturnedBooks_Table.setGridColor(new java.awt.Color(15, 28, 44));
        ReturnedBooks_Table.setMultipleSeleccion(false);
        ReturnedBooks_Table.setRowHeight(25);
        ReturnedBooks_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        ReturnedBooks_Table.setShowGrid(false);
        ReturnedBooks_Table.setShowHorizontalLines(true);
        ReturnedBooks_Table.setShowVerticalLines(true);
        ReturnedBooks_Table.getTableHeader().setReorderingAllowed(false);
        ReturnedBooks_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ReturnedBooks_TableMouseClicked(evt);
            }
        });
        BorrowerList_JScrollPane3.setViewportView(ReturnedBooks_Table);
        ReturnedBooks_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (ReturnedBooks_Table.getColumnModel().getColumnCount() > 0) {
            ReturnedBooks_Table.getColumnModel().getColumn(0).setResizable(false);
            ReturnedBooks_Table.getColumnModel().getColumn(2).setResizable(false);
            ReturnedBooks_Table.getColumnModel().getColumn(3).setResizable(false);
            ReturnedBooks_Table.getColumnModel().getColumn(4).setResizable(false);
            ReturnedBooks_Table.getColumnModel().getColumn(6).setResizable(false);
            ReturnedBooks_Table.getColumnModel().getColumn(7).setResizable(false);
        }

        jPanel21.add(BorrowerList_JScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1020, 600));

        jLabel16.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Search by - ");
        jPanel21.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 30, 100, 30));

        returnedbooksSearchBar.setBackground(new java.awt.Color(15, 28, 44));
        returnedbooksSearchBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        returnedbooksSearchBar.setForeground(new java.awt.Color(255, 255, 255));
        returnedbooksSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                returnedbooksSearchBarKeyReleased(evt);
            }
        });
        jPanel21.add(returnedbooksSearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 30, 150, 30));

        returnedBook_Category.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        returnedBook_Category.setEditable(false);
        returnedBook_Category.setForeground(new java.awt.Color(255, 255, 255));
        returnedBook_Category.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Name", "ID", "Student ID", "Returned Date", "Book ID", "Book Title" }));
        jPanel21.add(returnedBook_Category, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 30, 100, 30));

        ReturnedBooks_Panel.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 1020, 660));

        MainPanel.add(ReturnedBooks_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        Defaulter_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Defaulter_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel10.setBackground(new java.awt.Color(15, 28, 44));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        DefaulterList_Table.setBackground(new java.awt.Color(15, 28, 44));
        DefaulterList_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Name", "Student ID", "Issue Date", "Due Date", "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        DefaulterList_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        DefaulterList_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        DefaulterList_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        DefaulterList_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        DefaulterList_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        DefaulterList_Table.setColumnSelectionAllowed(true);
        DefaulterList_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        DefaulterList_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        DefaulterList_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        DefaulterList_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        DefaulterList_Table.setGridColor(new java.awt.Color(15, 28, 44));
        DefaulterList_Table.setMultipleSeleccion(false);
        DefaulterList_Table.setRowHeight(25);
        DefaulterList_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        DefaulterList_Table.setShowGrid(false);
        DefaulterList_Table.setShowHorizontalLines(true);
        DefaulterList_Table.setShowVerticalLines(true);
        DefaulterList_Table.getTableHeader().setReorderingAllowed(false);
        DefaulterList_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                DefaulterList_TableMouseClicked(evt);
            }
        });
        BorrowerList_JScrollPane2.setViewportView(DefaulterList_Table);
        DefaulterList_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (DefaulterList_Table.getColumnModel().getColumnCount() > 0) {
            DefaulterList_Table.getColumnModel().getColumn(0).setResizable(false);
            DefaulterList_Table.getColumnModel().getColumn(2).setResizable(false);
            DefaulterList_Table.getColumnModel().getColumn(3).setResizable(false);
            DefaulterList_Table.getColumnModel().getColumn(4).setResizable(false);
            DefaulterList_Table.getColumnModel().getColumn(5).setResizable(false);
            DefaulterList_Table.getColumnModel().getColumn(7).setResizable(false);
            DefaulterList_Table.getColumnModel().getColumn(8).setResizable(false);
        }

        jPanel10.add(BorrowerList_JScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 260, 1020, 400));

        jLabel56.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel56.setText("Search by: ");
        jPanel10.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 10, 90, 30));

        defaulterlist_Category.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        defaulterlist_Category.setEditable(false);
        defaulterlist_Category.setForeground(new java.awt.Color(255, 255, 255));
        defaulterlist_Category.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ID", "Name", "Student ID", "Issue Date", "Due Date", "Book ID", "Book Title" }));
        jPanel10.add(defaulterlist_Category, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 10, 110, 30));

        defaulterlistSearchBar.setBackground(new java.awt.Color(15, 28, 44));
        defaulterlistSearchBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        defaulterlistSearchBar.setForeground(new java.awt.Color(255, 255, 255));
        defaulterlistSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                defaulterlistSearchBarKeyReleased(evt);
            }
        });
        jPanel10.add(defaulterlistSearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 10, 150, 30));

        jPanel7.setBackground(new java.awt.Color(15, 28, 44));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txt_id.setBackground(new java.awt.Color(15, 28, 44));
        txt_id.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_id.setForeground(new java.awt.Color(255, 255, 255));
        txt_id.setPhColor(new java.awt.Color(255, 255, 255));
        txt_id.setPlaceholder("ID");
        txt_id.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_idKeyReleased(evt);
            }
        });
        jPanel7.add(txt_id, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 80, 160, 30));

        txt_issuedate.setEditable(false);
        txt_issuedate.setBackground(new java.awt.Color(15, 28, 44));
        txt_issuedate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_issuedate.setForeground(new java.awt.Color(255, 255, 255));
        txt_issuedate.setPhColor(new java.awt.Color(255, 255, 255));
        txt_issuedate.setPlaceholder("Issue Date");
        txt_issuedate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_issuedateKeyReleased(evt);
            }
        });
        jPanel7.add(txt_issuedate, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 130, 160, 30));

        txt_quantity2.setEditable(false);
        txt_quantity2.setBackground(new java.awt.Color(15, 28, 44));
        txt_quantity2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_quantity2.setForeground(new java.awt.Color(255, 255, 255));
        txt_quantity2.setPhColor(new java.awt.Color(255, 255, 255));
        txt_quantity2.setPlaceholder("Quantity");
        txt_quantity2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_quantity2KeyReleased(evt);
            }
        });
        jPanel7.add(txt_quantity2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 160, 30));

        txt_duedate.setEditable(false);
        txt_duedate.setBackground(new java.awt.Color(15, 28, 44));
        txt_duedate.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_duedate.setForeground(new java.awt.Color(255, 255, 255));
        txt_duedate.setPhColor(new java.awt.Color(255, 255, 255));
        txt_duedate.setPlaceholder("Due Date");
        txt_duedate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_duedateKeyReleased(evt);
            }
        });
        jPanel7.add(txt_duedate, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 130, 160, 30));

        txt_fullname.setEditable(false);
        txt_fullname.setBackground(new java.awt.Color(15, 28, 44));
        txt_fullname.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_fullname.setForeground(new java.awt.Color(255, 255, 255));
        txt_fullname.setPhColor(new java.awt.Color(255, 255, 255));
        txt_fullname.setPlaceholder("Name");
        txt_fullname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_fullnameKeyReleased(evt);
            }
        });
        jPanel7.add(txt_fullname, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 80, 160, 30));

        txt_status.setBackground(new java.awt.Color(15, 28, 44));
        txt_status.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_status.setForeground(new java.awt.Color(255, 255, 255));
        txt_status.setPhColor(new java.awt.Color(255, 255, 255));
        txt_status.setPlaceholder("Status");
        jPanel7.add(txt_status, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 180, 160, 30));

        txt_bookid.setEditable(false);
        txt_bookid.setBackground(new java.awt.Color(15, 28, 44));
        txt_bookid.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_bookid.setForeground(new java.awt.Color(255, 255, 255));
        txt_bookid.setPhColor(new java.awt.Color(255, 255, 255));
        txt_bookid.setPlaceholder("Book ID");
        txt_bookid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_bookidKeyReleased(evt);
            }
        });
        jPanel7.add(txt_bookid, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 130, 160, 30));

        txt_studentid2.setEditable(false);
        txt_studentid2.setBackground(new java.awt.Color(15, 28, 44));
        txt_studentid2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_studentid2.setForeground(new java.awt.Color(255, 255, 255));
        txt_studentid2.setPhColor(new java.awt.Color(255, 255, 255));
        txt_studentid2.setPlaceholder("Insert Student ID");
        txt_studentid2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_studentid2KeyReleased(evt);
            }
        });
        jPanel7.add(txt_studentid2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 180, 160, 30));

        defaulterList_search.setBackground(new java.awt.Color(255, 255, 255));
        defaulterList_search.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        defaulterList_search.setForeground(new java.awt.Color(15, 28, 44));
        defaulterList_search.setText("Search");
        defaulterList_search.setColorHover(new java.awt.Color(207, 76, 56));
        defaulterList_search.setColorText(new java.awt.Color(15, 28, 44));
        defaulterList_search.setColorTextHover(new java.awt.Color(15, 28, 44));
        defaulterList_search.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        defaulterList_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaulterList_searchActionPerformed(evt);
            }
        });
        jPanel7.add(defaulterList_search, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 220, 80, 20));

        txt_title.setEditable(false);
        txt_title.setBackground(new java.awt.Color(15, 28, 44));
        txt_title.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txt_title.setForeground(new java.awt.Color(255, 255, 255));
        txt_title.setPhColor(new java.awt.Color(255, 255, 255));
        txt_title.setPlaceholder("Book Title");
        txt_title.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_titleKeyReleased(evt);
            }
        });
        jPanel7.add(txt_title, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 180, 160, 30));

        LiftStatus_button.setBackground(new java.awt.Color(255, 255, 255));
        LiftStatus_button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        LiftStatus_button.setForeground(new java.awt.Color(15, 28, 44));
        LiftStatus_button.setText("Lift Status");
        LiftStatus_button.setColorHover(new java.awt.Color(207, 76, 56));
        LiftStatus_button.setColorText(new java.awt.Color(15, 28, 44));
        LiftStatus_button.setColorTextHover(new java.awt.Color(15, 28, 44));
        LiftStatus_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        LiftStatus_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LiftStatus_buttonActionPerformed(evt);
            }
        });
        jPanel7.add(LiftStatus_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 220, 80, 20));

        cancel_button1.setBackground(new java.awt.Color(255, 255, 255));
        cancel_button1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cancel_button1.setForeground(new java.awt.Color(15, 28, 44));
        cancel_button1.setText("Cancel");
        cancel_button1.setColorHover(new java.awt.Color(207, 76, 56));
        cancel_button1.setColorText(new java.awt.Color(15, 28, 44));
        cancel_button1.setColorTextHover(new java.awt.Color(15, 28, 44));
        cancel_button1.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        cancel_button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_button1ActionPerformed(evt);
            }
        });
        jPanel7.add(cancel_button1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 220, 80, 20));

        jLabel70.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel70.setForeground(new java.awt.Color(255, 255, 255));
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel70.setText("Quantity:");
        jPanel7.add(jLabel70, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 80, 20));

        jLabel59.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel59.setForeground(new java.awt.Color(255, 255, 255));
        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel59.setText("Student Name:");
        jPanel7.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 60, 80, 20));

        jLabel71.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel71.setForeground(new java.awt.Color(255, 255, 255));
        jLabel71.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel71.setText("Record ID");
        jPanel7.add(jLabel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 60, 80, 20));

        jLabel72.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel72.setForeground(new java.awt.Color(255, 255, 255));
        jLabel72.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel72.setText("Student Detail");
        jPanel7.add(jLabel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 10, 140, 30));

        jLabel73.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel73.setForeground(new java.awt.Color(255, 255, 255));
        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel73.setText("Issue Date");
        jPanel7.add(jLabel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 110, 80, 20));

        jLabel74.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel74.setForeground(new java.awt.Color(255, 255, 255));
        jLabel74.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel74.setText("Book Title");
        jPanel7.add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, 80, 20));

        jLabel75.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel75.setForeground(new java.awt.Color(255, 255, 255));
        jLabel75.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel75.setText("Due Date");
        jPanel7.add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 110, 80, 20));

        jLabel76.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel76.setForeground(new java.awt.Color(255, 255, 255));
        jLabel76.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel76.setText("Student ID");
        jPanel7.add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 160, 80, 20));

        jLabel77.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel77.setForeground(new java.awt.Color(255, 255, 255));
        jLabel77.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel77.setText("Status");
        jPanel7.add(jLabel77, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 160, 80, 20));

        jLabel78.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        jLabel78.setForeground(new java.awt.Color(255, 255, 255));
        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel78.setText("Book ID");
        jPanel7.add(jLabel78, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 110, 80, 20));

        jPanel10.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 670, 250));

        jLabel55.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(207, 76, 56));
        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel55.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel55.setText("Defaulter List");
        jPanel10.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 180, -1));

        Defaulter_Panel.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 1010, 660));

        MainPanel.add(Defaulter_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        Reports_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Reports_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel11.setBackground(new java.awt.Color(15, 28, 44));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Reports_Table.setBackground(new java.awt.Color(15, 28, 44));
        Reports_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Name", "Student ID", "Action"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        Reports_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        Reports_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        Reports_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        Reports_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        Reports_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        Reports_Table.setColumnSelectionAllowed(true);
        Reports_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        Reports_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        Reports_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        Reports_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        Reports_Table.setGridColor(new java.awt.Color(15, 28, 44));
        Reports_Table.setMultipleSeleccion(false);
        Reports_Table.setRowHeight(25);
        Reports_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        Reports_Table.setShowGrid(false);
        Reports_Table.setShowHorizontalLines(true);
        Reports_Table.setShowVerticalLines(true);
        Reports_Table.getTableHeader().setReorderingAllowed(false);
        Reports_Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Reports_TableMouseClicked(evt);
            }
        });
        BorrowerList_JScrollPane4.setViewportView(Reports_Table);
        Reports_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (Reports_Table.getColumnModel().getColumnCount() > 0) {
            Reports_Table.getColumnModel().getColumn(0).setResizable(false);
            Reports_Table.getColumnModel().getColumn(1).setResizable(false);
            Reports_Table.getColumnModel().getColumn(2).setResizable(false);
            Reports_Table.getColumnModel().getColumn(3).setResizable(false);
        }

        jPanel11.add(BorrowerList_JScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 1020, 580));

        jLabel57.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel57.setText("Search by: ");
        jPanel11.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 40, 90, 30));

        reports_Category.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        reports_Category.setEditable(false);
        reports_Category.setForeground(new java.awt.Color(255, 255, 255));
        reports_Category.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date", "Name", "Student ID" }));
        jPanel11.add(reports_Category, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 40, 110, 30));

        Reports_SearchBar.setBackground(new java.awt.Color(15, 28, 44));
        Reports_SearchBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        Reports_SearchBar.setForeground(new java.awt.Color(255, 255, 255));
        Reports_SearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Reports_SearchBarKeyReleased(evt);
            }
        });
        jPanel11.add(Reports_SearchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 40, 150, 30));

        jLabel58.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(207, 76, 56));
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel58.setText("Reports");
        jPanel11.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 180, 80));

        Reports_Panel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 1010, 660));

        MainPanel.add(Reports_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        SideTab.setBackground(new java.awt.Color(15, 28, 44));
        SideTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo.setFont(new java.awt.Font("Century Gothic", 1, 30)); // NOI18N
        logo.setForeground(new java.awt.Color(255, 255, 255));
        logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/book.png"))); // NOI18N
        logo.setText(" L M S");
        SideTab.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 230, 60));

        features.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        features.setForeground(new java.awt.Color(255, 255, 255));
        features.setText("Features");
        SideTab.add(features, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 60, 20));

        Settings_Panel.setBackground(new java.awt.Color(5, 21, 40));
        Settings_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Logout_button.setBackground(new java.awt.Color(5, 21, 40));
        Logout_button.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Logout_button.setForeground(new java.awt.Color(255, 255, 255));
        Logout_button.setText("Logout");
        Logout_button.setBorder(null);
        Logout_button.setContentAreaFilled(false);
        Logout_button.setOpaque(true);
        Logout_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Logout_buttonActionPerformed(evt);
            }
        });
        Settings_Panel.add(Logout_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 80, 30));

        SideTab.add(Settings_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 650, 80, 30));

        HomeTab.setBackground(new java.awt.Color(207, 76, 56));
        HomeTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Home_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Home_button.setForeground(new java.awt.Color(255, 255, 255));
        Home_button.setText(" Home");
        Home_button.setBorderPainted(false);
        Home_button.setContentAreaFilled(false);
        Home_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Home_buttonActionPerformed(evt);
            }
        });
        HomeTab.add(Home_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(HomeTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 200, 230, 40));

        ManageBooksTab.setBackground(new java.awt.Color(15, 28, 44));
        ManageBooksTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Managebooks_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Managebooks_button.setForeground(new java.awt.Color(255, 255, 255));
        Managebooks_button.setText("Manage Books");
        Managebooks_button.setBorderPainted(false);
        Managebooks_button.setContentAreaFilled(false);
        Managebooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Managebooks_buttonActionPerformed(evt);
            }
        });
        ManageBooksTab.add(Managebooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(ManageBooksTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 290, 230, 40));

        ManageUserTab.setBackground(new java.awt.Color(15, 28, 44));
        ManageUserTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        User_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        User_button.setForeground(new java.awt.Color(255, 255, 255));
        User_button.setText("Manage Users");
        User_button.setBorderPainted(false);
        User_button.setContentAreaFilled(false);
        User_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                User_buttonActionPerformed(evt);
            }
        });
        ManageUserTab.add(User_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(ManageUserTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 340, 230, 40));

        Records.setBackground(new java.awt.Color(15, 28, 44));
        Records.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Records_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Records_button.setForeground(new java.awt.Color(255, 255, 255));
        Records_button.setText("Records");
        Records_button.setBorderPainted(false);
        Records_button.setContentAreaFilled(false);
        Records_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Records_buttonActionPerformed(evt);
            }
        });
        Records.add(Records_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(Records, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 390, 230, 40));

        ReportsTab.setBackground(new java.awt.Color(15, 28, 44));
        ReportsTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Reports_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Reports_button.setForeground(new java.awt.Color(255, 255, 255));
        Reports_button.setText("Reports");
        Reports_button.setBorderPainted(false);
        Reports_button.setContentAreaFilled(false);
        Reports_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Reports_buttonActionPerformed(evt);
            }
        });
        ReportsTab.add(Reports_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(ReportsTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 440, 230, 40));

        ReturnedBooksTab.setBackground(new java.awt.Color(15, 28, 44));
        ReturnedBooksTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Returnbooks_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Returnbooks_button.setForeground(new java.awt.Color(255, 255, 255));
        Returnbooks_button.setText("Returned Books");
        Returnbooks_button.setBorderPainted(false);
        Returnbooks_button.setContentAreaFilled(false);
        Returnbooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Returnbooks_buttonActionPerformed(evt);
            }
        });
        ReturnedBooksTab.add(Returnbooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(ReturnedBooksTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 490, 230, 40));

        DefaulterTab.setBackground(new java.awt.Color(15, 28, 44));
        DefaulterTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Receipts_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Receipts_button.setForeground(new java.awt.Color(255, 255, 255));
        Receipts_button.setText("Defaulter List");
        Receipts_button.setBorderPainted(false);
        Receipts_button.setContentAreaFilled(false);
        Receipts_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Receipts_buttonActionPerformed(evt);
            }
        });
        DefaulterTab.add(Receipts_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(DefaulterTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 540, 230, 40));

        IssueBooksTab.setBackground(new java.awt.Color(15, 28, 44));
        IssueBooksTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Issuebooks_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Issuebooks_button.setForeground(new java.awt.Color(255, 255, 255));
        Issuebooks_button.setText("Issue Books");
        Issuebooks_button.setBorderPainted(false);
        Issuebooks_button.setContentAreaFilled(false);
        Issuebooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Issuebooks_buttonActionPerformed(evt);
            }
        });
        IssueBooksTab.add(Issuebooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(IssueBooksTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 590, 230, 40));

        SettingsTab.setBackground(new java.awt.Color(15, 28, 44));
        SettingsTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Settings_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Settings_button.setForeground(new java.awt.Color(255, 255, 255));
        Settings_button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/settings.png"))); // NOI18N
        Settings_button.setText(" Settings");
        Settings_button.setBorderPainted(false);
        Settings_button.setContentAreaFilled(false);
        Settings_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Settings_buttonActionPerformed(evt);
            }
        });
        SettingsTab.add(Settings_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 30));

        SideTab.add(SettingsTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 680, 230, 30));

        jLabel4.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Welcome!");
        SideTab.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 230, 30));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/administrator.png"))); // NOI18N
        SideTab.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 100, 90, 60));

        USERNAME.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        USERNAME.setForeground(new java.awt.Color(255, 255, 255));
        USERNAME.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        USERNAME.setText("ADMINISTRATOR");
        SideTab.add(USERNAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 230, 40));

        MainPanel.add(SideTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 720));

        getContentPane().add(MainPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    int pX, pY;
    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        pX = evt.getX();
        pY = evt.getY();
    }//GEN-LAST:event_formMousePressed

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        this.setLocation(this.getLocation().x + evt.getX() - pX,
        this.getLocation().y + evt.getY() - pY);
    }//GEN-LAST:event_formMouseDragged

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        this.requestFocus();
    }//GEN-LAST:event_formMouseClicked

    private void Home_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Home_buttonActionPerformed
    togglePanelVisibility(Home_Panel);
    HomeTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Home_buttonActionPerformed

    private void Managebooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Managebooks_buttonActionPerformed
    togglePanelVisibility(ManageBooks_Panel);
    ManageBooksTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Managebooks_buttonActionPerformed

    private void User_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_User_buttonActionPerformed
    togglePanelVisibility(ManageUser_Panel);
    ManageUserTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_User_buttonActionPerformed

    private void Issuebooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Issuebooks_buttonActionPerformed
    togglePanelVisibility(IssueBooks_Panel);
    setBookDetailsToTable();
    IssueBooksTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Issuebooks_buttonActionPerformed

    private void Records_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Records_buttonActionPerformed
    togglePanelVisibility(Records_Panel);
    Records.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Records_buttonActionPerformed

    private void Returnbooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Returnbooks_buttonActionPerformed
    togglePanelVisibility(ReturnedBooks_Panel);
    ReturnedBooksTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Returnbooks_buttonActionPerformed

    private void Receipts_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Receipts_buttonActionPerformed
    togglePanelVisibility(Defaulter_Panel);
    DefaulterTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Receipts_buttonActionPerformed

    private void minimizeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMousePressed
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_minimizeMousePressed

    private void closeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMousePressed
        System.exit(0);
    }//GEN-LAST:event_closeMousePressed

    private void Settings_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Settings_buttonActionPerformed
        if (Settings_Panel.isVisible()) {
          Settings_Panel.setVisible(false);
      } else {
          Settings_Panel.setVisible(true);
                }   
    }//GEN-LAST:event_Settings_buttonActionPerformed

    private void Logout_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Logout_buttonActionPerformed
    login_ui login = new login_ui();
    login.setVisible(true);
    dispose();
    }//GEN-LAST:event_Logout_buttonActionPerformed

    private void Add_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Add_buttonActionPerformed
        if (addBook() == true) {
            clearTextFields();
            setBookDetailsToTable();
            JOptionPane.showMessageDialog(this, "Book Added");
        }else {
                JOptionPane.showMessageDialog(this, "Book Addition Failed");
        }
    }//GEN-LAST:event_Add_buttonActionPerformed

    private void Update_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Update_buttonActionPerformed

    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the book?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);   
    if (result == JOptionPane.OK_OPTION) 
        {if (updateBook() == true) {
            clearTextFields();
            setBookDetailsToTable();
            insertDataToIssueBookTable();
            JOptionPane.showMessageDialog(this, "Book Updated");
        }else {
            JOptionPane.showMessageDialog(this, "Failed to Update the Book");
        }
    }
    }//GEN-LAST:event_Update_buttonActionPerformed

    private void Delete_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Delete_buttonActionPerformed
    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the book?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        if (deleteBook()) {
            setBookDetailsToTable();
            clearTextFields();
            JOptionPane.showMessageDialog(this, "Book Deleted");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to Delete the Book");
        }
    }
    }//GEN-LAST:event_Delete_buttonActionPerformed

    private void jPanel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseClicked
    BookDetails_Table.getSelectionModel().clearSelection();
    clearTextFields();
    }//GEN-LAST:event_jPanel4MouseClicked

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
    BookDetails_Table.getSelectionModel().clearSelection();
    }//GEN-LAST:event_jPanel1MouseClicked

    private void txt_searchBookKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchBookKeyReleased
    String searchString = txt_searchBook.getText();
    String selectedCategory = (String) txt_searchBy.getSelectedItem();
    searchBookInTable(selectedCategory, searchString, BookDetails_Table);
    }//GEN-LAST:event_txt_searchBookKeyReleased

    private void txt_searchUserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchUserKeyReleased
    String searchString = txt_searchUser.getText();
    String selectedCategory = (String) user_Category.getSelectedItem();
    searchUserDetails(selectedCategory, searchString, UserManager_Table);
    }//GEN-LAST:event_txt_searchUserKeyReleased

    private void UserManager_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_UserManager_TableMouseClicked
            int rowNo = UserManager_Table.getSelectedRow();
            TableModel model = UserManager_Table.getModel();

            txt_studentid.setText(model.getValueAt(rowNo, 0).toString());
            txt_studentname.setText(model.getValueAt(rowNo, 1).toString());
            txt_section.setText(model.getValueAt(rowNo, 2).toString());
            txt_strand.setText(model.getValueAt(rowNo, 3).toString());
            txt_email.setText(model.getValueAt(rowNo, 4).toString());
            txt_mobileNo.setText(model.getValueAt(rowNo, 5).toString());
            txt_address.setText(model.getValueAt(rowNo, 6).toString());
    }//GEN-LAST:event_UserManager_TableMouseClicked

    private void addStudent_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudent_buttonActionPerformed
    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to add the student?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        if (addStudent() == true) {
            clearUserTextField();
            setUserDetailsToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to Add the Student");
        }
    }
    }//GEN-LAST:event_addStudent_buttonActionPerformed

    private void Update_button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Update_button1ActionPerformed

    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to update the user?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);   
    if (result == JOptionPane.OK_OPTION) 
        {if (updateUser() == true) {
            clearUserTextField();
            setUserDetailsToTable();
            JOptionPane.showMessageDialog(this, "User  Updated");
        }else {
            JOptionPane.showMessageDialog(this, "Failed to Update the User");
        }
    }
    }//GEN-LAST:event_Update_button1ActionPerformed

    private void Delete_button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Delete_button1ActionPerformed
    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this user?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
    
    if (result == JOptionPane.OK_OPTION) {
        if (deleteUser()) {
            clearUserTextField();
            setUserDetailsToTable();
            JOptionPane.showMessageDialog(this, "User Deleted");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to Delete the User");
        }
    }
    }//GEN-LAST:event_Delete_button1ActionPerformed

    private void jPanel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2MouseClicked

    private void jPanel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel16MouseClicked
    UserManager_Table.getSelectionModel().clearSelection();
    clearTextFields();
    }//GEN-LAST:event_jPanel16MouseClicked

    private void txt_searchUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_searchUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_searchUserActionPerformed

    private void jPanel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel5MouseClicked

    private void jPanel17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel17MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel17MouseClicked

    private void IssuedList_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IssuedList_TableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_IssuedList_TableMouseClicked

    private void issueBook_studentSearchBarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueBook_studentSearchBarKeyReleased
        String selectedCategory = (String) issueBook_studentCategory.getSelectedItem();
        String searchString = issueBook_studentSearchBar.getText(); 
        searchIssueBooksStudentDetails(selectedCategory, searchString, StudentList_Table);
    }//GEN-LAST:event_issueBook_studentSearchBarKeyReleased

    private void Back_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Back_ButtonActionPerformed
            StudentSelection_Panel.setVisible(true);
            StudentList_Panel.setVisible(true);
            Student_SearchBar.setVisible(true);
            DefaultTableModel model = (DefaultTableModel) SelectedBook_Table.getModel();
            model.setRowCount(0);
            IssueDate.getEditor().setValue(null);
            DueDate.getEditor().setValue(null);
            BookSelection_Panel.setVisible(false);
            BookList_Panel.setVisible(false);
            Book_SearchBar.setVisible(false);
    }//GEN-LAST:event_Back_ButtonActionPerformed

    private void StudentList_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StudentList_TableMouseClicked
            int rowNo = StudentList_Table.getSelectedRow();
            TableModel model = StudentList_Table.getModel();
            
            tf_studentID.setText(model.getValueAt(rowNo, 0).toString());
            tf_studentName.setText(model.getValueAt(rowNo, 1).toString());
            tf_studentSection.setText(model.getValueAt(rowNo, 2).toString());
            tf_studentStrand.setText(model.getValueAt(rowNo, 3).toString());
            tf_studentEmail.setText(model.getValueAt(rowNo, 4).toString());
            tf_studentMobile.setText(model.getValueAt(rowNo, 5).toString());
            tf_studentAddress.setText(model.getValueAt(rowNo, 6).toString());

    }//GEN-LAST:event_StudentList_TableMouseClicked

    private void Proceed_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Proceed_ButtonActionPerformed
        DefaultTableModel selectedBookModel = (DefaultTableModel) SelectedBook_Table.getModel();

            if (selectedBookModel.getRowCount() > 0) {
                boolean isAdded = false;
                try {
                    isAdded = insertIssuedDetails();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "An error occurred while issuing books. Please try again.");
                }

                if (isAdded) {
                    insertIssuedBooksListTable();
                    addIssueBookReport();
                    selectedBookModel.setRowCount(0);
                    
                    StudentSelection_Panel.setVisible(true);
                    StudentList_Panel.setVisible(true);
                    Student_SearchBar.setVisible(true);
                    BookSelection_Panel.setVisible(false);
                    BookList_Panel.setVisible(false);
                    Book_SearchBar.setVisible(false);
                    clearTextFields();
                    JOptionPane.showMessageDialog(this, "Book Issued Successfully");
                } 
            } else {
                JOptionPane.showMessageDialog(this, "Please select at least one book.");
            }
    }//GEN-LAST:event_Proceed_ButtonActionPerformed

    
    
    private void BookList_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BookList_TableMouseClicked
            int rowNo = BookList_Table.getSelectedRow();
               TableModel model = BookList_Table.getModel();

               if (rowNo != -1) { // Check if a row is selected
                   // Retrieve data from the selected row
                   String bookID = model.getValueAt(rowNo, 0).toString(); // Assuming book ID is at index 0
                   String bookName = model.getValueAt(rowNo, 1).toString(); // Assuming book name is at index 1

                   DefaultTableModel selectedBookModel = (DefaultTableModel) SelectedBook_Table.getModel();

                   for (int i = 0; i < selectedBookModel.getRowCount(); i++) {
                       if (selectedBookModel.getValueAt(i, 1).equals(bookID)) {
                           int currentQty = Integer.parseInt(selectedBookModel.getValueAt(i, 2).toString());
                           selectedBookModel.setValueAt(currentQty + 1, i, 2);
                           return; // Exit the method after updating the quantity
                       }
                   }

                   // If the book ID is not found, add a new row with quantity 1
                   selectedBookModel.addRow(new Object[]{bookName, bookID, 1});
               }
    }//GEN-LAST:event_BookList_TableMouseClicked
  
    private boolean validateTextFields() {
        return !tf_studentID.getText().isEmpty() && !tf_studentName.getText().isEmpty() && !tf_studentSection.getText().isEmpty() && !tf_studentStrand.getText().isEmpty() && !tf_studentEmail.getText().isEmpty() && !tf_studentMobile.getText().isEmpty() && !tf_studentAddress.getText().isEmpty();
    }
    
    private void StudentPanel_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StudentPanel_ButtonActionPerformed
            if (validateTextFields()) {
                int option = JOptionPane.showConfirmDialog(this, "Confirm to Proceed", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    BookSelection_Panel.setVisible(true);
                    BookList_Panel.setVisible(true);
                    Book_SearchBar.setVisible(true);
                    StudentSelection_Panel.setVisible(false);
                    StudentList_Panel.setVisible(false);
                    Student_SearchBar.setVisible(false);
                } else {
                    // User canceled or closed the dialog, do nothing
                }
            } else {
                JOptionPane.showMessageDialog(this, "Can't Proceed with blank information");
            }
    }//GEN-LAST:event_StudentPanel_ButtonActionPerformed

    private void Delete_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Delete_ButtonActionPerformed
        DefaultTableModel selectedBookModel = (DefaultTableModel) SelectedBook_Table.getModel();
        selectedBookModel.setRowCount(0);
    }//GEN-LAST:event_Delete_ButtonActionPerformed

    private void SelectDelete_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectDelete_ButtonActionPerformed
        DefaultTableModel selectedBookModel = (DefaultTableModel) SelectedBook_Table.getModel();
        int[] selectedRows = SelectedBook_Table.getSelectedRows();

        // Iterate through the selected rows in reverse order to avoid index issues
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            selectedBookModel.removeRow(selectedRows[i]); // Remove the selected row
        }
    }//GEN-LAST:event_SelectDelete_ButtonActionPerformed

    private void Cancel_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Cancel_ButtonActionPerformed
        clearTextFields();
    }//GEN-LAST:event_Cancel_ButtonActionPerformed

    private void BookDetails_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BookDetails_TableMouseClicked
        int rowNo = BookDetails_Table.getSelectedRow();
        TableModel model = BookDetails_Table.getModel();
        
        txt_bookID.setText(model.getValueAt(rowNo, 0).toString());
        txt_booktitle.setText(model.getValueAt(rowNo, 1).toString());
        txt_bookauthor.setText(model.getValueAt(rowNo, 2).toString());
        txt_booksection.setSelectedItem(model.getValueAt(rowNo, 3).toString());
        txt_quantity.setText(model.getValueAt(rowNo, 4).toString());
        

    }//GEN-LAST:event_BookDetails_TableMouseClicked

    private void jPanel19MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel19MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel19MouseClicked

    private void IssuedList_Table2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IssuedList_Table2MouseClicked
    int rowNo = IssuedList_Table2.getSelectedRow();
    TableModel model = IssuedList_Table2.getModel();
    
    tf_id.setText(model.getValueAt(rowNo, 0).toString());
    tf_name.setText(model.getValueAt(rowNo, 1).toString());
    tf_studentid.setText(model.getValueAt(rowNo, 2).toString());
    tf_issuedate.setText(model.getValueAt(rowNo, 3).toString());
    tf_duedate.setText(model.getValueAt(rowNo, 4).toString());
    tf_bookid.setText(model.getValueAt(rowNo, 5).toString());
    tf_booktitle.setText(model.getValueAt(rowNo, 6).toString());
    tf_qty.setText(model.getValueAt(rowNo, 7).toString());
    tf_status.setText(model.getValueAt(rowNo, 8).toString());

    }//GEN-LAST:event_IssuedList_Table2MouseClicked

    private void jPanel20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel20MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel20MouseClicked

    private void txt_searchUser3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchUser3KeyReleased
    String selectedCategory = (String) user_Category3.getSelectedItem();
    String searchString = txt_searchUser3.getText(); 
    searchIssuedBooksTable(selectedCategory, searchString,IssuedList_Table2);
    }//GEN-LAST:event_txt_searchUser3KeyReleased

    private void DefaulterList_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DefaulterList_TableMouseClicked
    int rowNo = DefaulterList_Table.getSelectedRow();
    TableModel model = DefaulterList_Table.getModel();
    
    txt_id.setText(model.getValueAt(rowNo, 0).toString());
    txt_fullname.setText(model.getValueAt(rowNo, 1).toString());
    txt_studentid2.setText(model.getValueAt(rowNo, 2).toString());
    txt_issuedate.setText(model.getValueAt(rowNo, 3).toString());
    txt_duedate.setText(model.getValueAt(rowNo, 4).toString());
    txt_bookid.setText(model.getValueAt(rowNo, 5).toString());
    txt_title.setText(model.getValueAt(rowNo, 6).toString());
    txt_quantity2.setText(model.getValueAt(rowNo, 7).toString());
    txt_status.setText(model.getValueAt(rowNo, 8).toString());
    }//GEN-LAST:event_DefaulterList_TableMouseClicked

    private void returnedbooksSearchBarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_returnedbooksSearchBarKeyReleased
    String selectedCategory = (String) returnedBook_Category.getSelectedItem();
    String searchString = returnedbooksSearchBar.getText(); 
    returnedbooksSearchBar(selectedCategory, searchString,ReturnedBooks_Table);
    }//GEN-LAST:event_returnedbooksSearchBarKeyReleased

    private void ReturnedBooks_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ReturnedBooks_TableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_ReturnedBooks_TableMouseClicked

    private void jPanel21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel21MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel21MouseClicked

    private void cancel_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_buttonActionPerformed
    tf_id.setText("");
    tf_name.setText("");
    tf_studentid.setText("");
    tf_issuedate.setText("");
    tf_duedate.setText("");
    tf_bookid.setText("");
    tf_booktitle.setText("");
    tf_qty.setText("");
    tf_status.setText("");
    }//GEN-LAST:event_cancel_buttonActionPerformed

    private void return_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_return_buttonActionPerformed
        returnBook();
        fetchReturnedBooks();
        returnBookAndReport();
    }//GEN-LAST:event_return_buttonActionPerformed

    private void tf_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tf_nameActionPerformed

    private void records_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_records_searchActionPerformed
    getBorrowerDetails();
    }//GEN-LAST:event_records_searchActionPerformed

    private void defaulterlistSearchBarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_defaulterlistSearchBarKeyReleased
        String selectedCategory = (String) defaulterlist_Category.getSelectedItem();
        String searchString = defaulterlistSearchBar.getText();
        defaulterlistSearchBar(selectedCategory, searchString,DefaulterList_Table);
    }//GEN-LAST:event_defaulterlistSearchBarKeyReleased

    private void cancel_button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_button1ActionPerformed
        txt_id.setText("");
        txt_fullname.setText("");
        txt_studentid2.setText("");
        txt_issuedate.setText("");
        txt_duedate.setText("");
        txt_bookid.setText("");
        txt_title.setText("");
        txt_quantity2.setText("");
        txt_status.setText("");
    }//GEN-LAST:event_cancel_button1ActionPerformed

    private void LiftStatus_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LiftStatus_buttonActionPerformed
     liftStatus();

    }//GEN-LAST:event_LiftStatus_buttonActionPerformed

    private void txt_titleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_titleKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_titleKeyReleased

    private void txt_idKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_idKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_idKeyReleased

    private void txt_issuedateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_issuedateKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_issuedateKeyReleased

    private void txt_quantity2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_quantity2KeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_quantity2KeyReleased

    private void txt_duedateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_duedateKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_duedateKeyReleased

    private void txt_fullnameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_fullnameKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_fullnameKeyReleased

    private void txt_bookidKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_bookidKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_bookidKeyReleased

    private void txt_studentid2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_studentid2KeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_studentid2KeyReleased

    private void defaulterList_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaulterList_searchActionPerformed
    getDefaulterDetails();
    }//GEN-LAST:event_defaulterList_searchActionPerformed

    private void Reports_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Reports_buttonActionPerformed
    togglePanelVisibility(Reports_Panel);
    ReportsTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Reports_buttonActionPerformed

    private void Reports_SearchBarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Reports_SearchBarKeyReleased
        String selectedCategory = (String) reports_Category.getSelectedItem();
        String searchString = Reports_SearchBar.getText();
        ReportsSearchFunction(selectedCategory, searchString, Reports_Table);
    }//GEN-LAST:event_Reports_SearchBarKeyReleased

    private void Reports_TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Reports_TableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_Reports_TableMouseClicked

    private void issueBook_bookSearchBarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issueBook_bookSearchBarKeyReleased
        String selectedCategory = (String) issueBook_bookCategory.getSelectedItem();
        String searchString = issueBook_bookSearchBar.getText(); 
        searchIssueBooksDetails(selectedCategory, searchString, BookList_Table);
    }//GEN-LAST:event_issueBook_bookSearchBarKeyReleased

    private void Delete_button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Delete_button2ActionPerformed
        txt_studentid.setText("");
        txt_studentname.setText("");
        txt_section.setText("");
        txt_strand.setText("");
        txt_email.setText("");
        txt_mobileNo.setText("");
        txt_address.setText("");
    }//GEN-LAST:event_Delete_button2ActionPerformed

    private void clearTextFields() {
        txt_bookID.setText("");
        txt_booktitle.setText("");
        txt_bookauthor.setText("");
        txt_quantity.setText("");
        tf_studentID.setText("");
        tf_studentName.setText("");
        tf_studentSection.setText("");
        tf_studentStrand.setText("");
        tf_studentEmail.setText("");
        tf_studentMobile.setText("");
        tf_studentAddress.setText("");
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Admin_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Admin_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Admin_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Admin_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Admin_UI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rojeru_san.complementos.RSButtonHover Add_button;
    private javax.swing.JLabel Address;
    private rojeru_san.complementos.RSButtonHover Back_Button;
    private rojeru_san.complementos.RSTableMetro BookDetails_Table;
    private javax.swing.JScrollPane BookList_JScrollPane;
    private javax.swing.JPanel BookList_Panel;
    private rojeru_san.complementos.RSTableMetro BookList_Table;
    private javax.swing.JPanel BookSelection_Panel;
    private javax.swing.JPanel Book_SearchBar;
    private javax.swing.JScrollPane BorrowerList_JScrollPane;
    private javax.swing.JScrollPane BorrowerList_JScrollPane1;
    private javax.swing.JScrollPane BorrowerList_JScrollPane2;
    private javax.swing.JScrollPane BorrowerList_JScrollPane3;
    private javax.swing.JScrollPane BorrowerList_JScrollPane4;
    private rojeru_san.complementos.RSButtonHover Cancel_Button;
    private javax.swing.JLabel DashBoard_PendingCount;
    private javax.swing.JLabel Dashboard_AvailableBooks;
    private rojeru_san.complementos.RSTableMetro Dashboard_BookDetails;
    private javax.swing.JLabel Dashboard_BooksCount;
    private javax.swing.JLabel Dashboard_StudentCount;
    private rojeru_san.complementos.RSTableMetro Dashboard_StudentDetails;
    private javax.swing.JLabel Date;
    private rojeru_san.complementos.RSTableMetro DefaulterList_Table;
    private javax.swing.JPanel DefaulterTab;
    private javax.swing.JPanel Defaulter_Panel;
    private rojeru_san.complementos.RSButtonHover Delete_Button;
    private rojeru_san.complementos.RSButtonHover Delete_button;
    private rojeru_san.complementos.RSButtonHover Delete_button1;
    private rojeru_san.complementos.RSButtonHover Delete_button2;
    private org.jdesktop.swingx.JXDatePicker DueDate;
    private javax.swing.JPanel HomeTab;
    private javax.swing.JPanel Home_Panel;
    private javax.swing.JButton Home_button;
    private javax.swing.JPanel IssueBooksTab;
    private javax.swing.JPanel IssueBooks_Panel;
    private org.jdesktop.swingx.JXDatePicker IssueDate;
    private javax.swing.JButton Issuebooks_button;
    private rojeru_san.complementos.RSTableMetro IssuedList_Table;
    private rojeru_san.complementos.RSTableMetro IssuedList_Table2;
    private rojeru_san.complementos.RSButtonHover LiftStatus_button;
    private javax.swing.JButton Logout_button;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JPanel ManageBooksTab;
    private javax.swing.JPanel ManageBooks_Panel;
    private javax.swing.JPanel ManageUserTab;
    private javax.swing.JPanel ManageUser_Panel;
    private javax.swing.JButton Managebooks_button;
    private rojeru_san.complementos.RSButtonHover Proceed_Button;
    private javax.swing.JButton Receipts_button;
    private javax.swing.JPanel Records;
    private javax.swing.JPanel Records_Panel;
    private javax.swing.JButton Records_button;
    private javax.swing.JPanel ReportsTab;
    private javax.swing.JPanel Reports_Panel;
    private app.bolivia.swing.JCTextField Reports_SearchBar;
    private rojeru_san.complementos.RSTableMetro Reports_Table;
    private javax.swing.JButton Reports_button;
    private javax.swing.JButton Returnbooks_button;
    private javax.swing.JPanel ReturnedBooksTab;
    private javax.swing.JPanel ReturnedBooks_Panel;
    private rojeru_san.complementos.RSTableMetro ReturnedBooks_Table;
    private rojeru_san.complementos.RSButtonHover SelectDelete_Button;
    private rojeru_san.complementos.RSTableMetro SelectedBook_Table;
    private javax.swing.JPanel SettingsTab;
    private javax.swing.JPanel Settings_Panel;
    private javax.swing.JButton Settings_button;
    private javax.swing.JPanel SideTab;
    private javax.swing.JScrollPane StudentList_JScrollPane;
    private javax.swing.JPanel StudentList_Panel;
    private rojeru_san.complementos.RSTableMetro StudentList_Table;
    private rojeru_san.complementos.RSButtonHover StudentPanel_Button;
    private javax.swing.JPanel StudentSelection_Panel;
    private javax.swing.JPanel Student_SearchBar;
    private javax.swing.JLabel Time;
    public static javax.swing.JLabel USERNAME;
    private rojeru_san.complementos.RSButtonHover Update_button;
    private rojeru_san.complementos.RSButtonHover Update_button1;
    private rojeru_san.complementos.RSTableMetro UserManager_Table;
    private javax.swing.JButton User_button;
    private rojeru_san.complementos.RSButtonHover addStudent_button;
    private rojeru_san.complementos.RSButtonHover cancel_button;
    private rojeru_san.complementos.RSButtonHover cancel_button1;
    private javax.swing.JLabel close;
    private rojeru_san.complementos.RSButtonHover defaulterList_search;
    private app.bolivia.swing.JCTextField defaulterlistSearchBar;
    private lms.ComboBoxSuggestion defaulterlist_Category;
    private javax.swing.JLabel features;
    private lms.ComboBoxSuggestion issueBook_bookCategory;
    private app.bolivia.swing.JCTextField issueBook_bookSearchBar;
    private lms.ComboBoxSuggestion issueBook_studentCategory;
    private app.bolivia.swing.JCTextField issueBook_studentSearchBar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel logo;
    private javax.swing.JLabel minimize;
    private rojeru_san.complementos.RSButtonHover records_search;
    private lms.ComboBoxSuggestion reports_Category;
    private rojeru_san.complementos.RSButtonHover return_button;
    private lms.ComboBoxSuggestion returnedBook_Category;
    private app.bolivia.swing.JCTextField returnedbooksSearchBar;
    private app.bolivia.swing.JCTextField tf_bookid;
    private app.bolivia.swing.JCTextField tf_booktitle;
    private app.bolivia.swing.JCTextField tf_duedate;
    private app.bolivia.swing.JCTextField tf_id;
    private app.bolivia.swing.JCTextField tf_issuedate;
    private app.bolivia.swing.JCTextField tf_name;
    private app.bolivia.swing.JCTextField tf_qty;
    private app.bolivia.swing.JCTextField tf_status;
    private app.bolivia.swing.JCTextField tf_studentAddress;
    private app.bolivia.swing.JCTextField tf_studentEmail;
    private app.bolivia.swing.JCTextField tf_studentID;
    private app.bolivia.swing.JCTextField tf_studentMobile;
    private app.bolivia.swing.JCTextField tf_studentName;
    private app.bolivia.swing.JCTextField tf_studentSection;
    private app.bolivia.swing.JCTextField tf_studentStrand;
    private app.bolivia.swing.JCTextField tf_studentid;
    private app.bolivia.swing.JCTextField txt_address;
    private app.bolivia.swing.JCTextField txt_bookID;
    private app.bolivia.swing.JCTextField txt_bookauthor;
    private app.bolivia.swing.JCTextField txt_bookid;
    private javax.swing.JComboBox<String> txt_booksection;
    private app.bolivia.swing.JCTextField txt_booktitle;
    private app.bolivia.swing.JCTextField txt_duedate;
    private app.bolivia.swing.JCTextField txt_email;
    private app.bolivia.swing.JCTextField txt_fullname;
    private app.bolivia.swing.JCTextField txt_id;
    private app.bolivia.swing.JCTextField txt_issuedate;
    private app.bolivia.swing.JCTextField txt_mobileNo;
    private app.bolivia.swing.JCTextField txt_quantity;
    private app.bolivia.swing.JCTextField txt_quantity2;
    private app.bolivia.swing.JCTextField txt_searchBook;
    private lms.ComboBoxSuggestion txt_searchBy;
    private app.bolivia.swing.JCTextField txt_searchUser;
    private app.bolivia.swing.JCTextField txt_searchUser3;
    private app.bolivia.swing.JCTextField txt_section;
    private app.bolivia.swing.JCTextField txt_status;
    private app.bolivia.swing.JCTextField txt_strand;
    private app.bolivia.swing.JCTextField txt_studentid;
    private app.bolivia.swing.JCTextField txt_studentid2;
    private app.bolivia.swing.JCTextField txt_studentname;
    private app.bolivia.swing.JCTextField txt_title;
    private lms.ComboBoxSuggestion user_Category;
    private lms.ComboBoxSuggestion user_Category3;
    // End of variables declaration//GEN-END:variables
}
