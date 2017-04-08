package edu.ncsu.jlboezem.threading.runnables;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.ncsu.jlboezem.communication.packets.Packet;
import edu.ncsu.jlboezem.communication.packets.PacketStopThread;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class NetworkingReceive implements Runnable {

	@Override
	public void run() {
		String client = "";
		try {
			ObjectInputStream objIn;
			Socket mySock = ThreadManager.INSTANCE.receiveSocketsWaiting.take();
			client = mySock.getInetAddress().getCanonicalHostName() + ":" + mySock.getPort();
			objIn = new ObjectInputStream(mySock.getInputStream());
			while (!ThreadManager.INSTANCE.isStopping()) {
				Object input = ThreadManager.INSTANCE.getInput(this.getClass());
				if (input instanceof PacketStopThread)
					break;
				
				input = objIn.readObject();
				if (null == input)
					continue;
				Packet packet = (Packet)input;
				packet.from = client;
				ThreadManager.INSTANCE.writeToQueue("Receive", packet);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println("Connection to " + client + " dropped.");
		} catch (IOException e) {
			System.out.println("Connection to " + client + " dropped.");
			//TODO: clientDroppedEvent
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
