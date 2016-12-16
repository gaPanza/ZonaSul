package br.com.ipnetsolucoes.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.opencsv.CSVReader;

public class CsvParser {
	static Logger log = Logger.getLogger(CsvParser.class.getName());

	public static void CsvUtil(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-16"), ',',
							'\'', 1);
					String[] nextLine;
					while ((nextLine = reader.readNext()) != null) {
						/*
						 * Estrutura dos CSVs da Google:
						 * 31 Colunas
						 * Primeiro Campo: Nome
						 * Campo 29: Email
						 */
						try {
							System.out.println("[" + nextLine[0] + "]" + "," + nextLine[1] + "," + nextLine[2] + ","
									+ nextLine[3] + "," + nextLine[27] + "," + nextLine[28] + "," + nextLine[29] + ","
									+ nextLine[30] + ";");
							
							//Continuar adição de logica
							
							

						} catch (ArrayIndexOutOfBoundsException e) {
							log.log(Level.FATAL, "Erro ao Processar CSV" + e.getMessage());
							JOptionPane.showMessageDialog(null, "O CSV inserido não é um CSV Google, tente novamente",
									"CSV Inválido", JOptionPane.ERROR_MESSAGE);
							br.com.ipnetsolucoes.main.Screens.mainActivity();
						}
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		
		
		

	}
}
