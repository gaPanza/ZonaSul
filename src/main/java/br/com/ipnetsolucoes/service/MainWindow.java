package br.com.ipnetsolucoes.service;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

import org.apache.log4j.Level;

import java.awt.Toolkit;
import javax.swing.JTextPane;

public class MainWindow {

	private JFrame frame;
	private JPasswordField passwordField;
	private static JTextField textField;
	private static JButton btnEntrar;
	private static JFileChooser jfc = new JFileChooser();
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainWindow() {
		initialize();
	}

	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 558, 363);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField.setBounds(57, 100, 328, 36);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		btnEntrar = new JButton("Entrar");
		btnEntrar.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnEntrar.setBounds(294, 210, 91, 29);
		frame.getContentPane().add(btnEntrar);

		JLabel lblEmail = new JLabel("Email");
		lblEmail.setBounds(69, 66, 51, 23);
		frame.getContentPane().add(lblEmail);

		JLabel lblNewLabel = new JLabel("IPNET Logo");
		lblNewLabel.setIcon(new ImageIcon(MainWindow.class.getResource("/br/com/ipnetsolucoes/a.png")));
		lblNewLabel.setBounds(419, 53, 96, 100);
		frame.getContentPane().add(lblNewLabel);
		frame.setVisible(true);
	}
}
