package edu.ncsu.jlboezem.threading.runnables;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.ncsu.jlboezem.threading.ThreadManager;
import edu.ncsu.jlboezem.threading.ThreadManager.ThreadType;

public class NetworkingListen implements Runnable {
		@Override
		public void run() {
			ServerSocket sock = ThreadManager.INSTANCE.getServerSocket();
			if (null == sock) {
				System.out.println("Couldn't find Server Socket to listen with.");
				return;
			}
			while (!ThreadManager.INSTANCE.isStopping()) {
				try {
					Socket newClient = sock.accept();
					ThreadManager.INSTANCE.receiveSocketsWaiting.add(newClient);
					ThreadManager.INSTANCE.sendSocketsWaiting.add(newClient);
					if (!ThreadManager.INSTANCE.receiveSocketsWaiting.hasWaitingConsumer() && !ThreadManager.INSTANCE.receiveSocketsWaiting.isEmpty()) {
						ThreadManager.INSTANCE.addThread(ThreadType.NetworkingReceive);
						ThreadManager.INSTANCE.addThread(ThreadType.NetworkingSend);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
