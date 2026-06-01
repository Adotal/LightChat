package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author adotal
 */
public class LoginView extends JFrame {

    public LoginView() {

        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // 2. Create the main Panel with a GridBagLayout for alignment
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding

        // 3. Username Label and Text Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        JTextField userField = new JTextField(15);
        panel.add(userField, gbc);

        // 4. Password Label and Password Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        JPasswordField passField = new JPasswordField(15);
        panel.add(passField, gbc);

        // 5. Login Button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        // 6. Button Action Listener (Handle Login Logic)
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                // Replace this block with your actual authentication logic
                if (username.equals("admin") && password.equals("password123")) {
                    JOptionPane.showMessageDialog(getContentPane(), "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(getContentPane(), "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 7. Add panel to getContentPane() and make it visible
        getContentPane().add(panel);
    }
}
