package br.com.ipnetsolucoes.main;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import br.com.ipnetsolucoes.service.MainWindow;
import br.com.ipnetsolucoes.beans.Retorno;
import br.com.ipnetsolucoes.main.CsvParser;
import br.com.ipnetsolucoes.util.*;
import br.com.ipnetsolucoes.service.*;

public class Screens {

	private static JTextField textField;
	private static JButton btnEntrar;
	private static JFileChooser jfc = new JFileChooser();
	private static JFrame frame;
	static Logger log = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		mainActivity();
	}

	public static void mainActivity() {
		frame = mainFrame();
		btnEntrar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);

				String email = textField.getText().trim();

				if (!email.contains("@")) {
					// Retorna a tela de inserção de email com o erro de
					// inserção não @zonasul

					log.log(Level.INFO, "Email não contém @ZonaSul " + email);
					JOptionPane.showMessageDialog(null, "O email inserido não é um email @ZonaSul", "Erro de Domínio",
							JOptionPane.ERROR_MESSAGE);
					mainActivity();

				} else {
					// Autenticar o domínio via API

					// Recuperar todos os contatos do email

					// Recuperar os contatos do domínio
					// Realizar as alterações necessárias
					// Jogar as alterações no servidor

					JFrame fram3 = addCsvFrame(email);

				}

			}
		});

	}

	public static JFrame addCsvFrame(String email) {
		JFrame fram3 = new JFrame();
		FileFilter filter = new FileNameExtensionFilter("CSV file", "csv");

		fram3.setBounds(100, 100, 558, 363);
		fram3.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fram3.getContentPane().setLayout(null);

		jfc.setBounds(10, 11, 522, 302);
		jfc.setFileFilter(filter);
		jfc.setVisible(true);
		fram3.getContentPane().add(jfc);

		int result = jfc.showSaveDialog(fram3);

		if (result == JFileChooser.APPROVE_OPTION) {

			System.out.println("Parsed");
			File file = jfc.getSelectedFile();
			log.log(Level.INFO, file.getName());

			// Chama o parser CSV para fazer o parse

			try {
				Retorno<Configuracao> config = new Retorno<Configuracao>();
				Properties2 props = new Properties2();
				
				Configuracao configuration = new Configuracao(props.getProps(), file.getPath());
				config.setObjeto(configuration);

				ContatosService cs = new ContatosService();
				cs.ProcessAtualization(config, email, file);

				System.out.println("Tudo ok");

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (result == JFileChooser.CANCEL_OPTION) {
			System.out.println("F");
			log.log(Level.INFO, "Cancelado");
		}
		fram3.setVisible(true);

		return fram3;

	}

	public static JFrame mainFrame() {
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

		return frame;
	}

}
