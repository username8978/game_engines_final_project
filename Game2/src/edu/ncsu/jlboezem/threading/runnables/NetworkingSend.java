package edu.ncsu.jlboezem.threading.runnables;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.ncsu.jlboezem.communication.packets.Packet;
import edu.ncsu.jlboezem.communication.packets.PacketStopThread;
import edu.ncsu.jlboezem.threading.ThreadManager;

public class NetworkingSend implements Runnable {

	@Override
	public void run() {
		String client = "";
		try {
			ObjectOutputStream objOut;
			Socket mySock = ThreadManager.INSTANCE.sendSocketsWaiting.take();
			client = mySock.getInetAddress().getCanonicalHostName() + ":" + mySock.getPort();
			String mySendQueue = "Send" + client;
			ThreadManager.INSTANCE.addNamedQueue("Events" + client);
			objOut = new ObjectOutputStream(mySock.getOutputStream());
			while (!ThreadManager.INSTANCE.isStopping()) {
				Object input = ThreadManager.INSTANCE.getQueueData(mySendQueue);
				if (null == input) {
					input = ThreadManager.INSTANCE.getQueueData("Events" + client);
					if (null == input) {
						continue;
					}
				}
				if (input instanceof PacketStopThread) {
					break;
				}
				objOut.writeObject(input);
				objOut.reset();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			//TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
