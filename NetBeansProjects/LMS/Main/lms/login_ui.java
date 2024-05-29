package lms;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;



public class login_ui extends javax.swing.JFrame {

 
    public login_ui() {
        initComponents();
        Signup_panel.setVisible(false);
        //ForgotPassword_Panel.setVisible(false);
    }
    
    private static void clearFields(Container container) {
    for (Component component : container.getComponents()) {
        if (component instanceof JTextField jTextField) {
            jTextField.setText("");
        }
    }
}
    
    //[SIGN UP] - Insert the Registration Details to Data Base
    public void insertSignupDetails() {
        
        String email = txt_email2.getText();
        String studentStrand = String.valueOf(Strand_ComboBox.getSelectedItem());
        String fullname = txt_fullname.getText();
        String section = txt_section.getText();
        String studentno = txt_studentno.getText();
        String address = txt_address.getText();
        String mobile = txt_mobile.getText();
        String password = txt_studentno.getText();

        try {
                Connection con = DBConnection.getConnection();
                String sql = "insert into users (email, strand, student_name, section, student_no, address, mobile, password) values (?,?,?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                
                pst.setString(1, email);
                pst.setString(2, studentStrand);
                pst.setString(3, fullname);
                pst.setString(4, section);
                pst.setString(5, studentno);
                pst.setString(6, address);
                pst.setString(7, mobile);
                pst.setString(8, password);

                
                int updatedRowCount = pst.executeUpdate();
                
                if (updatedRowCount > 0) {
                    
                    JOptionPane.showMessageDialog(this, "Signup Success!");
                    
                    JOptionPane.showMessageDialog(this, "Note: Your STUDENT ID will be your default password.");
                    clearFields(Signup_panel);
                    
                } else {
                    
                    JOptionPane.showMessageDialog(this, "Recorded Insertion Failed");
                    
                }
                        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    //[SIGN UP] - Signup Validation
    public boolean signupValidation() {
        
        String email = txt_email2.getText(); 
        String fullname = txt_fullname.getText();
        String section = txt_section.getText();
        String studentno = txt_studentno.getText();
        String address = txt_address.getText();
        String mobile = txt_mobile.getText();
        
        if (email.equals("")) {
            txt_email2.setHelperText("Please input Email.");
            return false;
        } else if (!email.matches("^(.+)@(\\S+)$")) {
            txt_email2.setHelperText("invalid Email Address.");
            return false;
        }
    
        if (fullname.equals("")) {
                txt_fullname.setHelperText("Please input Name");
                return false;
        }
        
        if (section.equals("")) {
                txt_section.setHelperText("Please input Section");
                return false;
        }
        
        if (studentno.equals("")) {
            txt_studentno.setHelperText("Please input Student No.");
            return false;
        } else if (!studentno.matches("^\\d{6}$")) {
            txt_studentno.setHelperText("invalid student id");
            return false;
        }
        
        if (address.equals("")) {
                txt_address.setHelperText("Please input Address");
                return false;
        }
        
        if (mobile.equals("")) {
            txt_mobile.setHelperText("Please input Mobile No.");
            return false;
        } else if (!mobile.matches("^\\d{11}$")) {
            txt_mobile.setHelperText("invalid mobile number.");
            return false;
        }
                return true;
    }
    
    //[SIGN UP] - checks if there's a duplicate emails in database
    public boolean checkDuplicateEmail() {
        
        String email = txt_email2.getText();
        boolean isExist = false;
        
        try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");
                
                PreparedStatement pst = con.prepareStatement("select * from users where email = ?");
                pst.setString(1, email);
                ResultSet rs = pst.executeQuery();
                
                if (rs.next()) {
                    isExist = true;
                } else {
                    isExist = false;
                }
                
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
    } 
    
    //[SIGN UP] - checks if there's a duplicate studen id in database
    public boolean checkDuplicateStudentID() {
        
        String studentno = txt_studentno.getText();
        boolean isExist = false;
        
        try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");
                
                PreparedStatement pst = con.prepareStatement("select * from users where student_no = ?");
                pst.setString(1, studentno);
                ResultSet rs = pst.executeQuery();
                
                if (rs.next()) {
                    isExist = true;
                } else {
                    isExist = false;
                }
                
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
    } 
    //[SIGN UP] - METHOD FOR BOTH EMAIL AND STUDENT ID DUPLICATE CHECKER
    public boolean checkDuplicateDetails() {
    return checkDuplicateEmail() || checkDuplicateStudentID();
}
  
    
   
// [SIGN IN] - Verification
public void login() {
    String email = txt_email.getText();
    String password = txt_password.getText();

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "");

        // Check login with email and password
        String query = "SELECT id, email, strand, student_name, section, student_no, address, mobile, role, password FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString("student_name");
                    String studentID = rs.getString("student_no");
                    String role = rs.getString("role");
                    String contactNo = rs.getString("mobile");
                    String address = rs.getString("address");
                    String strand = rs.getString("strand");
                    String section = rs.getString("section");

                    // Check the role of the user
                    if ("admin".equals(role)) {
                        new Admin_UI().setVisible(true);
                        this.dispose();
                    } else if ("librarian".equals(role)) {
                        new Librarian_UI().setVisible(true);
                        this.dispose();
                    } else if ("user".equals(role)){
                        // Create User_UI with student details
                        User_UI userUI = new User_UI(fullName, studentID, role, contactNo, address, strand, section);
                        userUI.setVisible(true);
                        this.dispose();
                    }
                    return; // Exit the method if login is successful
                }
            }
        }

        // If no match found
        JOptionPane.showMessageDialog(this, "Login failed");
    } catch (Exception e) {
        e.printStackTrace();
    }
}


private boolean authenticateAndFetchStudentData(String studentID, String password) {
    boolean isAuthenticated = false;
    String query = "SELECT * FROM users WHERE student_no = ? AND password = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement pst = con.prepareStatement(query)) {

        pst.setString(1, studentID);
        pst.setString(2, password);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            isAuthenticated = true;
            // Retrieve and display the student's data
            displayStudentData(rs);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Student ID or Password.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error during authentication.");
    }
    return isAuthenticated;
}

    private void displayStudentData(ResultSet rs) throws SQLException {
        // Assuming rs is not null and has valid data
        String studentName = rs.getString("student_name");
        String studentID = rs.getString("student_no");
        String studentRole = "STUDENT"; // Static value
        String contactNo = rs.getString("mobile");
        String address = rs.getString("address");
        String strand = rs.getString("strand");
        String section = rs.getString("section");

        // Assuming User_UI has static references to these components or methods to set their values
        User_UI.studentName_ph.setText(studentName);
        User_UI.studentID_ph.setText(studentID);
        User_UI.studentRole_ph.setText(studentRole);

        User_UI.tf_name.setText(studentName);
        User_UI.tf_contactno.setText(contactNo);
        User_UI.tf_studentid.setText(studentID);
        User_UI.tf_address.setText(address);
        User_UI.tf_strand.setText(strand);
        User_UI.tf_section.setText(section);

        // Make the text fields uneditable
        User_UI.tf_name.setEditable(false);
        User_UI.tf_contactno.setEditable(false);
        User_UI.tf_studentid.setEditable(false);
        User_UI.tf_address.setEditable(false);
        User_UI.tf_strand.setEditable(false);
        User_UI.tf_section.setEditable(false);
    }

    
    /*Forgot Password Function --
    public void resetPassword() {
    String identifier = txt_emailorstudentid.getText();

    if (identifier.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter your email or student ID.");
        return;
    }

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms", "root", "")) {
        String query = "SELECT * FROM users WHERE email = ? OR student_no = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, identifier);
            pst.setString(2, identifier);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String newPassword = txt_newpass.getText();
                    String confirmPassword = txt_confirmnewpass.getText();

                    if (newPassword.equals(confirmPassword)) {
                        String updateQuery = "UPDATE users SET password = ? WHERE email = ? OR student_no = ?";
                        try (PreparedStatement updatePst = con.prepareStatement(updateQuery)) {
                            updatePst.setString(1, newPassword);
                            updatePst.setString(2, identifier);
                            updatePst.setString(3, identifier);
                            updatePst.executeUpdate();

                            JOptionPane.showMessageDialog(this, "Password reset successful");
                            Signin_panel.setVisible(true);
                            ForgotPassword_Panel.setVisible(false);
                            Signup_panel.setVisible(false);
                            clearFields(ForgotPassword_Panel);
                            
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Password and confirmation do not match. Please try again.");
                    }
                    return;
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Account not found with the provided email or student ID.");
    } catch (Exception e) {
        e.printStackTrace();
    }
} */



    

  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Signin_panel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Signup_link = new javax.swing.JLabel();
        txt_password = new lms.PasswordTF();
        txt_email = new lms.UsernameTF();
        Signin_button = new lms.LoginButton();
        Signup_panel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        Strand_ComboBox = new javax.swing.JComboBox<>();
        txt_fullname = new lms.UsernameTF();
        txt_email2 = new lms.UsernameTF();
        txt_mobile = new lms.UsernameTF();
        txt_studentno = new lms.UsernameTF();
        txt_section = new lms.UsernameTF();
        txt_address = new lms.UsernameTF();
        Signup_button = new lms.LoginButton();
        jLabel6 = new javax.swing.JLabel();
        close = new javax.swing.JLabel();
        minimize = new javax.swing.JLabel();
        background = new javax.swing.JLabel();

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

        Signin_panel.setBackground(new java.awt.Color(255, 255, 255));
        Signin_panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(207, 76, 56));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("SIGN IN");
        Signin_panel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 180, 70));

        jLabel3.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(207, 76, 56));
        jLabel3.setText("Don't have an account?");
        Signin_panel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 370, 150, 20));

        Signup_link.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        Signup_link.setForeground(new java.awt.Color(207, 76, 56));
        Signup_link.setText("Sign up");
        Signup_link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                Signup_linkMousePressed(evt);
            }
        });
        Signin_panel.add(Signup_link, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 370, 50, 20));

        txt_password.setToolTipText("");
        txt_password.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_password.setLabelText("Password");
        txt_password.setLineColor(new java.awt.Color(0, 0, 0));
        txt_password.setShowAndHide(true);
        txt_password.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_passwordFocusLost(evt);
            }
        });
        txt_password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_passwordActionPerformed(evt);
            }
        });
        Signin_panel.add(txt_password, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 220, 220, 60));

        txt_email.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_email.setLabelText("Email");
        txt_email.setLineColor(new java.awt.Color(0, 0, 0));
        txt_email.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_emailFocusLost(evt);
            }
        });
        txt_email.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_emailActionPerformed(evt);
            }
        });
        Signin_panel.add(txt_email, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 140, 220, 70));

        Signin_button.setBackground(new java.awt.Color(51, 51, 51));
        Signin_button.setForeground(new java.awt.Color(207, 76, 56));
        Signin_button.setText("Sign in");
        Signin_button.setFont(new java.awt.Font("Century Gothic", 0, 24)); // NOI18N
        Signin_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Signin_buttonActionPerformed(evt);
            }
        });
        Signin_panel.add(Signin_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 310, 120, 40));

        getContentPane().add(Signin_panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 50, 400, 530));

        Signup_panel.setBackground(new java.awt.Color(255, 255, 255));
        Signup_panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(207, 76, 56));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("SIGN UP");
        Signup_panel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 200, 60));

        jLabel4.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(207, 76, 56));
        jLabel4.setText("Strand:");
        Signup_panel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 310, 120, -1));

        jLabel5.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(207, 76, 56));
        jLabel5.setText("Sign In");
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
        });
        Signup_panel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 470, 40, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 3, 10)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(207, 76, 56));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Note: Your STUDENT ID will be your default password.");
        Signup_panel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 510, 400, 30));

        Strand_ComboBox.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        Strand_ComboBox.setForeground(new java.awt.Color(207, 76, 56));
        Strand_ComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ICT", "GAS", "HUMSS", "ABM" }));
        Strand_ComboBox.setBorder(null);
        Strand_ComboBox.setOpaque(true);
        Signup_panel.add(Strand_ComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 330, 180, 40));

        txt_fullname.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_fullname.setLabelText("Full Name");
        txt_fullname.setLineColor(new java.awt.Color(0, 0, 0));
        txt_fullname.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_fullnameFocusLost(evt);
            }
        });
        txt_fullname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_fullname(evt);
            }
        });
        Signup_panel.add(txt_fullname, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 370, 60));

        txt_email2.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_email2.setLabelText("Email");
        txt_email2.setLineColor(new java.awt.Color(0, 0, 0));
        txt_email2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_email2FocusLost(evt);
            }
        });
        Signup_panel.add(txt_email2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 180, 60));

        txt_mobile.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_mobile.setLabelText("Mobile No.");
        txt_mobile.setLineColor(new java.awt.Color(0, 0, 0));
        txt_mobile.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_mobileFocusLost(evt);
            }
        });
        txt_mobile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_mobileActionPerformed(evt);
            }
        });
        Signup_panel.add(txt_mobile, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 240, 180, 60));

        txt_studentno.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_studentno.setLabelText("Student ID");
        txt_studentno.setLineColor(new java.awt.Color(0, 0, 0));
        txt_studentno.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_studentnoFocusLost(evt);
            }
        });
        txt_studentno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_studentnoActionPerformed(evt);
            }
        });
        Signup_panel.add(txt_studentno, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 120, 180, 60));

        txt_section.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_section.setLabelText("Section");
        txt_section.setLineColor(new java.awt.Color(0, 0, 0));
        txt_section.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_sectionFocusLost(evt);
            }
        });
        Signup_panel.add(txt_section, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 240, 180, 60));

        txt_address.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txt_address.setLabelText("Address");
        txt_address.setLineColor(new java.awt.Color(0, 0, 0));
        txt_address.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_addressFocusLost(evt);
            }
        });
        txt_address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_addressActionPerformed(evt);
            }
        });
        Signup_panel.add(txt_address, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 180, 60));

        Signup_button.setBackground(new java.awt.Color(51, 51, 51));
        Signup_button.setForeground(new java.awt.Color(207, 76, 56));
        Signup_button.setText("Sign up");
        Signup_button.setFont(new java.awt.Font("Century Gothic", 0, 24)); // NOI18N
        Signup_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Signup_buttonActionPerformed(evt);
            }
        });
        Signup_panel.add(Signup_button, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 400, 120, 40));

        jLabel6.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(207, 76, 56));
        jLabel6.setText("Already have an account?");
        Signup_panel.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 470, 160, -1));

        getContentPane().add(Signup_panel, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 50, 400, 540));

        close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/close.png"))); // NOI18N
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeMousePressed(evt);
            }
        });
        getContentPane().add(close, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 10, -1, -1));

        minimize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/minimize.png"))); // NOI18N
        minimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                minimizeMousePressed(evt);
            }
        });
        getContentPane().add(minimize, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 10, -1, -1));

        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/LOGIN UI.png"))); // NOI18N
        getContentPane().add(background, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void Signup_linkMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Signup_linkMousePressed
    Signup_panel.setVisible(true);
    Signin_panel.setVisible(false);
        clearFields(Signin_panel);
    }//GEN-LAST:event_Signup_linkMousePressed

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
    Signin_panel.setVisible(true);
    Signup_panel.setVisible(false);
        clearFields(Signup_panel);
    }//GEN-LAST:event_jLabel5MousePressed

    private void closeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMousePressed
    System.exit(0); 
    }//GEN-LAST:event_closeMousePressed

    private void minimizeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMousePressed
    setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_minimizeMousePressed
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

    private void Signin_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Signin_buttonActionPerformed
        login();
    }//GEN-LAST:event_Signin_buttonActionPerformed

    private void Signup_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Signup_buttonActionPerformed
    if (signupValidation() == true) {
            if (checkDuplicateEmail() == false && checkDuplicateStudentID() == false) {
                insertSignupDetails();
                Signup_panel.setVisible(false);
                Signin_panel.setVisible(true);
            } else {
                if (checkDuplicateEmail() == true) {
                    txt_email2.setHelperText("Email already exist");
                }
                if (checkDuplicateStudentID() == true) {
                    txt_mobile.setHelperText("Student ID already exist");
                }
            }
        }
    }//GEN-LAST:event_Signup_buttonActionPerformed

    private void txt_emailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_emailFocusLost
        String email = txt_email.getText();
        
        if (email.equals("")) {
                txt_email.setHelperText("Please input Email");
        }else{
                txt_email.setHelperText("");
        }
    }//GEN-LAST:event_txt_emailFocusLost

    private void txt_passwordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_passwordFocusLost
        String password = txt_password.getText();
        
        if (password.equals("")) {
                txt_password.setHelperText("Please input password");
        }else{
                txt_password.setHelperText("");
        }
    }//GEN-LAST:event_txt_passwordFocusLost

    private void txt_email2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_email2FocusLost
        String email2 = txt_email2.getText();
        
        if (email2.equals("")) {
                txt_email2.setHelperText("Please input Email");
        }else{
                txt_email2.setHelperText("");
        }
    }//GEN-LAST:event_txt_email2FocusLost

    private void txt_fullnameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_fullnameFocusLost
        String fullname = txt_fullname.getText();
        
        if (fullname.equals("")) {
                txt_fullname.setHelperText("Please input Name");
        }else{
                txt_fullname.setHelperText("");
        }
    }//GEN-LAST:event_txt_fullnameFocusLost

    private void txt_sectionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_sectionFocusLost
        String section = txt_mobile.getText();
        
        if (section.equals("")) {
                txt_section.setHelperText("Please input section");
        }else{
                txt_section.setHelperText("");
        }
    }//GEN-LAST:event_txt_sectionFocusLost

    private void txt_studentnoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_studentnoFocusLost
        String studentno = txt_mobile.getText();
        
        if (studentno.equals("")) {
                txt_studentno.setHelperText("Please input student no.");
        }else{
                txt_studentno.setHelperText("");
        }
    }//GEN-LAST:event_txt_studentnoFocusLost

    private void txt_addressFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_addressFocusLost
        String address = txt_address.getText();
        
        if (address.equals("")) {
                txt_address.setHelperText("Please input address");
        }else{
                txt_address.setHelperText("");
        }
    }//GEN-LAST:event_txt_addressFocusLost

    private void txt_mobileFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_mobileFocusLost
        String mobile = txt_mobile.getText();
        
        if (mobile.equals("")) {
                txt_mobile.setHelperText("Please input mobile no.");
        }else{
                txt_mobile.setHelperText("");
        }
    }//GEN-LAST:event_txt_mobileFocusLost

    private void txt_passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_passwordActionPerformed
    Signin_button.doClick();
    }//GEN-LAST:event_txt_passwordActionPerformed

    private void txt_mobileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_mobileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_mobileActionPerformed

    private void txt_fullnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameTF1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameTF1ActionPerformed

    private void txt_sectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_studentnoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_studentnoActionPerformed

    private void txt_studentnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_studentno1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_studentno1ActionPerformed

    private void txt_addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_addressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_addressActionPerformed

    private void txt_fullname(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_fullname
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_fullname

    private void txt_emailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_emailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_emailActionPerformed

 
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
            java.util.logging.Logger.getLogger(login_ui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(login_ui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(login_ui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(login_ui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new login_ui().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private lms.LoginButton Signin_button;
    private javax.swing.JPanel Signin_panel;
    private lms.LoginButton Signup_button;
    private javax.swing.JLabel Signup_link;
    private javax.swing.JPanel Signup_panel;
    private javax.swing.JComboBox<String> Strand_ComboBox;
    private javax.swing.JLabel background;
    private javax.swing.JLabel close;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel minimize;
    private lms.UsernameTF txt_address;
    public static lms.UsernameTF txt_email;
    private lms.UsernameTF txt_email2;
    private lms.UsernameTF txt_fullname;
    private lms.UsernameTF txt_mobile;
    private lms.PasswordTF txt_password;
    private lms.UsernameTF txt_section;
    private lms.UsernameTF txt_studentno;
    // End of variables declaration//GEN-END:variables
}
