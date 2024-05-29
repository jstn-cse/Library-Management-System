
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.table.TableRowSorter;
import java.sql.SQLException;


public class User_UI extends javax.swing.JFrame {
    
    private JButton selectedButton = null;
    
    
    
        
    public User_UI() {
        initComponents();
    }
    
public User_UI(String fullName, String studentID, String role, String contactNo, String address, String strand, String section) {
        // Initialize components
        initComponents();
          setTime();
          
        Dashboard_BookDetails.getColumnModel().getColumn(0).setPreferredWidth(5);
        Dashboard_BookDetails.getColumnModel().getColumn(1).setPreferredWidth(200);
        Dashboard_BookDetails.getColumnModel().getColumn(3).setPreferredWidth(25);
        Dashboard_BookDetails.getColumnModel().getColumn(4).setPreferredWidth(5);
        Dashboard_BookDetails.getColumnModel().getColumn(5).setPreferredWidth(25);

        bookInformation_Panel.setVisible(false);
        viewBookStatus_Panel.setVisible(false);
        History_Panel.setVisible(false);
        Account_Panel.setVisible(false);
        Settings_Panel.setVisible(false);
        passwordSettings_Panel.setVisible(false);
        returnedBooks_ScrollPane.setVisible(false);
        overdueBooks_ScrollPane.setVisible(false);
        


         int delay = 500; // Refresh interval in milliseconds (e.g., 1000 ms = 1 second)
         Timer refreshTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method to refresh the table
                displayUserCount();
                dashboardBookDetails();
                String studentID = studentID_ph.getText();
                displayStudentRecords(studentID);
                displayBorrowedBooks(studentID);
                displayOverdueBooks(studentID);
             }
         });
          refreshTimer.start();

        // Set the data to the respective placeholders
        USERNAME.setText(fullName);
        studentName_ph.setText(fullName);
        studentID_ph.setText(studentID);
        studentRole_ph.setText(role);

        tf_name.setText(fullName);
        tf_contactno.setText(contactNo);
        tf_studentid.setText(studentID);
        tf_address.setText(address);
        tf_strand.setText(strand);
        tf_section.setText(section);

        // Make the text fields uneditable
        tf_name.setEditable(false);
        tf_contactno.setEditable(false);
        tf_studentid.setEditable(false);
        tf_address.setEditable(false);
        tf_strand.setEditable(false);
        tf_section.setEditable(false);
    }
 
//--------------------------------------------------Basically a Clock--------------------------------------------------//
    public void setTime() {
            new Thread (new Runnable() {
                @Override
                public void run() {
                   while (true){
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(User_UI.class.getName()).log(Level.SEVERE, null, ex);
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
    
//-------------------------Change the color of Tabs inside the SideBar------------------------------//
             private void resetTabColors() {
                HomeTab.setBackground(new Color(15,28,44));
                bookInformation_Tab.setBackground(new Color(15,28,44));
                studentInformation_Tab.setBackground(new Color(15,28,44));
                Records_Tab.setBackground(new Color(15,28,44));
            }
     
    
//--------------------------------------------------For switching panels--------------------------------------------------//
        private void togglePanelVisibility(JPanel panelToShow) {
        resetTabColors();

        Home_Panel.setVisible(false);
        viewBookStatus_Panel.setVisible(false);
        bookInformation_Panel.setVisible(false);
        History_Panel.setVisible(false);
        Account_Panel.setVisible(false);

        panelToShow.setVisible(true);
    }
        
        
        
//----------------------------------------------------[START OF DASH BOARD SECTION]----------------------------------------------------//
        
        public void displayUserCount() {
            try {
                // Establish connection
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

                // Create statement
                Statement stmt = con.createStatement();

                // Execute query to count users
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS userCount FROM users WHERE role = 'user'");

                // Check if result set has data
                if (rs.next()) {
                    // Get user count from result set
                    int userCount = rs.getInt("userCount");

                    // Update JLabel with user count
                    userDashBoard_TotalUsers.setText(Integer.toString(userCount));
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
        
//----------------------------------------------------[END OF DASH BOARD SECTION]----------------------------------------------------//
//----------------------------------------------------[START OF BOOK INFORMATION SECTION]----------------------------------------------------//
        
        //-------------------------- Insert Book Details in DASHBOARD -------------------------- //
            public void dashboardBookDetails() {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM book_details");
                    DefaultTableModel bookInformationTable = (DefaultTableModel) bookInformation_Table.getModel();
                    DefaultTableModel dashboardBookDetails = (DefaultTableModel) Dashboard_BookDetails.getModel();
                    bookInformationTable.setRowCount(0);
                    dashboardBookDetails.setRowCount(0);
                    int availableBookCount = 0;
                    while (rs.next()) {
                        String bookID = rs.getString("id");
                        String bookTitle = rs.getString("book_title");
                        String bookAuthor = rs.getString("author");
                        String bookSection = rs.getString("section");
                        String bookQuantity = rs.getString("quantity");
                        String bookStatus = rs.getString("status");

                        Object[] userManagerRow = {bookID, bookTitle, bookAuthor, bookSection, bookQuantity, bookStatus};
                        dashboardBookDetails.addRow(userManagerRow);
                        bookInformationTable.addRow(userManagerRow);
                        if ("Available".equalsIgnoreCase(bookStatus)) {
                            availableBookCount++;
                        }
                    }
                    Dashboard_BooksCount.setText(String.valueOf(availableBookCount));
                    rs.close();
                    st.close();
                } catch (Exception e) {
                    e.printStackTrace(); // Handle exceptions appropriately
                }
            }
            
        private void filterBooksByCategory(String category) {
            DefaultTableModel model = (DefaultTableModel) bookInformation_Table.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            bookInformation_Table.setRowSorter(sorter);

            if (category.equalsIgnoreCase("All")) {
                sorter.setRowFilter(null); // Show all rows
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + category, 3)); // Assumes category is in column index 3
            }
        }
        
        private void handleCategoryButtonSelection(JButton button, String category) {
            if (selectedButton != null && selectedButton != button) {
                selectedButton.setBackground(new Color(15,28,44)); // Original color
            }

            if (selectedButton == button && selectedButton.getBackground().equals(new Color(207, 76, 56))) {
                button.setBackground(new Color(15,28,44)); // Deselect and reset color
                filterBooksByCategory("All");
                selectedButton = null;
            } else {
                button.setBackground(new Color(207, 76, 56)); // Selected color
                filterBooksByCategory(category);
                selectedButton = button;
            }
        }
        
            // Search function for DEFAULTER LIST Table
    public void bookInfoSearchBar(String selectedCategory, String searchString, JTable bookInformation_Table) {
        DefaultTableModel model = (DefaultTableModel) bookInformation_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        bookInformation_Table.setRowSorter(trs);

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
            default:
                trs.setRowFilter(null);
                break;
        }
    }

//----------------------------------------------------[END OF BOOK INFORMATION SECTION]----------------------------------------------------//
//----------------------------------------------------[START  OF ACCOUNT DETAILS SECTION]----------------------------------------------------//
    
        public void displayBorrowedBooks(String studentID) {
            try {
                // Establish connection
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

                // Query to get the borrowed books for the given student ID with "Pending" status
                String query = "SELECT book_id, book_title, issue_date, due_date, qty, status FROM issuedbooks WHERE student_id = ? AND status = 'Pending'";
                try (PreparedStatement pst = con.prepareStatement(query)) {
                    pst.setString(1, studentID);

                    // Execute query
                    try (ResultSet rs = pst.executeQuery()) {
                        DefaultTableModel borrowedTableModel = (DefaultTableModel) borrowed_Table.getModel();
                        borrowedTableModel.setRowCount(0); // Clear any existing rows
                        int borrowedBookCount = 0;

                        while (rs.next()) {
                            String bookID = rs.getString("book_id");
                            String issueDate = rs.getString("issue_date");
                            String bookTitle = rs.getString("book_title");
                            int quantity = rs.getInt("qty");
                            String status = rs.getString("status");

                            // Add the row to the borrowed_Table
                            Object[] row = {bookID, issueDate, bookTitle, quantity, status};
                            borrowedTableModel.addRow(row);
                            borrowedBookCount++;
                        }

                        // Update the book count label
                        book_Count.setText(String.valueOf(borrowedBookCount));
                        DashBoard_BorrowedCount.setText(String.valueOf(borrowedBookCount));
                    }
                }

                // Close connection
                con.close();
            } catch (SQLException e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
        
        public void displayOverdueBooks(String studentID) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

                // Query to get the overdue books for the given student ID
                String query = "SELECT book_id, book_title, issue_date, due_date, qty, status FROM defaulterlist WHERE student_id = ? AND status = 'over due'";
                try (PreparedStatement pst = con.prepareStatement(query)) {
                    pst.setString(1, studentID);

                    try (ResultSet rs = pst.executeQuery()) {
                        DefaultTableModel overdueTableModel = (DefaultTableModel) overDue_table.getModel();
                        overdueTableModel.setRowCount(0); // Clear any existing rows
                        int overdueBookCount = 0;

                        while (rs.next()) {
                            String bookID = rs.getString("book_id");
                            String issueDate = rs.getString("issue_date");
                            String bookTitle = rs.getString("book_title");
                            int quantity = rs.getInt("qty");
                            String status = rs.getString("status");

                            // Add the row to the overDue_table
                            Object[] row = {bookID, bookTitle, issueDate, quantity, status};
                            overdueTableModel.addRow(row);
                            overdueBookCount++;
                        }

                        // Update the overdue book count label
                        Dashboard_StudentCount3.setText(String.valueOf(overdueBookCount));
                        Dashboard_OverdueCount.setText(String.valueOf(overdueBookCount));
                    }
                }

                con.close();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
    
//----------------------------------------------------[START OF STUDENT RECORD SECTION]----------------------------------------------------//
        
            // Search function for DEFAULTER LIST Table
    public void searchStudentRecordTable(String selectedCategory, String searchString, JTable studentRecord_Table) {
        DefaultTableModel model = (DefaultTableModel) studentRecord_Table.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        studentRecord_Table.setRowSorter(trs);

        switch (selectedCategory) {
            case "Book ID":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 0));
                break;
            case "Book Title":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 1));
                break;
            case "Issue Date":
                trs.setRowFilter(RowFilter.regexFilter("(?i)" + searchString, 2));
                break;
            default:
                trs.setRowFilter(null);
                break;
        }
    }
    
        public void displayStudentRecords(String studentID) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

                // Query to get the issued books for the given student ID
                String issuedBooksQuery = "SELECT student_name, student_id, issue_date, due_date, book_id, book_title, qty, status FROM issuedbooks WHERE student_id = ?";
                // Query to get the overdue books for the given student ID
                String defaulterListQuery = "SELECT student_name, student_id, issue_date, due_date, book_id, book_title, qty, status FROM defaulterlist WHERE student_id = ?";
                String returnedBooksQuery = "SELECT student_name, student_id, returned_date, book_id, book_title, quantity, status FROM returnedbooks WHERE student_id = ?";
                // Add more queries as needed for additional tables

                DefaultTableModel studentRecordTableModel = (DefaultTableModel) studentRecord_Table.getModel();
                DefaultTableModel returnedBooksTableModel = (DefaultTableModel) returnedBooks_Table.getModel();
                DefaultTableModel overdueBooksTableModel = (DefaultTableModel) overdueBooks_Table.getModel();
                studentRecordTableModel.setRowCount(0); // Clear any existing rows
                returnedBooksTableModel.setRowCount(0); // Clear any existing rows
                overdueBooksTableModel.setRowCount(0); // Clear any existing rows

                // Fetch and combine data from issuedbooks
                try (PreparedStatement pst = con.prepareStatement(issuedBooksQuery)) {
                    pst.setString(1, studentID);
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            String studentName = rs.getString("student_name");
                            String studentId = rs.getString("student_id");
                            String issueDate = rs.getString("issue_date");
                            String dueDate = rs.getString("due_date");
                            String bookID = rs.getString("book_id");
                            String bookTitle = rs.getString("book_title");
                            int quantity = rs.getInt("qty");
                            String status = rs.getString("status");

                            Object[] row = {studentName, studentId, issueDate, dueDate, bookID, bookTitle, quantity, status};
                            studentRecordTableModel.addRow(row);
                        }
                    }
                }

                // Fetch and combine data from defaulterlist
                try (PreparedStatement pst = con.prepareStatement(defaulterListQuery)) {
                    pst.setString(1, studentID);
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            String studentName = rs.getString("student_name");
                            String studentId = rs.getString("student_id");
                            String issueDate = rs.getString("issue_date");
                            String dueDate = rs.getString("due_date");
                            String bookID = rs.getString("book_id");
                            String bookTitle = rs.getString("book_title");
                            int quantity = rs.getInt("qty");
                            String status = rs.getString("status");

                            Object[] row = {studentName, studentId, issueDate, dueDate, bookID, bookTitle, quantity, status};
                            overdueBooksTableModel.addRow(row);
                        }
                    }
                }
                
                // Fetch and combine data from returnedbooks
                try (PreparedStatement pst = con.prepareStatement(returnedBooksQuery)) {
                    pst.setString(1, studentID);
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            String studentName = rs.getString("student_name");
                            String studentId = rs.getString("student_id");
                            String returnedDate = rs.getString("returned_date");
                            String bookID = rs.getString("book_id");
                            String bookTitle = rs.getString("book_title");
                            int quantity = rs.getInt("quantity");
                            String status = rs.getString("status");

                            Object[] row = {studentName, studentId, returnedDate, bookID, bookTitle, quantity, status};
                            returnedBooksTableModel.addRow(row);
                        }
                    }
                }

                con.close();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
        
//----------------------------------------------------[START OF STUDENT RECORD SECTION]----------------------------------------------------//
//----------------------------------------------------[START OF STUDENT ACCOUNT INFORMATION SECTION]----------------------------------------------------//
        //Update Password Method
        private void updatePassword(String studentID) {
            String currentPassword = tf_currentpass.getText();
            String newPassword = tf_newpass.getText();
            String confirmPassword = tf_confirmpass.getText();

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New password and confirm password do not match.");
                return;
            }

            String query = "UPDATE users SET password = ? WHERE student_no = ? OR password = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {

                pst.setString(1, newPassword);
                pst.setString(2, studentID);
                pst.setString(3, currentPassword);

                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Password updated successfully.");
                    // Optionally, clear the password fields after a successful update
                    tf_currentpass.setText("");
                    tf_newpass.setText("");
                    tf_confirmpass.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating password.");
            }
        }
    
//----------------------------------------------------[END  OF STUDENT ACCOUNT INFORMATION SECTION]----------------------------------------------------//

    
    
        
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
        userDashBoard_TotalUsers = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        DashBoard_BorrowedCount = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        Dashboard_BooksCount = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        Dashboard_OverdueCount = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Dashboard_BookDetails = new rojeru_san.complementos.RSTableMetro();
        Time = new javax.swing.JLabel();
        Date = new javax.swing.JLabel();
        bookInformation_Panel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        bookAvailable_checkbox = new javax.swing.JCheckBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        bookInformation_Table = new rojeru_san.complementos.RSTableMetro();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        goback_button = new javax.swing.JButton();
        ict_button = new javax.swing.JButton();
        english_button = new javax.swing.JButton();
        research_button = new javax.swing.JButton();
        abm_button = new javax.swing.JButton();
        math_button = new javax.swing.JButton();
        psychology_button = new javax.swing.JButton();
        humss_button = new javax.swing.JButton();
        law_button = new javax.swing.JButton();
        science_button = new javax.swing.JButton();
        txt_searchBy = new lms.ComboBoxSuggestion();
        txt_searchBook = new app.bolivia.swing.JCTextField();
        jLabel5 = new javax.swing.JLabel();
        viewBookStatus_Panel = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        borrowed_Table = new rojeru_san.complementos.RSTableMetro();
        jPanel6 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        book_Count = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        overDue_table = new rojeru_san.complementos.RSTableMetro();
        jPanel8 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        Dashboard_StudentCount3 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        History_Panel = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jPanel22 = new javax.swing.JPanel();
        studentRecord_ScrollPane = new javax.swing.JScrollPane();
        studentRecord_Table = new rojeru_san.complementos.RSTableMetro();
        returnedBooks_ScrollPane = new javax.swing.JScrollPane();
        returnedBooks_Table = new rojeru_san.complementos.RSTableMetro();
        overdueBooks_ScrollPane = new javax.swing.JScrollPane();
        overdueBooks_Table = new rojeru_san.complementos.RSTableMetro();
        jLabel43 = new javax.swing.JLabel();
        studentRecrods_Category = new lms.ComboBoxSuggestion();
        studentRecord_searchBar = new app.bolivia.swing.JCTextField();
        jLabel49 = new javax.swing.JLabel();
        cancel_button = new rojeru_san.complementos.RSButtonHover();
        overdueBooks_button = new rojeru_san.complementos.RSButtonHover();
        borrowedBooks_button = new rojeru_san.complementos.RSButtonHover();
        returnedBooks_button = new rojeru_san.complementos.RSButtonHover();
        Account_Panel = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        studentProfileSidebar = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        cancel_button4 = new rojeru_san.complementos.RSButtonHover();
        jLabel31 = new javax.swing.JLabel();
        studentID_ph = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        studentName_ph = new javax.swing.JLabel();
        studentRole_ph = new javax.swing.JLabel();
        userInfo_button = new rojeru_san.complementos.RSButtonHover();
        password_button = new rojeru_san.complementos.RSButtonHover();
        exit_button = new rojeru_san.complementos.RSButtonHover();
        userInfo_Panel = new javax.swing.JPanel();
        tf_name = new app.bolivia.swing.JCTextField();
        tf_studentid = new app.bolivia.swing.JCTextField();
        jLabel4 = new javax.swing.JLabel();
        tf_strand = new app.bolivia.swing.JCTextField();
        jLabel6 = new javax.swing.JLabel();
        tf_section = new app.bolivia.swing.JCTextField();
        jLabel7 = new javax.swing.JLabel();
        tf_address = new app.bolivia.swing.JCTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        tf_contactno = new app.bolivia.swing.JCTextField();
        jLabel42 = new javax.swing.JLabel();
        passwordSettings_Panel = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        tf_currentpass = new app.bolivia.swing.JCTextField();
        tf_confirmpass = new app.bolivia.swing.JCTextField();
        save_password = new rojeru_san.complementos.RSButtonHover();
        tf_newpass = new app.bolivia.swing.JCTextField();
        back_button2 = new rojeru_san.complementos.RSButtonHover();
        SideTab = new javax.swing.JPanel();
        logo = new javax.swing.JLabel();
        USERNAME = new javax.swing.JLabel();
        features = new javax.swing.JLabel();
        Settings_Panel = new javax.swing.JPanel();
        Account_button = new javax.swing.JButton();
        Logout_button = new javax.swing.JButton();
        HomeTab = new javax.swing.JPanel();
        Home_button = new javax.swing.JButton();
        bookInformation_Tab = new javax.swing.JPanel();
        Managebooks_button = new javax.swing.JButton();
        studentInformation_Tab = new javax.swing.JPanel();
        accountDetails_button = new javax.swing.JButton();
        Records_Tab = new javax.swing.JPanel();
        Issuebooks_button = new javax.swing.JButton();
        SettingsTab = new javax.swing.JPanel();
        Settings_button = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

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

        userDashBoard_TotalUsers.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        userDashBoard_TotalUsers.setForeground(new java.awt.Color(255, 255, 255));
        userDashBoard_TotalUsers.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        userDashBoard_TotalUsers.setText("0");
        jPanel14.add(userDashBoard_TotalUsers, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, 110, 40));

        jLabel25.setBackground(new java.awt.Color(207, 76, 56));
        jLabel25.setOpaque(true);
        jPanel14.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 20));

        jPanel3.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 230, 130));

        jPanel12.setBackground(new java.awt.Color(15, 28, 44));
        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel14.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Books Borrowed");
        jPanel12.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 150, 40));

        DashBoard_BorrowedCount.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        DashBoard_BorrowedCount.setForeground(new java.awt.Color(255, 255, 255));
        DashBoard_BorrowedCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DashBoard_BorrowedCount.setText("0");
        jPanel12.add(DashBoard_BorrowedCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 70, 130, 40));

        jLabel17.setBackground(new java.awt.Color(207, 76, 56));
        jLabel17.setOpaque(true);
        jPanel12.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 20));

        jLabel19.setBackground(new java.awt.Color(153, 153, 153));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/issuedbooks.png"))); // NOI18N
        jLabel19.setOpaque(true);
        jPanel12.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 70, 70));

        jPanel3.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 70, 230, 130));

        jPanel15.setBackground(new java.awt.Color(15, 28, 44));
        jPanel15.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel26.setFont(new java.awt.Font("Century Gothic", 0, 17)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Available Books");
        jPanel15.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 150, 40));

        jLabel27.setBackground(new java.awt.Color(153, 153, 153));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/book_1.png"))); // NOI18N
        jLabel27.setOpaque(true);
        jPanel15.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 70, 70));

        Dashboard_BooksCount.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Dashboard_BooksCount.setForeground(new java.awt.Color(255, 255, 255));
        Dashboard_BooksCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Dashboard_BooksCount.setText("0");
        jPanel15.add(Dashboard_BooksCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 120, 40));

        jLabel29.setBackground(new java.awt.Color(207, 76, 56));
        jLabel29.setOpaque(true);
        jPanel15.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 20));

        jPanel3.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 70, 230, 130));

        jPanel13.setBackground(new java.awt.Color(15, 28, 44));
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setFont(new java.awt.Font("Century Gothic", 0, 17)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Overdue Books");
        jPanel13.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 150, 40));

        Dashboard_OverdueCount.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Dashboard_OverdueCount.setForeground(new java.awt.Color(255, 255, 255));
        Dashboard_OverdueCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Dashboard_OverdueCount.setText("0");
        jPanel13.add(Dashboard_OverdueCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 120, 40));

        jLabel21.setBackground(new java.awt.Color(207, 76, 56));
        jLabel21.setOpaque(true);
        jPanel13.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 20));

        jLabel15.setBackground(new java.awt.Color(153, 153, 153));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/defaulterlist.png"))); // NOI18N
        jLabel15.setOpaque(true);
        jPanel13.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 70, 70));

        jPanel3.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 70, 230, 130));

        jLabel11.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("Books List");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 120, 40));

        Dashboard_BookDetails.setBackground(new java.awt.Color(15, 28, 44));
        Dashboard_BookDetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Book ID", "Book Author", "Book Title", "Category", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
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
            Dashboard_BookDetails.getColumnModel().getColumn(4).setResizable(false);
            Dashboard_BookDetails.getColumnModel().getColumn(5).setResizable(false);
        }

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 1000, 390));

        Time.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Time.setText("12:00:00 AM");
        jPanel3.add(Time, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, 160, 30));

        Date.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Date.setText("Sunday, 01-12, 2024");
        jPanel3.add(Date, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 160, 30));

        Home_Panel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 1030, 650));

        MainPanel.add(Home_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        bookInformation_Panel.setBackground(new java.awt.Color(204, 204, 204));
        bookInformation_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(17, 36, 59));
        jPanel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel4MouseClicked(evt);
            }
        });
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(207, 76, 56));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/book.png"))); // NOI18N
        jLabel30.setText(" Books Informations");
        jPanel4.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 240, 60));

        bookAvailable_checkbox.setFont(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        bookAvailable_checkbox.setForeground(new java.awt.Color(207, 76, 56));
        bookAvailable_checkbox.setText("Show Available Status Only");
        bookAvailable_checkbox.setContentAreaFilled(false);
        bookAvailable_checkbox.setIconTextGap(5);
        bookAvailable_checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookAvailable_checkboxActionPerformed(evt);
            }
        });
        jPanel4.add(bookAvailable_checkbox, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 60, 170, 30));

        bookInformation_Table.setBackground(new java.awt.Color(15, 28, 44));
        bookInformation_Table.setModel(new javax.swing.table.DefaultTableModel(
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
        bookInformation_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        bookInformation_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        bookInformation_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        bookInformation_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        bookInformation_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        bookInformation_Table.setColumnSelectionAllowed(true);
        bookInformation_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        bookInformation_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        bookInformation_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        bookInformation_Table.setGridColor(new java.awt.Color(15, 28, 44));
        bookInformation_Table.setRowHeight(25);
        bookInformation_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        bookInformation_Table.setShowGrid(false);
        bookInformation_Table.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(bookInformation_Table);
        bookInformation_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (bookInformation_Table.getColumnModel().getColumnCount() > 0) {
            bookInformation_Table.getColumnModel().getColumn(0).setResizable(false);
            bookInformation_Table.getColumnModel().getColumn(1).setResizable(false);
            bookInformation_Table.getColumnModel().getColumn(2).setResizable(false);
            bookInformation_Table.getColumnModel().getColumn(3).setResizable(false);
            bookInformation_Table.getColumnModel().getColumn(4).setResizable(false);
            bookInformation_Table.getColumnModel().getColumn(5).setResizable(false);
        }

        jPanel4.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 90, 770, 550));

        jPanel1.setBackground(new java.awt.Color(15, 28, 44));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(207, 76, 56));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Category");
        jLabel2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(207, 76, 56)));
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 1, 220, 40));

        goback_button.setBackground(new java.awt.Color(15, 28, 44));
        goback_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        goback_button.setForeground(new java.awt.Color(255, 255, 255));
        goback_button.setText("Go Back");
        goback_button.setBorder(null);
        goback_button.setContentAreaFilled(false);
        goback_button.setOpaque(true);
        goback_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goback_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(goback_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 500, 180, 30));

        ict_button.setBackground(new java.awt.Color(15, 28, 44));
        ict_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ict_button.setForeground(new java.awt.Color(255, 255, 255));
        ict_button.setText("ICT");
        ict_button.setBorder(null);
        ict_button.setContentAreaFilled(false);
        ict_button.setOpaque(true);
        ict_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ict_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(ict_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 180, 30));

        english_button.setBackground(new java.awt.Color(15, 28, 44));
        english_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        english_button.setForeground(new java.awt.Color(255, 255, 255));
        english_button.setText("English");
        english_button.setBorder(null);
        english_button.setContentAreaFilled(false);
        english_button.setOpaque(true);
        english_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                english_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(english_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 180, 30));

        research_button.setBackground(new java.awt.Color(15, 28, 44));
        research_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        research_button.setForeground(new java.awt.Color(255, 255, 255));
        research_button.setText("Research");
        research_button.setBorder(null);
        research_button.setContentAreaFilled(false);
        research_button.setOpaque(true);
        research_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                research_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(research_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 180, 30));

        abm_button.setBackground(new java.awt.Color(15, 28, 44));
        abm_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        abm_button.setForeground(new java.awt.Color(255, 255, 255));
        abm_button.setText("ABM");
        abm_button.setBorder(null);
        abm_button.setContentAreaFilled(false);
        abm_button.setOpaque(true);
        abm_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abm_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(abm_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 180, 30));

        math_button.setBackground(new java.awt.Color(15, 28, 44));
        math_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        math_button.setForeground(new java.awt.Color(255, 255, 255));
        math_button.setText("Math");
        math_button.setBorder(null);
        math_button.setContentAreaFilled(false);
        math_button.setOpaque(true);
        math_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                math_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(math_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 180, 30));

        psychology_button.setBackground(new java.awt.Color(15, 28, 44));
        psychology_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        psychology_button.setForeground(new java.awt.Color(255, 255, 255));
        psychology_button.setText("Psychology");
        psychology_button.setBorder(null);
        psychology_button.setContentAreaFilled(false);
        psychology_button.setOpaque(true);
        psychology_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psychology_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(psychology_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 180, 30));

        humss_button.setBackground(new java.awt.Color(15, 28, 44));
        humss_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        humss_button.setForeground(new java.awt.Color(255, 255, 255));
        humss_button.setText("HUMSS");
        humss_button.setBorder(null);
        humss_button.setContentAreaFilled(false);
        humss_button.setOpaque(true);
        humss_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                humss_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(humss_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 180, 30));

        law_button.setBackground(new java.awt.Color(15, 28, 44));
        law_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        law_button.setForeground(new java.awt.Color(255, 255, 255));
        law_button.setText("Law");
        law_button.setBorder(null);
        law_button.setContentAreaFilled(false);
        law_button.setOpaque(true);
        law_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                law_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(law_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 360, 180, 30));

        science_button.setBackground(new java.awt.Color(15, 28, 44));
        science_button.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        science_button.setForeground(new java.awt.Color(255, 255, 255));
        science_button.setText("Science");
        science_button.setBorder(null);
        science_button.setContentAreaFilled(false);
        science_button.setOpaque(true);
        science_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                science_buttonActionPerformed(evt);
            }
        });
        jPanel1.add(science_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 400, 180, 30));

        jPanel4.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 220, 540));

        txt_searchBy.setEditable(false);
        txt_searchBy.setForeground(new java.awt.Color(255, 255, 255));
        txt_searchBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Book ID", "Book Title", "Author" }));
        jPanel4.add(txt_searchBy, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 60, 110, 30));

        txt_searchBook.setBackground(new java.awt.Color(15, 28, 44));
        txt_searchBook.setForeground(new java.awt.Color(255, 255, 255));
        txt_searchBook.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_searchBookKeyReleased(evt);
            }
        });
        jPanel4.add(txt_searchBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 60, 370, -1));

        jLabel5.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Search by - ");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 60, 100, 30));

        bookInformation_Panel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1030, 660));

        MainPanel.add(bookInformation_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        viewBookStatus_Panel.setBackground(new java.awt.Color(204, 204, 204));
        viewBookStatus_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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
        jLabel34.setText("View Book Status");
        jPanel2.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 10, 270, 70));

        borrowed_Table.setBackground(new java.awt.Color(15, 28, 44));
        borrowed_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Book ID", "Issued Date", "Title", "Qty", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        borrowed_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        borrowed_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        borrowed_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        borrowed_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        borrowed_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        borrowed_Table.setColumnSelectionAllowed(true);
        borrowed_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        borrowed_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        borrowed_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        borrowed_Table.setGridColor(new java.awt.Color(15, 28, 44));
        borrowed_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        borrowed_Table.setShowGrid(false);
        borrowed_Table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(borrowed_Table);
        borrowed_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (borrowed_Table.getColumnModel().getColumnCount() > 0) {
            borrowed_Table.getColumnModel().getColumn(0).setResizable(false);
            borrowed_Table.getColumnModel().getColumn(1).setResizable(false);
            borrowed_Table.getColumnModel().getColumn(2).setResizable(false);
            borrowed_Table.getColumnModel().getColumn(3).setResizable(false);
            borrowed_Table.getColumnModel().getColumn(4).setResizable(false);
        }

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 500, 420));

        jPanel6.setBackground(new java.awt.Color(15, 39, 68));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel33.setBackground(new java.awt.Color(153, 153, 153));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user_1.png"))); // NOI18N
        jLabel33.setOpaque(true);
        jPanel6.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 80, 70));

        jLabel32.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setText("Book Borrowed");
        jPanel6.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, 150, 40));

        book_Count.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        book_Count.setForeground(new java.awt.Color(255, 255, 255));
        book_Count.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        book_Count.setText("0");
        jPanel6.add(book_Count, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 50, 110, 30));

        jPanel2.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 500, 90));

        jLabel35.setBackground(new java.awt.Color(207, 76, 56));
        jLabel35.setOpaque(true);
        jPanel2.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 500, 30));

        overDue_table.setBackground(new java.awt.Color(15, 28, 44));
        overDue_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Book ID", "Issued Date", "Title", "Qty", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        overDue_table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        overDue_table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        overDue_table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        overDue_table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        overDue_table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        overDue_table.setColumnSelectionAllowed(true);
        overDue_table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        overDue_table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        overDue_table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        overDue_table.setGridColor(new java.awt.Color(15, 28, 44));
        overDue_table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        overDue_table.setShowGrid(false);
        overDue_table.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(overDue_table);
        overDue_table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (overDue_table.getColumnModel().getColumnCount() > 0) {
            overDue_table.getColumnModel().getColumn(0).setResizable(false);
            overDue_table.getColumnModel().getColumn(1).setResizable(false);
            overDue_table.getColumnModel().getColumn(2).setResizable(false);
            overDue_table.getColumnModel().getColumn(3).setResizable(false);
            overDue_table.getColumnModel().getColumn(4).setResizable(false);
        }

        jPanel2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 220, 490, 420));

        jPanel8.setBackground(new java.awt.Color(15, 39, 68));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel36.setBackground(new java.awt.Color(153, 153, 153));
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user_1.png"))); // NOI18N
        jLabel36.setOpaque(true);
        jPanel8.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, 80, 70));

        jLabel37.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("Overdue Books");
        jPanel8.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 10, 150, 40));

        Dashboard_StudentCount3.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Dashboard_StudentCount3.setForeground(new java.awt.Color(255, 255, 255));
        Dashboard_StudentCount3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Dashboard_StudentCount3.setText("0");
        jPanel8.add(Dashboard_StudentCount3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 50, 110, 30));

        jPanel2.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 120, 490, 90));

        jLabel38.setBackground(new java.awt.Color(207, 76, 56));
        jLabel38.setOpaque(true);
        jPanel2.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 90, 490, 30));

        jPanel16.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1020, 650));

        viewBookStatus_Panel.add(jPanel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 1020, 650));

        MainPanel.add(viewBookStatus_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        History_Panel.setBackground(new java.awt.Color(204, 204, 204));
        History_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel21.setBackground(new java.awt.Color(255, 255, 255));
        jPanel21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel21MouseClicked(evt);
            }
        });
        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel22.setBackground(new java.awt.Color(15, 28, 44));
        jPanel22.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel22MouseClicked(evt);
            }
        });
        jPanel22.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentRecord_Table.setBackground(new java.awt.Color(15, 28, 44));
        studentRecord_Table.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        studentRecord_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Student ID", "Issue Date", "Due Date", "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        studentRecord_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        studentRecord_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        studentRecord_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        studentRecord_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        studentRecord_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        studentRecord_Table.setColumnSelectionAllowed(true);
        studentRecord_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        studentRecord_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        studentRecord_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        studentRecord_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        studentRecord_Table.setGridColor(new java.awt.Color(15, 28, 44));
        studentRecord_Table.setMultipleSeleccion(false);
        studentRecord_Table.setRowHeight(25);
        studentRecord_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        studentRecord_Table.setShowGrid(false);
        studentRecord_Table.setShowHorizontalLines(true);
        studentRecord_Table.setShowVerticalLines(true);
        studentRecord_Table.getTableHeader().setReorderingAllowed(false);
        studentRecord_ScrollPane.setViewportView(studentRecord_Table);
        studentRecord_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (studentRecord_Table.getColumnModel().getColumnCount() > 0) {
            studentRecord_Table.getColumnModel().getColumn(0).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(1).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(2).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(3).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(3).setHeaderValue("Due Date");
            studentRecord_Table.getColumnModel().getColumn(4).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(5).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(6).setResizable(false);
            studentRecord_Table.getColumnModel().getColumn(7).setResizable(false);
        }

        jPanel22.add(studentRecord_ScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1020, 540));

        returnedBooks_Table.setBackground(new java.awt.Color(15, 28, 44));
        returnedBooks_Table.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        returnedBooks_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Student ID", "Returned Date", "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        returnedBooks_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        returnedBooks_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        returnedBooks_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        returnedBooks_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        returnedBooks_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        returnedBooks_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        returnedBooks_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        returnedBooks_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        returnedBooks_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        returnedBooks_Table.setGridColor(new java.awt.Color(15, 28, 44));
        returnedBooks_Table.setMultipleSeleccion(false);
        returnedBooks_Table.setRowHeight(25);
        returnedBooks_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        returnedBooks_Table.setShowGrid(false);
        returnedBooks_Table.setShowHorizontalLines(true);
        returnedBooks_Table.setShowVerticalLines(true);
        returnedBooks_Table.getTableHeader().setReorderingAllowed(false);
        returnedBooks_ScrollPane.setViewportView(returnedBooks_Table);
        returnedBooks_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (returnedBooks_Table.getColumnModel().getColumnCount() > 0) {
            returnedBooks_Table.getColumnModel().getColumn(0).setResizable(false);
            returnedBooks_Table.getColumnModel().getColumn(1).setResizable(false);
            returnedBooks_Table.getColumnModel().getColumn(2).setResizable(false);
            returnedBooks_Table.getColumnModel().getColumn(3).setResizable(false);
            returnedBooks_Table.getColumnModel().getColumn(4).setResizable(false);
            returnedBooks_Table.getColumnModel().getColumn(5).setResizable(false);
            returnedBooks_Table.getColumnModel().getColumn(6).setResizable(false);
        }

        jPanel22.add(returnedBooks_ScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1020, 540));

        overdueBooks_Table.setBackground(new java.awt.Color(15, 28, 44));
        overdueBooks_Table.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        overdueBooks_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Student ID", "Issue Date", "Due Date", "Book ID", "Book Title", "Quantity", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        overdueBooks_Table.setColorBackgoundHead(new java.awt.Color(15, 28, 44));
        overdueBooks_Table.setColorBordeFilas(new java.awt.Color(15, 28, 44));
        overdueBooks_Table.setColorBordeHead(new java.awt.Color(15, 28, 44));
        overdueBooks_Table.setColorFilasForeground1(new java.awt.Color(0, 0, 0));
        overdueBooks_Table.setColorFilasForeground2(new java.awt.Color(0, 0, 0));
        overdueBooks_Table.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        overdueBooks_Table.setFuenteFilas(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        overdueBooks_Table.setFuenteFilasSelect(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        overdueBooks_Table.setFuenteHead(new java.awt.Font("Century Gothic", 0, 10)); // NOI18N
        overdueBooks_Table.setGridColor(new java.awt.Color(15, 28, 44));
        overdueBooks_Table.setMultipleSeleccion(false);
        overdueBooks_Table.setRowHeight(25);
        overdueBooks_Table.setSelectionBackground(new java.awt.Color(15, 28, 44));
        overdueBooks_Table.setShowGrid(false);
        overdueBooks_Table.setShowHorizontalLines(true);
        overdueBooks_Table.setShowVerticalLines(true);
        overdueBooks_Table.getTableHeader().setReorderingAllowed(false);
        overdueBooks_ScrollPane.setViewportView(overdueBooks_Table);
        overdueBooks_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (overdueBooks_Table.getColumnModel().getColumnCount() > 0) {
            overdueBooks_Table.getColumnModel().getColumn(0).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(1).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(2).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(3).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(3).setHeaderValue("Due Date");
            overdueBooks_Table.getColumnModel().getColumn(4).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(5).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(6).setResizable(false);
            overdueBooks_Table.getColumnModel().getColumn(7).setResizable(false);
        }

        jPanel22.add(overdueBooks_ScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 1020, 540));

        jLabel43.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(207, 76, 56));
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-manager.png"))); // NOI18N
        jLabel43.setText("Student Record");
        jPanel22.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 210, 60));

        studentRecrods_Category.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        studentRecrods_Category.setEditable(false);
        studentRecrods_Category.setForeground(new java.awt.Color(255, 255, 255));
        studentRecrods_Category.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Book Title", "Book ID", "Issue Date" }));
        jPanel22.add(studentRecrods_Category, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 30, 100, 30));

        studentRecord_searchBar.setBackground(new java.awt.Color(15, 28, 44));
        studentRecord_searchBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        studentRecord_searchBar.setForeground(new java.awt.Color(255, 255, 255));
        studentRecord_searchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                studentRecord_searchBarKeyReleased(evt);
            }
        });
        jPanel22.add(studentRecord_searchBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 30, 160, 30));

        jLabel49.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel49.setText("Search by: ");
        jPanel22.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 30, 90, 30));

        cancel_button.setBackground(new java.awt.Color(15, 28, 44));
        cancel_button.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(16, 42, 72)));
        cancel_button.setForeground(new java.awt.Color(207, 76, 56));
        cancel_button.setText("Back");
        cancel_button.setColorHover(new java.awt.Color(15, 28, 44));
        cancel_button.setColorText(new java.awt.Color(207, 76, 56));
        cancel_button.setColorTextHover(new java.awt.Color(207, 76, 56));
        cancel_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        cancel_button.setOpaque(true);
        cancel_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_buttonActionPerformed(evt);
            }
        });
        jPanel22.add(cancel_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 610, 80, 20));

        overdueBooks_button.setBackground(new java.awt.Color(15, 28, 44));
        overdueBooks_button.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(16, 42, 72)));
        overdueBooks_button.setForeground(new java.awt.Color(207, 76, 56));
        overdueBooks_button.setText("Overdue Books");
        overdueBooks_button.setColorHover(new java.awt.Color(19, 40, 67));
        overdueBooks_button.setColorText(new java.awt.Color(207, 76, 56));
        overdueBooks_button.setColorTextHover(new java.awt.Color(207, 76, 56));
        overdueBooks_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        overdueBooks_button.setOpaque(true);
        overdueBooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overdueBooks_buttonActionPerformed(evt);
            }
        });
        jPanel22.add(overdueBooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 110, 30));

        borrowedBooks_button.setBackground(new java.awt.Color(15, 28, 44));
        borrowedBooks_button.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(16, 42, 72)));
        borrowedBooks_button.setForeground(new java.awt.Color(207, 76, 56));
        borrowedBooks_button.setText("Borrowed Books");
        borrowedBooks_button.setColorHover(new java.awt.Color(19, 40, 67));
        borrowedBooks_button.setColorText(new java.awt.Color(207, 76, 56));
        borrowedBooks_button.setColorTextHover(new java.awt.Color(207, 76, 56));
        borrowedBooks_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        borrowedBooks_button.setOpaque(true);
        borrowedBooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrowedBooks_buttonActionPerformed(evt);
            }
        });
        jPanel22.add(borrowedBooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 110, 30));

        returnedBooks_button.setBackground(new java.awt.Color(15, 28, 44));
        returnedBooks_button.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(16, 42, 72)));
        returnedBooks_button.setForeground(new java.awt.Color(207, 76, 56));
        returnedBooks_button.setText("Returned Books");
        returnedBooks_button.setColorHover(new java.awt.Color(19, 40, 67));
        returnedBooks_button.setColorText(new java.awt.Color(207, 76, 56));
        returnedBooks_button.setColorTextHover(new java.awt.Color(207, 76, 56));
        returnedBooks_button.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        returnedBooks_button.setOpaque(true);
        returnedBooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnedBooks_buttonActionPerformed(evt);
            }
        });
        jPanel22.add(returnedBooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 30, 110, 30));

        jPanel21.add(jPanel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1030, 640));

        History_Panel.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 1020, -1));

        MainPanel.add(History_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        Account_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Account_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel19MouseClicked(evt);
            }
        });
        jPanel19.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel20.setBackground(new java.awt.Color(255, 255, 255));
        jPanel20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel20MouseClicked(evt);
            }
        });
        jPanel20.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        studentProfileSidebar.setBackground(new java.awt.Color(15, 28, 44));
        studentProfileSidebar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel28.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(207, 76, 56));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Student Profile Information");
        jLabel28.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        studentProfileSidebar.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 90));

        cancel_button4.setBackground(new java.awt.Color(255, 255, 255));
        cancel_button4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cancel_button4.setForeground(new java.awt.Color(15, 28, 44));
        cancel_button4.setText("Cancel");
        cancel_button4.setColorHover(new java.awt.Color(207, 76, 56));
        cancel_button4.setColorText(new java.awt.Color(15, 28, 44));
        cancel_button4.setColorTextHover(new java.awt.Color(15, 28, 44));
        cancel_button4.setFont(new java.awt.Font("Century Gothic", 0, 11)); // NOI18N
        cancel_button4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_button4ActionPerformed(evt);
            }
        });
        studentProfileSidebar.add(cancel_button4, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 610, 70, 20));

        jLabel31.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel31.setText("Full Name:");
        studentProfileSidebar.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 90, 70, 30));

        studentID_ph.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        studentID_ph.setForeground(new java.awt.Color(255, 255, 255));
        studentID_ph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        studentID_ph.setText("Student ID Here");
        studentProfileSidebar.add(studentID_ph, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 150, 160, -1));

        jLabel40.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel40.setText("Profile Information:");
        jLabel40.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        studentProfileSidebar.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 230, 280, 30));

        studentName_ph.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        studentName_ph.setForeground(new java.awt.Color(255, 255, 255));
        studentName_ph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        studentName_ph.setText("Name HERE");
        studentProfileSidebar.add(studentName_ph, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, 160, 50));

        studentRole_ph.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        studentRole_ph.setForeground(new java.awt.Color(255, 255, 255));
        studentRole_ph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        studentRole_ph.setText("Student Role");
        studentProfileSidebar.add(studentRole_ph, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 180, 160, 40));

        userInfo_button.setBackground(new java.awt.Color(15, 28, 44));
        userInfo_button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        userInfo_button.setText("Personal Information");
        userInfo_button.setBorderPainted(false);
        userInfo_button.setColorHover(new java.awt.Color(207, 76, 56));
        userInfo_button.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        userInfo_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userInfo_buttonActionPerformed(evt);
            }
        });
        studentProfileSidebar.add(userInfo_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 270, 280, -1));

        password_button.setBackground(new java.awt.Color(15, 28, 44));
        password_button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        password_button.setText("Password Settings");
        password_button.setBorderPainted(false);
        password_button.setColorHover(new java.awt.Color(207, 76, 56));
        password_button.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        password_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                password_buttonActionPerformed(evt);
            }
        });
        studentProfileSidebar.add(password_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 320, 280, -1));

        exit_button.setBackground(new java.awt.Color(15, 28, 44));
        exit_button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        exit_button.setText("Exit");
        exit_button.setBorderPainted(false);
        exit_button.setColorHover(new java.awt.Color(207, 76, 56));
        exit_button.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        exit_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exit_buttonActionPerformed(evt);
            }
        });
        studentProfileSidebar.add(exit_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 620, 280, -1));

        jPanel20.add(studentProfileSidebar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 280, 660));

        userInfo_Panel.setBackground(new java.awt.Color(15, 28, 44));
        userInfo_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tf_name.setBackground(new java.awt.Color(15, 28, 44));
        tf_name.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_name.setForeground(new java.awt.Color(255, 255, 255));
        tf_name.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_name.setPhColor(new java.awt.Color(255, 255, 255));
        tf_name.setPlaceholder("Name");
        tf_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_nameActionPerformed(evt);
            }
        });
        userInfo_Panel.add(tf_name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 330, 50));

        tf_studentid.setBackground(new java.awt.Color(15, 28, 44));
        tf_studentid.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_studentid.setForeground(new java.awt.Color(255, 255, 255));
        tf_studentid.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_studentid.setPhColor(new java.awt.Color(255, 255, 255));
        tf_studentid.setPlaceholder("Student ID");
        userInfo_Panel.add(tf_studentid, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 330, 50));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Strand:");
        userInfo_Panel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 330, 70, 30));

        tf_strand.setBackground(new java.awt.Color(15, 28, 44));
        tf_strand.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_strand.setForeground(new java.awt.Color(255, 255, 255));
        tf_strand.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_strand.setPhColor(new java.awt.Color(255, 255, 255));
        tf_strand.setPlaceholder("Strand");
        userInfo_Panel.add(tf_strand, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 360, 330, 50));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Section:");
        userInfo_Panel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 330, 90, 30));

        tf_section.setBackground(new java.awt.Color(15, 28, 44));
        tf_section.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_section.setForeground(new java.awt.Color(255, 255, 255));
        tf_section.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_section.setPhColor(new java.awt.Color(255, 255, 255));
        tf_section.setPlaceholder("Section");
        userInfo_Panel.add(tf_section, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 360, 330, 50));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Address:");
        userInfo_Panel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 240, 90, 30));

        tf_address.setBackground(new java.awt.Color(15, 28, 44));
        tf_address.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_address.setForeground(new java.awt.Color(255, 255, 255));
        tf_address.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_address.setPhColor(new java.awt.Color(255, 255, 255));
        tf_address.setPlaceholder("Address");
        userInfo_Panel.add(tf_address, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 270, 330, 50));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Contact No:");
        userInfo_Panel.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 140, 110, 30));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Student ID:");
        userInfo_Panel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 90, 30));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("ACCOUNT INFORMATION");
        userInfo_Panel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 40, 260, 30));

        tf_contactno.setBackground(new java.awt.Color(15, 28, 44));
        tf_contactno.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_contactno.setForeground(new java.awt.Color(255, 255, 255));
        tf_contactno.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_contactno.setPhColor(new java.awt.Color(255, 255, 255));
        tf_contactno.setPlaceholder("Contact No.");
        userInfo_Panel.add(tf_contactno, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 170, 330, 50));

        jLabel42.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel42.setText("Full Name:");
        userInfo_Panel.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, 90, 30));

        jPanel20.add(userInfo_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 720, 600));

        passwordSettings_Panel.setBackground(new java.awt.Color(15, 28, 44));
        passwordSettings_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel46.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel46.setText("Current Password:");
        passwordSettings_Panel.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, 150, 30));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("New Password:");
        passwordSettings_Panel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 270, 130, 30));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Password Settings");
        passwordSettings_Panel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, 260, 50));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("Confirm Password:");
        passwordSettings_Panel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 370, 160, 30));

        tf_currentpass.setBackground(new java.awt.Color(15, 28, 44));
        tf_currentpass.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_currentpass.setForeground(new java.awt.Color(255, 255, 255));
        tf_currentpass.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_currentpass.setPhColor(new java.awt.Color(255, 255, 255));
        tf_currentpass.setPlaceholder("Enter Password");
        tf_currentpass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tf_currentpassActionPerformed(evt);
            }
        });
        passwordSettings_Panel.add(tf_currentpass, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 190, 330, 50));

        tf_confirmpass.setBackground(new java.awt.Color(15, 28, 44));
        tf_confirmpass.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_confirmpass.setForeground(new java.awt.Color(255, 255, 255));
        tf_confirmpass.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_confirmpass.setPhColor(new java.awt.Color(255, 255, 255));
        tf_confirmpass.setPlaceholder("Enter Password");
        passwordSettings_Panel.add(tf_confirmpass, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 390, 330, 50));

        save_password.setBackground(new java.awt.Color(255, 255, 255));
        save_password.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        save_password.setForeground(new java.awt.Color(15, 28, 44));
        save_password.setText("Save");
        save_password.setColorHover(new java.awt.Color(207, 76, 56));
        save_password.setColorText(new java.awt.Color(15, 28, 44));
        save_password.setColorTextHover(new java.awt.Color(15, 28, 44));
        save_password.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        save_password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_passwordActionPerformed(evt);
            }
        });
        passwordSettings_Panel.add(save_password, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 480, 120, 20));

        tf_newpass.setBackground(new java.awt.Color(15, 28, 44));
        tf_newpass.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        tf_newpass.setForeground(new java.awt.Color(255, 255, 255));
        tf_newpass.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tf_newpass.setPhColor(new java.awt.Color(255, 255, 255));
        tf_newpass.setPlaceholder("Enter Password");
        passwordSettings_Panel.add(tf_newpass, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 290, 330, 50));

        back_button2.setBackground(new java.awt.Color(255, 255, 255));
        back_button2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        back_button2.setForeground(new java.awt.Color(15, 28, 44));
        back_button2.setText("Back");
        back_button2.setColorHover(new java.awt.Color(207, 76, 56));
        back_button2.setColorText(new java.awt.Color(15, 28, 44));
        back_button2.setColorTextHover(new java.awt.Color(15, 28, 44));
        back_button2.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        back_button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back_button2ActionPerformed(evt);
            }
        });
        passwordSettings_Panel.add(back_button2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 480, 130, 20));

        jPanel20.add(passwordSettings_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 720, 600));

        jPanel19.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1030, 660));

        Account_Panel.add(jPanel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 1020, -1));

        MainPanel.add(Account_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 30, 1050, 690));

        SideTab.setBackground(new java.awt.Color(15, 28, 44));
        SideTab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logo.setFont(new java.awt.Font("Century Gothic", 1, 30)); // NOI18N
        logo.setForeground(new java.awt.Color(255, 255, 255));
        logo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/book.png"))); // NOI18N
        logo.setText(" L M S");
        SideTab.add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 230, 70));

        USERNAME.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        USERNAME.setForeground(new java.awt.Color(255, 255, 255));
        USERNAME.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        USERNAME.setText("USER FULL NAME");
        SideTab.add(USERNAME, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 230, 40));

        features.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        features.setForeground(new java.awt.Color(255, 255, 255));
        features.setText("Features");
        SideTab.add(features, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, 60, 20));

        Settings_Panel.setBackground(new java.awt.Color(5, 21, 40));
        Settings_Panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Account_button.setBackground(new java.awt.Color(5, 21, 40));
        Account_button.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        Account_button.setForeground(new java.awt.Color(255, 255, 255));
        Account_button.setText("Account");
        Account_button.setBorder(null);
        Account_button.setContentAreaFilled(false);
        Account_button.setOpaque(true);
        Account_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Account_buttonActionPerformed(evt);
            }
        });
        Settings_Panel.add(Account_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 70, 20));

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
        Settings_Panel.add(Logout_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 70, 20));

        SideTab.add(Settings_Panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 610, 70, 70));

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

        SideTab.add(HomeTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 220, 230, 40));

        bookInformation_Tab.setBackground(new java.awt.Color(15, 28, 44));
        bookInformation_Tab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Managebooks_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Managebooks_button.setForeground(new java.awt.Color(255, 255, 255));
        Managebooks_button.setText("Browse Books List");
        Managebooks_button.setBorderPainted(false);
        Managebooks_button.setContentAreaFilled(false);
        Managebooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Managebooks_buttonActionPerformed(evt);
            }
        });
        bookInformation_Tab.add(Managebooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(bookInformation_Tab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 300, 230, 40));

        studentInformation_Tab.setBackground(new java.awt.Color(15, 28, 44));
        studentInformation_Tab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        accountDetails_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        accountDetails_button.setForeground(new java.awt.Color(255, 255, 255));
        accountDetails_button.setText("View Book Status");
        accountDetails_button.setBorderPainted(false);
        accountDetails_button.setContentAreaFilled(false);
        accountDetails_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountDetails_buttonActionPerformed(evt);
            }
        });
        studentInformation_Tab.add(accountDetails_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 40));

        SideTab.add(studentInformation_Tab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 350, 230, 40));

        Records_Tab.setBackground(new java.awt.Color(15, 28, 44));
        Records_Tab.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Issuebooks_button.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        Issuebooks_button.setForeground(new java.awt.Color(255, 255, 255));
        Issuebooks_button.setText("View Borrowing History");
        Issuebooks_button.setBorderPainted(false);
        Issuebooks_button.setContentAreaFilled(false);
        Issuebooks_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Issuebooks_buttonActionPerformed(evt);
            }
        });
        Records_Tab.add(Issuebooks_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 0, 230, 40));

        SideTab.add(Records_Tab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 400, 230, 40));

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
        SettingsTab.add(Settings_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 40));

        SideTab.add(SettingsTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 680, 230, 40));

        jLabel1.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Welcome!");
        SideTab.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 230, 30));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user.png"))); // NOI18N
        SideTab.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 116, 90, 50));

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
    togglePanelVisibility(bookInformation_Panel);
    bookInformation_Tab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Managebooks_buttonActionPerformed

    private void accountDetails_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountDetails_buttonActionPerformed
    togglePanelVisibility(viewBookStatus_Panel);
    studentInformation_Tab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_accountDetails_buttonActionPerformed

    private void Issuebooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Issuebooks_buttonActionPerformed
    togglePanelVisibility(History_Panel);
    //setBookDetailsToTable();
    Records_Tab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_Issuebooks_buttonActionPerformed

    private void minimizeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMousePressed
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_minimizeMousePressed

    private void closeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMousePressed
        System.exit(0);
    }//GEN-LAST:event_closeMousePressed

    private void Account_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Account_buttonActionPerformed
        Settings_Panel.setVisible(false);
        togglePanelVisibility(Account_Panel);
    }//GEN-LAST:event_Account_buttonActionPerformed

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

    private void jPanel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseClicked

    }//GEN-LAST:event_jPanel4MouseClicked

    
    
    private void jPanel19MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel19MouseClicked

    }//GEN-LAST:event_jPanel19MouseClicked

    private void jPanel20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel20MouseClicked

    }//GEN-LAST:event_jPanel20MouseClicked

    private void tf_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_nameActionPerformed

    }//GEN-LAST:event_tf_nameActionPerformed

    private void bookAvailable_checkboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookAvailable_checkboxActionPerformed
        DefaultTableModel tableModel = (DefaultTableModel) bookInformation_Table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookInformation_Table.setRowSorter(sorter);

        if (bookAvailable_checkbox.isSelected()) {
            // Filter to show only available books
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String status = (String) entry.getValue(5); // 5 is the index of the Status column
                    return "Available".equals(status);
                }
            });
        } else {
            // Show all books
            sorter.setRowFilter(null);
        }
    }//GEN-LAST:event_bookAvailable_checkboxActionPerformed

    private void jPanel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel16MouseClicked

    }//GEN-LAST:event_jPanel16MouseClicked

    private void jPanel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2MouseClicked

    private void cancel_button4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_button4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cancel_button4ActionPerformed

    private void userInfo_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userInfo_buttonActionPerformed
            userInfo_Panel.setVisible(true);
            passwordSettings_Panel.setVisible(false);
    }//GEN-LAST:event_userInfo_buttonActionPerformed

    private void password_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_password_buttonActionPerformed
            passwordSettings_Panel.setVisible(true);
            userInfo_Panel.setVisible(false);
    }//GEN-LAST:event_password_buttonActionPerformed

    private void tf_currentpassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tf_currentpassActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tf_currentpassActionPerformed

    private void save_passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_passwordActionPerformed
    String studentID = studentID_ph.getText(); // Assuming studentID_ph contains the student ID
    updatePassword(studentID);
    }//GEN-LAST:event_save_passwordActionPerformed

    private void back_button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back_button2ActionPerformed
        passwordSettings_Panel.setVisible(false);
        userInfo_Panel.setVisible(true);
    }//GEN-LAST:event_back_button2ActionPerformed

    private void exit_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exit_buttonActionPerformed
        Account_Panel.setVisible(false);
        togglePanelVisibility(Home_Panel);
        HomeTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_exit_buttonActionPerformed

    private void ict_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ict_buttonActionPerformed
    handleCategoryButtonSelection(ict_button, "ICT");
    }//GEN-LAST:event_ict_buttonActionPerformed

    private void english_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_english_buttonActionPerformed
    handleCategoryButtonSelection(english_button, "English");
    }//GEN-LAST:event_english_buttonActionPerformed

    private void research_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_research_buttonActionPerformed
    handleCategoryButtonSelection(research_button, "Research");
    }//GEN-LAST:event_research_buttonActionPerformed

    private void abm_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abm_buttonActionPerformed
    handleCategoryButtonSelection(abm_button, "ABM");
    }//GEN-LAST:event_abm_buttonActionPerformed

    private void math_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_math_buttonActionPerformed
    handleCategoryButtonSelection(math_button, "Math");
    }//GEN-LAST:event_math_buttonActionPerformed

    private void psychology_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_psychology_buttonActionPerformed
    handleCategoryButtonSelection(psychology_button, "Psychology");
    }//GEN-LAST:event_psychology_buttonActionPerformed

    private void humss_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_humss_buttonActionPerformed
    handleCategoryButtonSelection(humss_button, "HUMSS");
    }//GEN-LAST:event_humss_buttonActionPerformed

    private void law_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_law_buttonActionPerformed
    handleCategoryButtonSelection(law_button, "Law");
    }//GEN-LAST:event_law_buttonActionPerformed

    private void science_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_science_buttonActionPerformed
    handleCategoryButtonSelection(science_button, "Science");
    }//GEN-LAST:event_science_buttonActionPerformed

    private void studentRecord_searchBarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_studentRecord_searchBarKeyReleased
        String selectedCategory = (String) studentRecrods_Category.getSelectedItem();
        String searchString = studentRecord_searchBar.getText();
        searchStudentRecordTable(selectedCategory, searchString,studentRecord_Table);
    }//GEN-LAST:event_studentRecord_searchBarKeyReleased

    private void cancel_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_buttonActionPerformed
        History_Panel.setVisible(false);
        togglePanelVisibility(Home_Panel);
        HomeTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_cancel_buttonActionPerformed

    private void jPanel22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel22MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel22MouseClicked

    private void jPanel21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel21MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel21MouseClicked

    private void overdueBooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overdueBooks_buttonActionPerformed
        this.overdueBooks_ScrollPane.setVisible(true);
        this.studentRecord_ScrollPane.setVisible(false);
        this.returnedBooks_ScrollPane.setVisible(false);
    }//GEN-LAST:event_overdueBooks_buttonActionPerformed

    private void borrowedBooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrowedBooks_buttonActionPerformed
        this.studentRecord_ScrollPane.setVisible(true);
        this.returnedBooks_ScrollPane.setVisible(false);
        this.overdueBooks_ScrollPane.setVisible(false);
    }//GEN-LAST:event_borrowedBooks_buttonActionPerformed

    private void returnedBooks_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnedBooks_buttonActionPerformed
        this.returnedBooks_ScrollPane.setVisible(true);
        this.studentRecord_ScrollPane.setVisible(false);
        this.overdueBooks_ScrollPane.setVisible(false);
    }//GEN-LAST:event_returnedBooks_buttonActionPerformed

    private void goback_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goback_buttonActionPerformed
        bookInformation_Panel.setVisible(false);
        togglePanelVisibility(Home_Panel);
        HomeTab.setBackground(new Color(207,76,56));
    }//GEN-LAST:event_goback_buttonActionPerformed

    private void txt_searchBookKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_searchBookKeyReleased
        String searchString = txt_searchBook.getText();
        String selectedCategory = (String) txt_searchBy.getSelectedItem();
        bookInfoSearchBar(selectedCategory, searchString, bookInformation_Table);
    }//GEN-LAST:event_txt_searchBookKeyReleased

    
    
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
            java.util.logging.Logger.getLogger(User_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(User_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(User_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(User_UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new User_UI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Account_Panel;
    private javax.swing.JButton Account_button;
    private javax.swing.JLabel DashBoard_BorrowedCount;
    private rojeru_san.complementos.RSTableMetro Dashboard_BookDetails;
    private javax.swing.JLabel Dashboard_BooksCount;
    private javax.swing.JLabel Dashboard_OverdueCount;
    public static javax.swing.JLabel Dashboard_StudentCount3;
    private javax.swing.JLabel Date;
    private javax.swing.JPanel History_Panel;
    private javax.swing.JPanel HomeTab;
    private javax.swing.JPanel Home_Panel;
    private javax.swing.JButton Home_button;
    private javax.swing.JButton Issuebooks_button;
    private javax.swing.JButton Logout_button;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JButton Managebooks_button;
    private javax.swing.JPanel Records_Tab;
    private javax.swing.JPanel SettingsTab;
    private javax.swing.JPanel Settings_Panel;
    private javax.swing.JButton Settings_button;
    private javax.swing.JPanel SideTab;
    private javax.swing.JLabel Time;
    public static javax.swing.JLabel USERNAME;
    private javax.swing.JButton abm_button;
    private javax.swing.JButton accountDetails_button;
    private rojeru_san.complementos.RSButtonHover back_button2;
    private javax.swing.JCheckBox bookAvailable_checkbox;
    private javax.swing.JPanel bookInformation_Panel;
    private javax.swing.JPanel bookInformation_Tab;
    private rojeru_san.complementos.RSTableMetro bookInformation_Table;
    public static javax.swing.JLabel book_Count;
    private rojeru_san.complementos.RSButtonHover borrowedBooks_button;
    public static rojeru_san.complementos.RSTableMetro borrowed_Table;
    private rojeru_san.complementos.RSButtonHover cancel_button;
    private rojeru_san.complementos.RSButtonHover cancel_button4;
    private javax.swing.JLabel close;
    private javax.swing.JButton english_button;
    private rojeru_san.complementos.RSButtonHover exit_button;
    private javax.swing.JLabel features;
    private javax.swing.JButton goback_button;
    private javax.swing.JButton humss_button;
    private javax.swing.JButton ict_button;
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton law_button;
    private javax.swing.JLabel logo;
    private javax.swing.JButton math_button;
    private javax.swing.JLabel minimize;
    public static rojeru_san.complementos.RSTableMetro overDue_table;
    private javax.swing.JScrollPane overdueBooks_ScrollPane;
    private rojeru_san.complementos.RSTableMetro overdueBooks_Table;
    private rojeru_san.complementos.RSButtonHover overdueBooks_button;
    private javax.swing.JPanel passwordSettings_Panel;
    private rojeru_san.complementos.RSButtonHover password_button;
    private javax.swing.JButton psychology_button;
    private javax.swing.JButton research_button;
    private javax.swing.JScrollPane returnedBooks_ScrollPane;
    private rojeru_san.complementos.RSTableMetro returnedBooks_Table;
    private rojeru_san.complementos.RSButtonHover returnedBooks_button;
    private rojeru_san.complementos.RSButtonHover save_password;
    private javax.swing.JButton science_button;
    public static javax.swing.JLabel studentID_ph;
    private javax.swing.JPanel studentInformation_Tab;
    public static javax.swing.JLabel studentName_ph;
    private javax.swing.JPanel studentProfileSidebar;
    private javax.swing.JScrollPane studentRecord_ScrollPane;
    private rojeru_san.complementos.RSTableMetro studentRecord_Table;
    private app.bolivia.swing.JCTextField studentRecord_searchBar;
    private lms.ComboBoxSuggestion studentRecrods_Category;
    public static javax.swing.JLabel studentRole_ph;
    public static app.bolivia.swing.JCTextField tf_address;
    private app.bolivia.swing.JCTextField tf_confirmpass;
    public static app.bolivia.swing.JCTextField tf_contactno;
    private app.bolivia.swing.JCTextField tf_currentpass;
    public static app.bolivia.swing.JCTextField tf_name;
    private app.bolivia.swing.JCTextField tf_newpass;
    public static app.bolivia.swing.JCTextField tf_section;
    public static app.bolivia.swing.JCTextField tf_strand;
    public static app.bolivia.swing.JCTextField tf_studentid;
    private app.bolivia.swing.JCTextField txt_searchBook;
    private lms.ComboBoxSuggestion txt_searchBy;
    public static javax.swing.JLabel userDashBoard_TotalUsers;
    private javax.swing.JPanel userInfo_Panel;
    private rojeru_san.complementos.RSButtonHover userInfo_button;
    private javax.swing.JPanel viewBookStatus_Panel;
    // End of variables declaration//GEN-END:variables
}
