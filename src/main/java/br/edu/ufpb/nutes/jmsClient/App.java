package br.edu.ufpb.nutes.jmsClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z y");

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);

		JSONObject device = new JSONObject();

		ArrayList<String> wireAdapter = getWireNetworkInterfaces();

		try {
			if (wireAdapter.size() > 1) {
				while (true) {
					System.out.println(
							"Digite o NÚMERO referente a interface de rede que se comunicará com a Central de Monitoramento:\n");

					for (int i = 0; i < wireAdapter.size(); i++) {
						System.out.println(i + 1 + "- " + wireAdapter.get(i));
					}

					int option = scanner.nextInt();

					if (option >= 1 && option <= 2) {
						device.put("Address", NetworkInterface.getByName(wireAdapter.get(option - 1))
								.getInterfaceAddresses().get(1).getAddress().getHostAddress());
						break;
					}
				}
			}else {
				device.put("Address", NetworkInterface.getByName(wireAdapter.get(0))
						.getInterfaceAddresses().get(1).getAddress().getHostAddress());
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			System.out.println("Digite o NÚMERO da operação referente ao situação do paciente:\n" + "1- Urgência\n"
					+ "2- Emergência\n" + "3- Normal\n" + "-1 para Sair\n");

			int option = scanner.nextInt();

			switch (option) {
			case 1:
				device.put("Alarm", Priority.URGENCIA);
				device.put("Timestamp", dateFormat.format(cal.getTime()));
				break;
			case 2:
				device.put("Alarm", Priority.EMERGENCIA);
				device.put("Timestamp", dateFormat.format(cal.getTime()));
				break;
			case 3:
				device.put("Alarm", Priority.NORMAL);
				device.put("Timestamp", dateFormat.format(cal.getTime()));
				break;
			case -1:
				System.exit(0);
			default:
				System.out.println("Valor Inválido");
			}

			String hostName;
			try {
				hostName = InetAddress.getLocalHost().getHostName();
				
				jmsCommunication(new JSONObject().put(hostName, device));

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				System.out.println("Não houve comunicação com a central de monitoramento");
//				e.printStackTrace();
			}
			
			
		}
	}

	private static void jmsCommunication(JSONObject device) throws JMSException {
		// TODO Auto-generated method stub
		Connection connection = null;
		try {
			// Producer
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.3.1:61616");
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue("alarms");

			Message msg = session.createTextMessage(device.toString());
			MessageProducer producer = session.createProducer(queue);
			System.out.println("Sending text '" + device.toString() + "'");
			producer.send(msg);

		} finally {
			if (connection != null) {
				connection.close();
			}
//			broker.stop();
		}

	}

	private static ArrayList<String> getWirelessNetworkInterfaces() throws IOException {
		ArrayList<String> networkInterfaces = new ArrayList<String>();

		BufferedReader buffIn = null;

		Runtime rt = Runtime.getRuntime();
		Process proc = null;

		String command = "iwconfig";

		proc = rt.exec(command);
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (proc.exitValue() == 0) {
			buffIn = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line;
			while ((line = buffIn.readLine()) != null) {
				if (line.indexOf("IEEE 802.11") != -1)
					networkInterfaces.add(line.substring(0, line.indexOf("IEEE 802.11")).replaceAll(" ", ""));
			}

		}

		return networkInterfaces;
	}

	private static ArrayList<String> getWireNetworkInterfaces() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			ArrayList<String> wireAdapter = new ArrayList<String>();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
				if (!networkInterface.isLoopback()
						&& !getWirelessNetworkInterfaces().contains(networkInterface.getName()))
					wireAdapter.add(networkInterface.getName());
			}

			return wireAdapter;

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
