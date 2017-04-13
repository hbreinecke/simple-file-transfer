package com.simple.transfer;

class RunnableThread implements Runnable {

	Thread runner;
	IReceiver receiver;
	
	public RunnableThread() {
	}

	public RunnableThread(String threadName, IReceiver receiver) {
		this.receiver = receiver;
		runner = new Thread(this, threadName); // (1) Create a new thread.
		// System.out.println(runner.getName());
		runner.start(); // (2) Start the thread.
	}

	public void stop() {
		runner.interrupt();
		//runner=null;
		//System.out.println("Interrupted");
	}

	public void run() {
		// Display info about this particular thread

		while (!runner.isInterrupted()) {
			//System.out.println("Und er läuft und er läuft und er läuft");
			receiver.receive();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				runner.interrupt();
				System.out.println("Unterbrechung in sleep()");
			}
		}
		//System.out.println(Thread.currentThread());
		// Thread.sleep(1000);
	}
}
