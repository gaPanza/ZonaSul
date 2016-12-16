package br.com.ipnetsolucoes.main;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import br.com.ipnetsolucoes.service.MainWindow;

public class Main extends JFrame {

	private static final long serialVersionUID = -1000023600374350168L;
	static Logger log = Logger.getLogger(Main.class.getName());
	private static JTextField textField;
	private static JButton btnEntrar;
	private static JFileChooser jfc = new JFileChooser();
	private static JButton btnFile = new JButton("Selecione o Arquivo csv");
	private static JFrame frame;

	public Main(String titulo) {
		super(titulo);
		log.log(Level.INFO, "Inicializando Visão do Usuário");

	}

	public static void main(String[] args) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				new Main("IPNET Migrador");
			}
		};
		EventQueue.invokeLater(r);

		String logName = new SimpleDateFormat("YYYY_MM_dd_hh_mm_ss").format(new Date()) + "_EMAIL";
		log.log(Level.INFO, "Data de execução: " + logName);

		initialize();

		btnEntrar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				String email = textField.getText().trim();

				// Tratar o email com validação

				if (!email.contains("@zonasul")) {
					// Adicionar uma tela de erro que fecha a aplicação
					System.out.println("Email não contem @ZonaSul");
					log.log(Level.INFO, "Email não contém @ZonaSul " + email);
					
				}

				// Realiza Autenticação

				// Pede Permissão ao Usuário

				// Starta a nova tela
				secondScreen();
			};
		});

		// Realiza o login e valida as credenciais com o XOAUTH

		// Aguarda o Input do Usuário e Senha por parte do utilizador

		// Aguarda o Input do CSV

		// Realiza as operações em Async
	}

	private static void onPressed() {

	}

	private static void initialize() {
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

	private static void secondScreen() {
		JFrame fram3 = new JFrame();
		fram3.setBounds(100, 100, 558, 363);
		fram3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fram3.getContentPane().setLayout(null);

		jfc.setBounds(10, 11, 522, 302);
		jfc.setVisible(true);
		
		fram3.setVisible(true);

		int result = jfc.showOpenDialog(fram3);

		if (result == JFileChooser.APPROVE_OPTION) {
			log.log(Level.INFO, jfc.getSelectedFile().getName());
			log.log(Level.INFO, "Aprovado");
		} else if (result == JFileChooser.CANCEL_OPTION) {
			System.out.println("F");
			log.log(Level.INFO, "Cancelado");
		}

	}

}
