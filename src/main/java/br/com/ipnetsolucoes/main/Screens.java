package br.com.ipnetsolucoes.main;


import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import br.com.ipnetsolucoes.beans.Retorno;

import br.com.ipnetsolucoes.util.*;
import br.com.ipnetsolucoes.service.*;

public class Screens {
	private static JButton btnEntrar;
	private static JFileChooser jfc = new JFileChooser();
	private static JFrame frame;
	static Logger log = Logger.getLogger(Screens.class.getName());

	public static void main(String[] args) {
		mainActivity();
	}

	public static void mainActivity() {
		frame = mainFrame();
		btnEntrar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
					addCsvFrame();
			}
				});

	

	}

	public static JFrame addCsvFrame() {
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
				cs.ProcessAtualization(config, file);

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
		frame.setBounds(0, 0, 640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		btnEntrar = new JButton("Atualizar Contatos");
		btnEntrar.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnEntrar.setBounds(20, 250, 200, 30);
		frame.getContentPane().add(btnEntrar);

		JLabel fundo = new JLabel("");
		fundo.setIcon(new ImageIcon(Screens.class.getResource("/br/com/ipnetsolucoes/logo.jpg")));
		fundo.setBounds(0, 0, 640, 480);
		
		JLabel logo = new JLabel("IPNET Logo");
		ImageIcon icon = new ImageIcon(Screens.class.getResource("/br/com/ipnetsolucoes/logo.gif"));
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth()/2, icon.getIconHeight()/2, 100));
		logo.setIcon(icon);
		logo.setBounds(40, 180, icon.getIconWidth(), icon.getIconHeight());
		
		fundo.add(logo);
		
		frame.getContentPane().add(fundo);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		return frame;
	}

}
