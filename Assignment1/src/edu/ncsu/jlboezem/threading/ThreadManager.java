package edu.ncsu.jlboezem.threading;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import edu.ncsu.jlboezem.communication.ConnectionManager;
import edu.ncsu.jlboezem.communication.packets.Packet;
import edu.ncsu.jlboezem.threading.runnables.NetworkingListen;
import edu.ncsu.jlboezem.threading.runnables.NetworkingProcessor;
import edu.ncsu.jlboezem.threading.runnables.NetworkingReceive;
import edu.ncsu.jlboezem.threading.runnables.NetworkingSend;

public class ThreadManager {
	public static final ThreadManager INSTANCE = new ThreadManager();
	
	public enum ThreadType {
		NetworkingReceive, NetworkingSend, NetworkingListen, NetworkingProcessor, PropertyTicker
	}
	
	protected ConcurrentHashMap<Class<? extends Runnable>, TransferQueue<Packet>> threadDataIn;
	protected ConcurrentHashMap<Class<? extends Runnable>, TransferQueue<Packet>> threadDataOut;
	
	//TODO: look into replacing dataIn and dataOut threads with just named queues.
	protected ConcurrentHashMap<String, TransferQueue<Packet>> namedQueues;
	
	protected Executor threadPool;

	public TransferQueue<Socket> receiveSocketsWaiting;
	public TransferQueue<Socket> sendSocketsWaiting;
	private ServerSocket serverSocket;
	
	private boolean stopping;
	
	private String localName;
	
	public ThreadManager() {
		serverSocket = null;
		threadPool = Executors.newCachedThreadPool();
		threadDataIn = new ConcurrentHashMap<Class<? extends Runnable>, TransferQueue<Packet>>();
		threadDataOut = new ConcurrentHashMap<Class<? extends Runnable>, TransferQueue<Packet>>();
		namedQueues = new ConcurrentHashMap<String, TransferQueue<Packet>>();
		receiveSocketsWaiting = new LinkedTransferQueue<Socket>();
		sendSocketsWaiting = new LinkedTransferQueue<Socket>();
	}
	
	public void start() {
		stopping = false;
	}

	public boolean addThread(ThreadType type) {
		Class<? extends Runnable> runnable = getRunnableClassFromType(type);
		if (null == runnable)
			return false;
		try {
			threadPool.execute(runnable.newInstance());
			threadDataIn.putIfAbsent(runnable, new LinkedTransferQueue<Packet>());
			threadDataOut.putIfAbsent(runnable, new LinkedTransferQueue<Packet>());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public void writeToQueueLike(String target, Packet message) {
		namedQueues.entrySet().parallelStream().filter(entry -> entry.getKey().matches(target)).forEach(entry -> entry.getValue().add(message));
	}
	/**
	 * Create a named queue if it doesn't exist.
	 * @param name name of the queue
	 */
	public void addNamedQueue(String name) {
		namedQueues.putIfAbsent(name, new LinkedTransferQueue<Packet>());
	}
	/**
	 * Write a Packet to the given named queue.
	 * @param name name of queue to write to.
	 * @param obj Packet to write to the queue.
	 * @throws InterruptedException
	 * @throws NullPointerException if invalid name or Packet is null.
	 */
	public void writeToQueue(String name, Packet obj) throws InterruptedException {
		try {
			namedQueues.get(name).add(obj);
		} catch (NullPointerException e) {
			addNamedQueue(name);
			namedQueues.get(name).add(obj);
		}
	}
	/**
	 * Wait (blocking) until given named queue has data and return the first Packet.
	 * @param name name of queue to get data from.
	 * @return first Packet in queue.
	 * @throws InterruptedException
	 * @throws NullPointerException if invalid name.
	 */
	public Packet waitForQueueData(String name) throws InterruptedException {
		try {
			return namedQueues.get(name).take();
		} catch (NullPointerException e) {
			addNamedQueue(name);
			return namedQueues.get(name).take();
		}
	}
	/**
	 * Gets the first Packet in queue, or null if queue is empty.
	 * @param name name of queue to get data from.
	 * @return first Packet in queue, or null if nothing in queue.
	 * @throws InterruptedException
	 * @throws NullPointerException if invalid name.
	 */
	public Packet getQueueData(String name) throws InterruptedException, NullPointerException {
		try {
			return namedQueues.get(name).poll();
		} catch (NullPointerException e) {
			addNamedQueue(name);
			return namedQueues.get(name).poll();
		}
	}
	public Packet waitForInput(Class<? extends Runnable> runnable) throws InterruptedException {
		return threadDataIn.get(runnable).take();
	}
	public Packet getInput(Class<? extends Runnable> runnable) throws InterruptedException {
		return threadDataIn.get(runnable).poll();
	}
	public Packet waitForOutput(Class<? extends Runnable> runnable) throws InterruptedException {
		return threadDataOut.get(runnable).take();
	}
	public void writeOutput(Class<? extends Runnable> runnable, Packet obj) throws InterruptedException {
		threadDataOut.get(runnable).add(obj);
	}
	public void writeInput(Class<? extends Runnable> runnable, Packet obj) throws InterruptedException {
		threadDataIn.get(runnable).add(obj);
	}
	
	protected Class<? extends Runnable> getRunnableClassFromType(ThreadType type) {
		switch(type) {
		case NetworkingListen:
			return NetworkingListen.class;
		case NetworkingReceive:
			return NetworkingReceive.class;
		case NetworkingSend:
			return NetworkingSend.class;
		case NetworkingProcessor:
			return NetworkingProcessor.class;
		default:
			return null;
		}
	}
	
	public void stop() {
		stopping = true;
	}
	
	public boolean isStopping() {
		return stopping;
	}

	public void setServerSocket(ServerSocket sock) {
		this.serverSocket = sock;
	}
	public ServerSocket getServerSocket() {
		if (!"Server".equals(ConnectionManager.INSTANCE.getType())) {
			return null;
		}
		return serverSocket;
	}

	public void setLocalName(String name) {
		this.localName = name;
	}
	public String getLocalName() {
		return localName;
	}
}
